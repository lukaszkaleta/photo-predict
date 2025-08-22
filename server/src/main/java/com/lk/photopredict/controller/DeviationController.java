package com.lk.photopredict.controller;

import com.lk.photopredict.analysis.RecordTranscript;
import com.lk.photopredict.analysis.VertexAIImageDescriptionGenerator;
import com.lk.photopredict.analysis.VertexAITextGenerator;
import com.lk.photopredict.payload.DeviationAnalysis;
import com.lk.photopredict.payload.Solution;
import com.lk.photopredict.repository.DataStore;
import com.lk.photopredict.payload.Deviation;
import com.lk.photopredict.payload.DeviationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/deviations")
public class DeviationController {
    private static final Logger logger = LoggerFactory.getLogger(DeviationController.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final DataStore deviationStorage;
    private final DataStore photosStorage;
    private final DataStore recordingStorage;

    private final ObjectMapper objectMapper;

    private final CredentialsProvider credentialsProvider;

    @Autowired
    public DeviationController(
            ObjectMapper objectMapper,
            @Qualifier("image") DataStore imageStore,
            @Qualifier("deviation") DataStore deviationStorage,
            @Qualifier("recording") DataStore recordingStorage, CredentialsProvider credentialsProvider) {
        this.deviationStorage = deviationStorage;
        this.photosStorage = imageStore;
        this.recordingStorage = recordingStorage;
        this.objectMapper = objectMapper;
        this.credentialsProvider = credentialsProvider;
    }

    @PostMapping
    public Deviation create(@RequestBody DeviationRequest request) {
        logger.info("Creating new deviation");

        // Validate that at least one field is not empty
        if ((request.images() == null || request.images().isEmpty()) &&
            (request.recordings() == null || request.recordings().isEmpty()) &&
            (request.comment() == null || request.comment().trim().isEmpty())) {
            logger.warn("Attempted to create deviation with all empty fields");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "At least one of images, recordings, or comment must be provided");
        }

        String deviationId = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        // Only process non-empty fields
        List<String> photos = (request.images() != null && !request.images().isEmpty())
            ? photosStorage.decodeAndSaveAll(request.images())
            : new ArrayList<>();

        List<String> recordings = (request.recordings() != null && !request.recordings().isEmpty())
            ? recordingStorage.decodeAndSaveAll(request.recordings())
            : new ArrayList<>();

        String comment = (request.comment() != null) ? request.comment().trim() : "";

        // Save deviation metadata
        Deviation deviation = new Deviation(
                deviationId,
                timestamp,
                photos,
                recordings,
                comment
        );
        String deviationPayload;
        try {
            deviationPayload = objectMapper.writeValueAsString(deviation);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize deviation", e);
            throw new RuntimeException(e);
        }
        deviationStorage.save(deviationPayload, deviationId);
        logger.info("Successfully created deviation with ID: {}", deviationId);

        // Start analysis only if there are images or recordings to analyze
        if (!photos.isEmpty() || !recordings.isEmpty()) {
            executorService.submit(() -> {
                DeviationAnalysis deviationAnalysis = new DeviationAnalysis();

                if (!recordings.isEmpty()) {
                    RecordTranscript recordTranscript = new RecordTranscript(credentialsProvider);
                    recordTranscript.process(recordings, deviationAnalysis);
                }

                if (!photos.isEmpty()) {
                    VertexAIImageDescriptionGenerator vertexAIImageDescriptionGenerator = new VertexAIImageDescriptionGenerator(credentialsProvider);
                    vertexAIImageDescriptionGenerator.generateImageDescriptions(photos, deviationAnalysis);

                    String prompt = deviationAnalysis.solutionPrompt(deviation);
                    String text;
                    try {
                        logger.debug("Generating text from Vertex AI using prompt");
                        text = new VertexAITextGenerator(credentialsProvider).generateText(prompt);
                        logger.debug("Successfully generated text from Vertex AI, response length: {}", text.length());
                    } catch (IOException e) {
                        logger.error("Failed to generate text from Vertex AI: {}", e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                    Solution solution;
                    try {
                        logger.debug("Attempting to parse solution JSON from Vertex AI response");
                        solution = objectMapper.readValue(text.replace("```json", "").replace("```", ""), Solution.class);
                        logger.debug("Successfully parsed solution JSON: issueType={}, priorityLevel={}, repairEffortHours={}",
                            solution.getIssueType(), solution.getPriorityLevel(), solution.getRepairEffortHours());
                    } catch (JsonProcessingException e) {
                        logger.error("Failed to parse solution JSON from Vertex AI response: {}", e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                    deviationAnalysis.setSolution(solution);
                }

                String da;
                try {
                    logger.debug("Serializing deviation analysis to JSON");
                    da = objectMapper.writeValueAsString(deviationAnalysis);
                    logger.debug("Successfully serialized deviation analysis, JSON length: {}", da.length());
                } catch (JsonProcessingException e) {
                    logger.error("Failed to serialize deviation analysis: {}", e.getMessage(), e);
                    throw new RuntimeException(e);
                }
                deviationStorage.save(da, deviationId + DeviationAnalysis.ID_SUFFIX);
            });
        }

        return deviation;
    }

    @GetMapping
    public List<Deviation> getAll() {
        logger.info("Retrieving all deviations");
        List<String> deviationIds = deviationStorage.ids();
        logger.debug("Found {} deviation IDs in storage", deviationIds.size());

        List<Deviation> deviations = new ArrayList<>();
        for (String id : deviationIds) {
            // Skip analysis entries
            if (id.endsWith(DeviationAnalysis.ID_SUFFIX)) {
                logger.debug("Skipping analysis entry with ID: {}", id);
                continue;
            }

            logger.debug("Processing deviation ID: {}", id);
            String content = deviationStorage.get(id);
            if (content == null) {
                logger.warn("No content found for deviation ID: {}", id);
                continue;
            }
            try {
                Deviation deviation = objectMapper.readValue(content, Deviation.class);
                deviations.add(deviation);
                logger.debug("Successfully parsed deviation with ID: {}", id);
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse deviation: {}", id, e);
                // Continue processing other deviations instead of failing the entire request
            }
        }
        logger.info("Successfully retrieved {} deviations", deviations.size());
        return deviations;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deviation> get(@PathVariable("id") String deviationId) throws JsonProcessingException {
        logger.info("Retrieving deviation with ID: {}", deviationId);
        String content = deviationStorage.get(deviationId);
        if (content == null) {
            logger.warn("Deviation not found with ID: {}", deviationId);
            return ResponseEntity.notFound().build();
        }
        Deviation deviation = objectMapper.readValue(content, Deviation.class);
        logger.info("Successfully retrieved deviation with ID: {}", deviationId);
        return ResponseEntity.ok(deviation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String deviationId) throws JsonProcessingException {
        logger.info("Deleting deviation with ID: {}", deviationId);
        // First get the deviation to get the associated images and recordings
        String content = deviationStorage.get(deviationId);
        if (content == null) {
            logger.warn("Deviation not found with ID: {}", deviationId);
            return ResponseEntity.notFound().build();
        }

        Deviation deviation = objectMapper.readValue(content, Deviation.class);

        // Delete all associated images
        for (String imageId : deviation.images()) {
            photosStorage.delete(imageId);
        }

        // Delete all associated recordings
        for (String recordingId : deviation.recordings()) {
            recordingStorage.delete(recordingId);
        }

        // Delete the analysis data
        String analysisId = deviationId + DeviationAnalysis.ID_SUFFIX;
        deviationStorage.delete(analysisId);
        logger.debug("Deleted analysis data with ID: {}", analysisId);

        // Finally delete the deviation itself
        deviationStorage.delete(deviationId);
        logger.info("Successfully deleted deviation with ID: {}", deviationId);

        return ResponseEntity.ok().build();
    }
}

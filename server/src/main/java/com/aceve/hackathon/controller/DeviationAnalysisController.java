package com.aceve.hackathon.controller;

import com.aceve.hackathon.payload.DeviationAnalysis;
import com.aceve.hackathon.repository.DataStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deviations/{id}/analysis")
public class DeviationAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(DeviationAnalysisController.class);
    private final DataStore deviationStore;
    private final ObjectMapper objectMapper;

    public DeviationAnalysisController(@Qualifier("deviation") DataStore deviationStore, ObjectMapper objectMapper) {
        this.deviationStore = deviationStore;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public DeviationAnalysis get(@PathVariable String id) {
        logger.info("Fetching deviation analysis for id: {}", id);
        String analysisPayload = deviationStore.get(id + DeviationAnalysis.ID_SUFFIX);
        if (analysisPayload == null) {
            logger.warn("No deviation analysis found for id: {}", id);
            return null;
        }
        try {
            logger.debug("Attempting to parse deviation analysis JSON");
            DeviationAnalysis analysis = objectMapper.readValue(analysisPayload, DeviationAnalysis.class);
            logger.debug("Successfully retrieved and parsed deviation analysis for id: {}", id);
            return analysis;
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse deviation analysis for id: {}", id, e);
            throw new RuntimeException(e);
        }
    }
}

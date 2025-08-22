package com.lk.photopredict.controller;

import com.lk.photopredict.analysis.RecordTranscript;
import com.lk.photopredict.repository.DataStore;
import com.google.api.gax.core.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records")
public class RecordsController {
    private static final Logger logger = LoggerFactory.getLogger(RecordsController.class);
    private final DataStore recordingDataStore;

    private final CredentialsProvider credentialsProvider;

    public RecordsController(@Qualifier("recording") DataStore recordingDataStore, CredentialsProvider credentialsProvider) {
        this.recordingDataStore = recordingDataStore;
        this.credentialsProvider = credentialsProvider;
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<ByteArrayResource> getRecord(@PathVariable String recordId) {
        logger.info("Retrieving record with ID: {}", recordId);
        byte[] recordData = recordingDataStore.getBytes(recordId);
        if (recordData == null) {
            logger.warn("Record not found with ID: {}", recordId);
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(recordData);
        logger.info("Successfully retrieved record with ID: {}", recordId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recordId + "\"")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .contentLength(recordData.length)
                .body(resource);
    }

    @GetMapping("/{recordId}/transcription")
    void transcription(@PathVariable("recordId") String recordId) {
        new RecordTranscript(credentialsProvider).process(recordId);
    }
}

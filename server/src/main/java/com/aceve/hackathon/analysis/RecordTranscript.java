package com.aceve.hackathon.analysis;

import com.aceve.hackathon.payload.DeviationAnalysis;
import com.aceve.hackathon.repository.Bucket;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.speech.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class RecordTranscript {

    private final Logger logger = LoggerFactory.getLogger(RecordTranscript.class);

    private final CredentialsProvider credentialsProvider;

    public RecordTranscript(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public String process(String id) {
        logger.info("Starting transcription for record ID: {}", id);
        // Instantiates a client

        SpeechSettings build;
        try {
            build = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
        } catch (IOException e) {
            logger.error("Failed to build speech settings", e);
            throw new RuntimeException(e);
        }

        SpeechClient speechClient;
        try {
            speechClient = SpeechClient.create(build);
        } catch (IOException e) {
            logger.error("Failed to create speech client", e);
            throw new RuntimeException(e);
        }

        // The path to the audio file to transcribe
        String gcsUri = Bucket.Name.Recording.gcsPath(id);
        logger.debug("Using GCS URI: {}", gcsUri);

        // Builds the sync recognize request
        RecognitionConfig config =
                RecognitionConfig.newBuilder()
                        .setLanguageCode("en-US")
                        .build();
        RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

        String transcript = "";
        try {
            // Performs speech recognition on the audio file
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().getFirst();
                transcript += alternative.getTranscript();
                logger.info("Transcription for record {}: {}", id, transcript);
            }
        } catch (Exception e) {
            logger.error("Failed to transcribe record: {}", id, e);
            throw new RuntimeException("Failed to transcribe record: " + id, e);
        }
        return transcript;
    }

    public void process(List<String> recordings, DeviationAnalysis deviationAnalysis) {
        for(String rId : recordings) {
            String transcription = this.process(rId);
            deviationAnalysis.addTranscription(rId, transcription);
        }
    }
}

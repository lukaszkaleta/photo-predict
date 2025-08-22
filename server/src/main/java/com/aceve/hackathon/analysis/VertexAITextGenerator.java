package com.aceve.hackathon.analysis;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.aceve.hackathon.HackathonApplication.LOCATION;
import static com.aceve.hackathon.HackathonApplication.PROJECT_ID;

public class VertexAITextGenerator {
    private static final Logger logger = LoggerFactory.getLogger(VertexAITextGenerator.class);
    private static final String MODEL_NAME = "gemini-2.0-flash-001";

    private final CredentialsProvider credentialsProvider;

    public VertexAITextGenerator(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    /**
     * Generates text response using Vertex AI Gemini model.
     *
     * @param prompt The text prompt to send to the model
     * @return The generated text response
     * @throws IOException If there is an error calling the API
     */
    public String generateText(String prompt) throws IOException {
        logger.debug("Generating text response for prompt: {}", prompt);

        logger.debug("Initializing VertexAI with project: {} and location: {}", PROJECT_ID, LOCATION);
        VertexAI.Builder builder = new VertexAI.Builder()
                .setCredentials(this.credentialsProvider.getCredentials())
                .setProjectId(PROJECT_ID)
                .setLocation(LOCATION);
        try (VertexAI vertexAI = builder.build()) {
            logger.debug("VertexAI client built successfully");
            // Create a generative model instance
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAI);
            logger.debug("Generative model created with name: {}", MODEL_NAME);

            // Create content with text prompt
            Content content = ContentMaker.fromString(prompt);

            // Generate content from the model
            logger.debug("Sending request to VertexAI");
            GenerateContentResponse response = model.generateContent(content);
            logger.debug("Received response from VertexAI");

            // Parse and return the response
            return ResponseHandler.getText(response);
        }
    }
}
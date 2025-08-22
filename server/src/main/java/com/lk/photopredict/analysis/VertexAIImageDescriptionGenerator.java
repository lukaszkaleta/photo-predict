package com.lk.photopredict.analysis;

import com.lk.photopredict.payload.DeviationAnalysis;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.aiplatform.v1.Model;
import com.google.cloud.aiplatform.v1.ModelServiceClient;
import com.google.cloud.aiplatform.v1.ModelServiceSettings;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import static com.lk.photopredict.HackathonApplication.LOCATION;
import static com.lk.photopredict.HackathonApplication.PROJECT_ID;

public class VertexAIImageDescriptionGenerator {
    private static final Logger logger = LoggerFactory.getLogger(VertexAIImageDescriptionGenerator.class);

    private static final String MODEL_NAME = "gemini-2.0-flash-001"; // Added version number

    private final CredentialsProvider credentialsProvider;

    public static String LONG_PROMPT = """
                Analyze the provided photo of the electrical installation and identify potential issues. Review the following aspects and provide suggestions for improvement or further investigation:
                1. Wiring and Insulation: Look for signs of wear, fraying, or exposed wires. Are all wires properly insulated and secured? Suggest any repairs if necessary.
                2. Circuit Breakers and Fuses: Check the positioning, labeling, and condition of the circuit breakers. Are they functioning correctly? Suggest actions if any breaker's condition is questionable.
                3. Conduit and Cable Routing: Are cables routed through proper conduits, and are they free from sharp bends or unsafe exposure? Suggest any necessary adjustments.
                4. Grounding: Verify the grounding of the system. Is the installation properly grounded? If not, suggest steps to correct the grounding.
                5. Overloaded Outlets or Junction Boxes: Are there any signs of overloaded circuits, overheating, or discoloration around outlets or junction boxes? Recommend necessary fixes.
                6. Code Compliance: Is the installation in line with standard electrical codes (local or national)? Identify areas where it may be out of compliance and provide recommendations for updates.
                7. Connection Integrity: Are there any loose or improperly connected terminals? Look for arc marks or signs of heat damage. Recommend any necessary reconnections or replacements.
                8. General Condition and Environmental Damage: Is there any rust, corrosion, or environmental wear present that could affect performance? Suggest remediation steps if needed.
                9. Maintenance Access and Ventilation: Does the installation have sufficient space for easy maintenance and adequate ventilation, especially near heat-producing equipment? Provide recommendations for reorganization if necessary.
                Please provide a summary with specific suggestions for any repairs, further review, or potential hazards identified.""";

    public VertexAIImageDescriptionGenerator(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public void listAvailableModels() throws IOException {
        logger.debug("Listing available models in project: {} and location: {}", PROJECT_ID, LOCATION);
        ModelServiceSettings settings = ModelServiceSettings.newBuilder()
                .setCredentialsProvider(credentialsProvider)
                .setEndpoint(LOCATION + "-aiplatform.googleapis.com:443")
                .build();

        try (ModelServiceClient client = ModelServiceClient.create(settings)) {
            String parent = String.format("projects/%s/locations/%s", PROJECT_ID, LOCATION);
            for (Model model : client.listModels(parent).iterateAll()) {
                logger.info("Found model: {} (Display name: {})", model.getName(), model.getDisplayName());
            }
        }
    }

    /**
     * Generates a detailed description of an image using Vertex AI Gemini model.
     *
     * @param imagePath The path to the local image file
     * @return A detailed description of the image content
     * @throws IOException If there is an error reading the file or calling the API
     */
    public String generateImageDescriptionFromLocal(String imagePath) throws IOException {
        logger.debug("Generating image description from local file: {}", imagePath);
        // Read image file and encode to base64
        Path path = Paths.get(imagePath);
        byte[] imageBytes = Files.readAllBytes(path);
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        logger.debug("Initializing VertexAI with project: {} and location: {}", PROJECT_ID, LOCATION);
        VertexAI.Builder builder = new VertexAI.Builder()
                .setCredentials(credentialsProvider.getCredentials())
                .setProjectId(PROJECT_ID)
                .setLocation(LOCATION);
        try (VertexAI vertexAI = builder.build()) {
            logger.debug("VertexAI client built successfully");
            // Create a generative model instance
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAI);
            logger.debug("Generative model created with name: {}", MODEL_NAME);

            // Create content with text prompt and image
            Content content = ContentMaker.fromMultiModalData(
                    "Please provide a detailed description of this image. Include what you see, the setting, any people or objects present, colors, atmosphere, and any other notable elements. Be descriptive but concise.",
                    createImagePartFromBase64(base64Image, imagePath)
            );

            // Generate content from the model
            logger.debug("Sending request to VertexAI");
            GenerateContentResponse response = model.generateContent(content);
            logger.debug("Received response from VertexAI");

            // Parse and return the response
            return ResponseHandler.getText(response);
        }
    }

    /**
     * Generates a detailed description of an image from Google Cloud Storage using Vertex AI Gemini model.
     *
     * @param imageId The id of the image (format: gs://bucket-name/path/to/image.jpg)
     * @return A detailed description of the image content
     * @throws IOException If there is an error calling the API
     */
    public String generateImageDescriptionFromGcs(String imageId, String prompt) throws IOException {
        logger.debug("Generating image description from GCS: {}", imageId);
        String gcsUri = "gs://h2025-images/" + imageId;
        logger.debug("Using GCS URI: {}", gcsUri);

        logger.debug("Initializing VertexAI with project: {} and location: {}", PROJECT_ID, LOCATION);
        VertexAI.Builder builder = new VertexAI.Builder()
                .setCredentials(credentialsProvider.getCredentials())
                .setProjectId(PROJECT_ID)
                .setLocation(LOCATION);
        try (VertexAI vertexAI = builder.build()) {
            logger.debug("VertexAI client built successfully");
            // Create a generative model instance
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAI);
            logger.debug("Generative model created with name: {}", MODEL_NAME);

            // Create content with text prompt and GCS image reference
            String defaultPrompt = "Please provide a detailed description of this image. Include what you see, the setting, any people or objects present, colors, atmosphere, and any other notable elements. Be descriptive but concise.";
            Content content = ContentMaker.fromMultiModalData(prompt.isEmpty() ?
                            defaultPrompt : prompt,
                    createImagePartFromGcs(gcsUri)
            );

            // Generate content from the model
            logger.debug("Sending request to VertexAI");
            GenerateContentResponse response = model.generateContent(content);
            logger.debug("Received response from VertexAI");

            // Parse and return the response
            return ResponseHandler.getText(response);
        }
    }

    /**
     * Creates an image part from base64 data for the Vertex AI request
     *
     * @param base64Image The base64-encoded image data
     * @param imagePath   The path to the image file for MIME type detection
     * @return A Part object containing the image data
     */
    private static Part createImagePartFromBase64(String base64Image, String imagePath) {
        // Determine MIME type based on file extension
        String mimeType = getMimeType(imagePath);

        // Create the part with the image data
        return Part.newBuilder()
                .setInlineData(
                        com.google.cloud.vertexai.api.Blob.newBuilder()
                                .setMimeType(mimeType)
                                .setData(com.google.protobuf.ByteString.copyFrom(
                                        Base64.getDecoder().decode(base64Image)))
                                .build())
                .build();
    }

    /**
     * Creates an image part from a GCS URI for the Vertex AI request
     *
     * @param gcsUri The GCS URI of the image
     * @return A Part object referencing the GCS image
     */
    private static Part createImagePartFromGcs(String gcsUri) {
        // Create a FileData object with the GCS URI
        com.google.cloud.vertexai.api.FileData fileData = com.google.cloud.vertexai.api.FileData.newBuilder()
                .setFileUri(gcsUri)
                .setMimeType(getMimeType(gcsUri))
                .build();

        // Create the part with the file reference
        return Part.newBuilder()
                .setFileData(fileData)
                .build();
    }

    /**
     * Determines the MIME type based on the file extension
     *
     * @param filePath The path to the file
     * @return The MIME type as a string
     */
    private static String getMimeType(String filePath) {
        String lowercasePath = filePath.toLowerCase();
        if (lowercasePath.endsWith(".jpg") || lowercasePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercasePath.endsWith(".png")) {
            return "image/png";
        } else if (lowercasePath.endsWith(".gif")) {
            return "image/gif";
        } else if (lowercasePath.endsWith(".webp")) {
            return "image/webp";
        } else if (lowercasePath.endsWith(".bmp")) {
            return "image/bmp";
        } else {
            // Default to jpeg if unable to determine
            return "image/jpeg";
        }
    }

    public void generateImageDescriptions(List<String> images, DeviationAnalysis deviationAnalysis) {
        for(String image : images) {
            String decription;
            try {
                decription = this.generateImageDescriptionFromGcs(image, LONG_PROMPT);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            deviationAnalysis.addImageDescription(image, decription);
        }
    }
}

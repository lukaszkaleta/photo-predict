package com.aceve.hackathon.analysis;

import com.aceve.hackathon.HackathonApplication;
import com.aceve.hackathon.analysis.VertexAIImageDescriptionGenerator;
import com.google.api.gax.core.CredentialsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;


@SpringBootTest
class VertexAIImageDescriptionGeneratorTest {


    @Autowired private CredentialsProvider credentialsProvider;

    @Test
    void listAvailableModels() throws IOException {
        VertexAIImageDescriptionGenerator vertexAIImageDescriptionGenerator = new VertexAIImageDescriptionGenerator(credentialsProvider);
        vertexAIImageDescriptionGenerator.listAvailableModels();
    }

    @Test
    void generateImageDescriptionFromGcs() throws IOException {
        VertexAIImageDescriptionGenerator vertexAIImageDescriptionGenerator = new VertexAIImageDescriptionGenerator(credentialsProvider);
//        String id = "db96297f-2d4b-4228-9522-587fa44c6f58"; // measuring tape
        //        The image shows a close-up of a small, teal-colored measuring tape resting on a light brown wooden surface. The tape measure has a green label with yellow lettering that reads "2M/13mm SUPER QUALITY MALAYSIA." Above the text, there is a graphic with the word "MECSON" inscribed. A black plastic loop extends from the side of the tape, and a red piece is also visible. The lighting is moderate, and the camera angle is slightly above, providing a clear view of the object and its immediate surroundings.
        String id = "Circuit-Breakers-Panel.jpg";
        String shortPrompt = "Analyze the photo of the electrical installation. Check for wiring issues, circuit breakers, grounding, code compliance, overloads, connection integrity, environmental damage, and general maintenance. Provide suggestions for repairs or further investigation.";

        String prompt = VertexAIImageDescriptionGenerator.LONG_PROMPT;
        System.out.println(vertexAIImageDescriptionGenerator.generateImageDescriptionFromGcs(id, shortPrompt));
        System.out.println(vertexAIImageDescriptionGenerator.generateImageDescriptionFromGcs(id, prompt));
    }
}
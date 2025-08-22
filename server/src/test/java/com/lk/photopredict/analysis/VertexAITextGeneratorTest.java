package com.lk.photopredict.analysis;

import com.lk.photopredict.payload.Solution;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class VertexAITextGeneratorTest {

    @Autowired
    private  CredentialsProvider credentialsProvider;
    @Test
    void generateTextTest() throws IOException {
        VertexAITextGenerator vertexAITextGenerator = new VertexAITextGenerator(credentialsProvider);
//        Here, the results of voice-to-text conversion, comments, and image descriptors can be mixed to produce the final result.
        String prompt = """
                Expert description based on test reports: The customer complains of periodic problems with the electrical installation. From time to time, the circuit breakers trip in an uncontrolled manner.
                Automated Inspection Recommendations based on attached photo:
                 Professional Inspection: A qualified electrician should inspect the panel.
                 Rewiring and Organization: The electrician should rewire the panel, properly routing and securing the wires. Using wire ties or appropriate fasteners is important.
                 Confirm Conductor Sizes: The electrician needs to confirm that all conductors are properly sized for the corresponding breaker amperage.
                 Verify Grounding: Confirm that the grounding system is properly installed and connected.
                 Torque Connections: Check the tightness of all connections and torque them to the manufacturer's specifications.
                 Labeling: Create a clear and accurate circuit directory for the panel.
                 Circuit Audit: Have the electrician perform a load calculation to ensure that the panel is not overloaded.
                 Update Components: Replace any outdated or incompatible circuit breakers.
                 Safety Assessment: The electrician should perform a general safety assessment of the entire electrical system.
                 In summary, while the panel might be currently functioning, the disorganized wiring poses safety risks and should be addressed by a qualified electrician.
                Use the following JSON template structure and prepare the json object. Field values should be based on the text above. Predict the time in hours for repairEffortHours.
                {
                    "issueType": "",
                    "summary": "",
                    "priorityLevel": "",
                    "repairEffortHours": ""
               }
                """;
        String s = vertexAITextGenerator.generateText(prompt);

        Solution solution = null;
        try {
            solution = new ObjectMapper().readValue(s, Solution.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        System.out.println(s);
        //{
        //  "issueType": "Electrical Wiring",
        //  "summary": "Periodic circuit breaker trips and disorganized electrical panel wiring posing safety risks. Requires inspection, rewiring, and potential component updates.",
        //  "priorityLevel": "High",
        //  "repairEffortHours": "8"
        //}
        //{
        //  "issueType": "Electrical Fault/Safety Hazard",
        //  "summary": "Periodic circuit breaker tripping and disorganized electrical panel wiring indicate potential safety hazards. A qualified electrician is needed to inspect, rewire, and update the panel to address these issues.",
        //  "priorityLevel": "High",
        //  "repairEffortHours": "8"
        //}
        //{
        //  "issueType": "Electrical Installation Fault",
        //  "summary": "Periodic circuit breaker tripping and disorganized electrical panel wiring requiring professional inspection and remediation.",
        //  "priorityLevel": "High",
        //  "repairEffortHours": "8"
        //}
    }
}
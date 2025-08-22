package com.lk.photopredict.payload;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeviationAnalysis {

    public static final String ID_SUFFIX = "-analysis";
    private final Map<String, String> transcriptions = new LinkedHashMap<>();
    private final Map<String, String> images = new LinkedHashMap<>();
    private Solution solution;

    public Map<String, String> getTranscriptions() {
        return transcriptions;
    }

    public Map<String, String> getImages() {
        return images;
    }

    public Solution getSolution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    public void addTranscription(String rId, String transcription) {
        this.transcriptions.put(rId, transcription);
    }

    public void addImageDescription(String image, String description) {
        images.put(image, description);
    }

    public String solutionPrompt(Deviation deviation) {
        String expertCommentsPrefixPrompt = "Expert description based on test reports: ";
        String imageToTextPrefixPrompt = "Automated Inspection Recommendations based on attached photo: ";
        String resultPrompt = """
                Use the following JSON template structure and prepare the json object. Field values should be based on the text above. Predict the time in hours for repairEffortHours.
                Resulted string will be mapped to Java object with fields: issueType, summary, priorityLevel, repairEffortHours, checkList.
                The checklist should include a list of necessary operations.
                Provide the raw JSON without markdown formatting.
                {
                    "issueType": "",
                    "summary": "",
                    "priorityLevel": "",
                    "repairEffortHours": "",
                    "checkList": [""]
                }
                """;
        String priority1 = deviation.comment();
        String priority2 = transcriptionPrompt();
        String priority3 = imageDescriptionPrompt();
        return Stream.of(expertCommentsPrefixPrompt, priority1, priority2, imageToTextPrefixPrompt, priority3, resultPrompt).filter(el -> !el.isEmpty()).collect(Collectors.joining());
    }

    private String imageDescriptionPrompt() {
        return promptFromMapValue(images);
    }

    private String transcriptionPrompt() {
        return promptFromMapValue(transcriptions);
    }

    private String promptFromMapValue(Map<String, String> map) {
        return Stream.concat(transcriptions.values().stream(), map.values().stream()).filter(el -> !el.isEmpty())
                .collect(Collectors.joining("\n"));
    }
}

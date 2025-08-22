package com.aceve.hackathon.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Solution {
    @JsonProperty("issueType")
    private String issueType;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("priorityLevel")
    private String priorityLevel;
    
    @JsonProperty("repairEffortHours")
    private String repairEffortHours;
    
    @JsonProperty("checkList")
    private List<String> checkList;

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getRepairEffortHours() {
        return repairEffortHours;
    }

    public void setRepairEffortHours(String repairEffortHours) {
        this.repairEffortHours = repairEffortHours;
    }

    public List<String> getCheckList() {
        return checkList;
    }

    public void setCheckList(List<String> checkList) {
        this.checkList = checkList;
    }
}

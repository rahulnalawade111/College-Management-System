package com.college.sms.dto;

import jakarta.validation.constraints.Size;

public class SubmissionRequest {

    @Size(max = 5000, message = "Submission text must be under 5000 characters")
    private String submissionText;

    @Size(max = 500)
    private String fileUrl;

    public String getSubmissionText() { return submissionText; }
    public void setSubmissionText(String submissionText) { this.submissionText = submissionText; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}

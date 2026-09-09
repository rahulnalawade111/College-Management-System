package com.college.sms.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class GradeRequest {

    @NotNull(message = "Marks are required")
    @DecimalMin(value = "0", message = "Marks cannot be negative")
    private BigDecimal marksObtained;

    @Size(max = 1000)
    private String feedback;

    public BigDecimal getMarksObtained() { return marksObtained; }
    public void setMarksObtained(BigDecimal marksObtained) { this.marksObtained = marksObtained; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}

package com.college.sms.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 10-point grading scale (see .drytis/schema.md):
 *   ≥90 O/10, ≥80 A/8, ≥70 B/7, ≥60 C/6, ≥50 D/5, ≥45 P/4, else F/0 (fail).
 * SGPA = Σ(grade_point × credits) / Σ(credits), 0-credit subjects excluded.
 */
@Service
public class AcademicCalculationService {

    public record GradeResult(String grade, BigDecimal gradePoint) {}

    public GradeResult computeGrade(BigDecimal percent) {
        if (percent == null) {
            return new GradeResult("F", BigDecimal.ZERO);
        }
        double p = percent.doubleValue();
        if (p >= 90) return new GradeResult("O", BigDecimal.TEN);
        if (p >= 80) return new GradeResult("A", new BigDecimal("8.0"));
        if (p >= 70) return new GradeResult("B", new BigDecimal("7.0"));
        if (p >= 60) return new GradeResult("C", new BigDecimal("6.0"));
        if (p >= 50) return new GradeResult("D", new BigDecimal("5.0"));
        if (p >= 45) return new GradeResult("P", new BigDecimal("4.0"));
        return new GradeResult("F", BigDecimal.ZERO);
    }

    public BigDecimal computePercent(BigDecimal total, int maxMarks) {
        if (total == null || maxMarks <= 0) {
            return BigDecimal.ZERO;
        }
        return total.multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(maxMarks), 1, RoundingMode.HALF_UP);
    }

    public BigDecimal computeSgpa(BigDecimal weightedPoints, int totalCredits) {
        if (totalCredits <= 0 || weightedPoints == null) {
            return BigDecimal.ZERO;
        }
        return weightedPoints.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);
    }
}

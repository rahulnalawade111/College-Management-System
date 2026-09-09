package com.college.sms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "results", uniqueConstraints = @UniqueConstraint(
        name = "uk_result_unique",
        columnNames = {"exam_id", "student_id", "subject_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "internal_marks", precision = 5, scale = 2)
    private BigDecimal internalMarks;

    @Column(name = "external_marks", precision = 5, scale = 2)
    private BigDecimal externalMarks;

    @Column(name = "practical_marks", precision = 5, scale = 2)
    private BigDecimal practicalMarks;

    @Column(name = "total_marks", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalMarks;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;

    @Column(nullable = false, length = 3)
    private String grade;

    @Column(name = "grade_point", nullable = false, precision = 3, scale = 1)
    private BigDecimal gradePoint;

    @Column(nullable = false)
    private Integer credits;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

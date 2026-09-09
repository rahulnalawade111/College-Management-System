package com.college.sms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "faculty_id")
    private Long facultyId;

    @Column(nullable = false, length = 150)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /** HOMEWORK | LAB | PROJECT | REPORT */
    @Column(name = "assignment_type", nullable = false, length = 20)
    private String assignmentType;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    /** ACTIVE | CLOSED */
    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.assignmentType == null) {
            this.assignmentType = "HOMEWORK";
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

package com.college.sms.repository;

import com.college.sms.entity.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    List<ExamSchedule> findByExamIdOrderByExamDateAscStartTimeAsc(Long examId);

    boolean existsByExamIdAndSubjectId(Long examId, Long subjectId);

    boolean existsByExamId(Long examId);

    boolean existsBySubjectId(Long subjectId);

    /** A subject cannot have two exams at overlapping date+time slots. */
    @Query("select count(s) from ExamSchedule s where s.subject.id = :subjectId " +
            "and s.examDate = :date and s.startTime < :endTime and s.endTime > :startTime " +
            "and s.id <> coalesce(:excludeId, -1)")
    long countOverlaps(@Param("subjectId") Long subjectId,
                       @Param("date") java.time.LocalDate date,
                       @Param("startTime") java.time.LocalTime startTime,
                       @Param("endTime") java.time.LocalTime endTime,
                       @Param("excludeId") Long excludeId);
}

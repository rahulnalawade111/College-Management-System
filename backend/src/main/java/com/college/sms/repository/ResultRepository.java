package com.college.sms.repository;

import com.college.sms.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    List<Result> findByExamIdAndStudentIdOrderBySubjectIdAsc(Long examId, Long studentId);

    List<Result> findByExamId(Long examId);

    Optional<Result> findByExamIdAndStudentIdAndSubjectId(Long examId, Long studentId, Long subjectId);

    boolean existsByExamIdAndStudentIdAndSubjectId(Long examId, Long studentId, Long subjectId);

    boolean existsByExamId(Long examId);

    /** SGPA for one student+exam: sum(grade_point * credits) / sum(credits), 0-credit subjects excluded. */
    @Query("select coalesce(sum(r.gradePoint * r.credits), 0), coalesce(sum(r.credits), 0) " +
            "from Result r where r.student.id = :studentId and r.exam.id = :examId and r.credits > 0")
    List<Object[]> sgpaComponents(@Param("studentId") Long studentId, @Param("examId") Long examId);

    /** CGPA across all published results of the student. */
    @Query("select coalesce(sum(r.gradePoint * r.credits), 0), coalesce(sum(r.credits), 0) " +
            "from Result r where r.student.id = :studentId and r.credits > 0 and r.published = true")
    List<Object[]> cgpaComponents(@Param("studentId") Long studentId);
}

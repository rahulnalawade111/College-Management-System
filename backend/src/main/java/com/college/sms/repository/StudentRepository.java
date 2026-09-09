package com.college.sms.repository;

import com.college.sms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    @Query("select s from Student s " +
            "join fetch s.course join fetch s.department join fetch s.semester join fetch s.academicYear " +
            "where s.id = :id")
    Optional<Student> findByIdWithReferences(@Param("id") Long id);

    Optional<Student> findByStudentIdIgnoreCase(String studentId);

    Optional<Student> findByEmailIgnoreCase(String email);

    /** Login-linked student: users.student_id points at the students row. */
    @Query("select s from Student s where s.id = (select u.studentId from User u where u.id = :userId)")
    Optional<Student> findByUserId(@Param("userId") Long userId);

    boolean existsByStudentIdIgnoreCase(String studentId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByStudentIdIgnoreCaseAndIdNot(String studentId, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    long countByAdmissionDateBetween(LocalDate from, LocalDate to);

    List<Student> findTop3ByAdmissionDateGreaterThanEqualOrderByAdmissionDateDesc(LocalDate date);
}

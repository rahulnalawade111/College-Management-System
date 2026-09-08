package com.college.sms.repository;

import com.college.sms.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);

    List<Attendance> findBySubjectIdAndAttendanceDate(Long subjectId, LocalDate date);

    boolean existsByStudentIdAndSubjectIdAndAttendanceDate(Long studentId, Long subjectId, LocalDate date);

    boolean existsBySubjectId(Long subjectId);

    boolean existsByStudentId(Long studentId);

    @Query("select a.subject.id, a.subject.subjectName, a.subject.subjectCode, count(a), " +
            "sum(case when a.status = 'PRESENT' then 1 else 0 end), " +
            "sum(case when a.status = 'LATE' then 1 else 0 end), " +
            "sum(case when a.status = 'ABSENT' then 1 else 0 end), " +
            "sum(case when a.status = 'EXCUSED' then 1 else 0 end) " +
            "from Attendance a where a.student.id = :studentId group by a.subject.id, a.subject.subjectName, a.subject.subjectCode")
    List<Object[]> subjectSummaryByStudentId(@Param("studentId") Long studentId);

    @Query("select count(a), sum(case when a.status = 'PRESENT' then 1 else 0 end) " +
            "from Attendance a where a.student.id = :studentId")
    Object[] overallByStudentId(@Param("studentId") Long studentId);

    @Query("select a.attendanceDate, " +
            "sum(case when a.status = 'PRESENT' then 1 else 0 end), " +
            "sum(case when a.status = 'ABSENT' then 1 else 0 end), " +
            "sum(case when a.status = 'LATE' then 1 else 0 end), " +
            "sum(case when a.status = 'EXCUSED' then 1 else 0 end), count(a) " +
            "from Attendance a where a.attendanceDate >= :since group by a.attendanceDate order by a.attendanceDate")
    List<Object[]> dailyCountsSince(@Param("since") LocalDate since);

    @Query("select count(distinct a.student.id) from Attendance a where a.attendanceDate = :date")
    long countDistinctStudentsOnDate(@Param("date") LocalDate date);
}

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

    @Query("select count(a), coalesce(sum(case when a.status in ('PRESENT','LATE') then 1 else 0 end), 0) " +
            "from Attendance a where a.student.id = :studentId")
    List<Object[]> overallByStudentId(@Param("studentId") Long studentId);

    @SuppressWarnings("unused")
    default Object[] overallByStudentIdRow(Long studentId) {
        List<Object[]> rows = overallByStudentId(studentId);
        return rows.isEmpty() ? new Object[]{0L, 0L} : rows.get(0);
    }

    @Query("select a.attendanceDate, " +
            "sum(case when a.status = 'PRESENT' then 1 else 0 end), " +
            "sum(case when a.status = 'ABSENT' then 1 else 0 end), " +
            "sum(case when a.status = 'LATE' then 1 else 0 end), " +
            "sum(case when a.status = 'EXCUSED' then 1 else 0 end), count(a) " +
            "from Attendance a where a.attendanceDate >= :since group by a.attendanceDate order by a.attendanceDate")
    List<Object[]> dailyCountsSince(@Param("since") LocalDate since);

    @Query("select count(distinct a.student.id) from Attendance a where a.attendanceDate = :date")
    long countDistinctStudentsOnDate(@Param("date") LocalDate date);

    // ------------------------------------------------------------------
    // Dashboard aggregates
    // ------------------------------------------------------------------

    List<Attendance> findTop5ByOrderByCreatedAtDesc();

    List<Attendance> findTop5ByStudentIdOrderByCreatedAtDesc(Long studentId);

    /** Overall attendance % across the subjects taught by one faculty (PRESENT+LATE / total). */
    @Query("select coalesce(sum(case when a.status in ('PRESENT','LATE') then 1 else 0 end) * 100.0 / nullif(count(a), 0), 0) " +
            "from Attendance a where a.facultyId = :facultyId")
    double overallAttendancePercentByFaculty(@Param("facultyId") Long facultyId);

    /** Attendance % for one subject (PRESENT+LATE / total). */
    @Query("select coalesce(sum(case when a.status in ('PRESENT','LATE') then 1 else 0 end) * 100.0 / nullif(count(a), 0), 0) " +
            "from Attendance a where a.subject.id = :subjectId")
    double attendancePercentBySubject(@Param("subjectId") Long subjectId);

    @Query("select a.attendanceDate, " +
            "sum(case when a.status = 'PRESENT' then 1 else 0 end), " +
            "sum(case when a.status = 'ABSENT' then 1 else 0 end), " +
            "sum(case when a.status = 'LATE' then 1 else 0 end), " +
            "sum(case when a.status = 'EXCUSED' then 1 else 0 end), count(a) " +
            "from Attendance a where a.attendanceDate >= :since and a.facultyId = :facultyId " +
            "group by a.attendanceDate order by a.attendanceDate")
    List<Object[]> dailyCountsByFacultySince(@Param("facultyId") Long facultyId,
                                             @Param("since") LocalDate since);

    /** Students below a threshold % college-wide: id, code, first, last, course, pct. */
    @Query("select s.id, s.studentId, s.firstName, s.lastName, c.courseName, " +
            "sum(case when a.status in ('PRESENT','LATE') then 1 else 0 end) * 100.0 / count(a) " +
            "from Attendance a join a.student s join s.course c " +
            "group by s.id, s.studentId, s.firstName, s.lastName, c.courseName " +
            "having sum(case when a.status in ('PRESENT','LATE') then 1 else 0 end) * 100.0 / count(a) < :threshold " +
            "order by 6 asc")
    List<Object[]> lowAttendanceStudents(@Param("threshold") double threshold);

    /** Same but restricted to the subjects taught by one faculty. */
    @Query("select s.id, s.studentId, s.firstName, s.lastName, c.courseName, " +
            "sum(case when a.status in ('PRESENT','LATE') then 1 else 0 end) * 100.0 / count(a) " +
            "from Attendance a join a.student s join s.course c " +
            "where a.facultyId = :facultyId " +
            "group by s.id, s.studentId, s.firstName, s.lastName, c.courseName " +
            "having sum(case when a.status in ('PRESENT','LATE') then 1 else 0 end) * 100.0 / count(a) < :threshold " +
            "order by 6 asc")
    List<Object[]> lowAttendanceStudentsByFaculty(@Param("facultyId") Long facultyId,
                                                  @Param("threshold") double threshold);
}

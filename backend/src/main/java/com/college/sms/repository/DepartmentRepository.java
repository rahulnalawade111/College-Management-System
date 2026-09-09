package com.college.sms.repository;

import com.college.sms.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    boolean existsByDepartmentCodeIgnoreCase(String code);

    boolean existsByDepartmentNameIgnoreCase(String name);

    boolean existsByDepartmentCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByDepartmentNameIgnoreCaseAndIdNot(String name, Long id);

    /** Per-department student counts split by gender (for the enrollment chart). */
    @Query("select d.id, d.departmentName, count(s), " +
            "sum(case when s.gender = 'Male' then 1 else 0 end), " +
            "sum(case when s.gender = 'Female' then 1 else 0 end) " +
            "from Department d left join Student s on s.department.id = d.id " +
            "group by d.id, d.departmentName order by count(s) desc")
    List<Object[]> studentCountByDepartment();
}

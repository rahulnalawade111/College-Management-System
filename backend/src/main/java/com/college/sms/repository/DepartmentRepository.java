package com.college.sms.repository;

import com.college.sms.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByDepartmentCode(String departmentCode);

    boolean existsByDepartmentCodeIgnoreCase(String departmentCode);

    boolean existsByDepartmentNameIgnoreCase(String departmentName);

    boolean existsByDepartmentCodeIgnoreCaseAndIdNot(String departmentCode, Long id);

    boolean existsByDepartmentNameIgnoreCaseAndIdNot(String departmentName, Long id);
}

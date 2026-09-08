package com.college.sms.service;

import com.college.sms.dto.DepartmentRequest;
import com.college.sms.dto.DepartmentResponse;
import com.college.sms.entity.Department;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.CourseRepository;
import com.college.sms.repository.DepartmentRepository;
import com.college.sms.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return toResponse(getDepartment(id));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByDepartmentCodeIgnoreCase(request.departmentCode())) {
            throw new DuplicateResourceException("Department code already exists: " + request.departmentCode());
        }
        if (departmentRepository.existsByDepartmentNameIgnoreCase(request.departmentName())) {
            throw new DuplicateResourceException("Department name already exists: " + request.departmentName());
        }
        Department department = Department.builder()
                .departmentCode(request.departmentCode())
                .departmentName(request.departmentName())
                .description(request.description())
                .hodId(request.hodId())
                .build();
        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = getDepartment(id);
        if (departmentRepository.existsByDepartmentCodeIgnoreCaseAndIdNot(request.departmentCode(), id)) {
            throw new DuplicateResourceException("Department code already exists: " + request.departmentCode());
        }
        if (departmentRepository.existsByDepartmentNameIgnoreCaseAndIdNot(request.departmentName(), id)) {
            throw new DuplicateResourceException("Department name already exists: " + request.departmentName());
        }
        department.setDepartmentCode(request.departmentCode());
        department.setDepartmentName(request.departmentName());
        department.setDescription(request.description());
        department.setHodId(request.hodId());
        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public void delete(Long id) {
        Department department = getDepartment(id);
        if (courseRepository.existsByDepartmentId(id) || subjectRepository.existsByDepartmentId(id)) {
            throw new BadRequestException(
                    "Cannot delete department '" + department.getDepartmentName()
                            + "' — it is referenced by courses or subjects.");
        }
        departmentRepository.delete(department);
    }

    private Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + id));
    }

    private DepartmentResponse toResponse(Department d) {
        return new DepartmentResponse(d.getId(), d.getDepartmentCode(), d.getDepartmentName(),
                d.getDescription(), d.getHodId(), null);
    }
}

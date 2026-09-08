package com.college.sms.service;

import com.college.sms.dto.FacultyRequest;
import com.college.sms.dto.FacultyResponse;
import com.college.sms.entity.Department;
import com.college.sms.entity.Faculty;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.DepartmentRepository;
import com.college.sms.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public Page<FacultyResponse> findAll(String search, Long departmentId, String status, Pageable pageable) {
        Specification<Faculty> spec = Specification.where(null);
        if (search != null && !search.isBlank()) {
            String term = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), term),
                    cb.like(cb.lower(root.get("lastName")), term),
                    cb.like(cb.lower(root.get("employeeId")), term),
                    cb.like(cb.lower(root.get("email")), term)));
        }
        if (departmentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return facultyRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public FacultyResponse findById(Long id) {
        return toResponse(getFaculty(id));
    }

    @Transactional
    public FacultyResponse create(FacultyRequest request) {
        validateUnique(null, request.employeeId(), request.email());
        Faculty faculty = Faculty.builder()
                .employeeId(request.employeeId())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .qualification(request.qualification())
                .experienceYears(request.experienceYears())
                .designation(request.designation())
                .department(getDepartment(request.departmentId()))
                .joiningDate(request.joiningDate())
                .photoUrl(request.photoUrl())
                .status(request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status())
                .build();
        return toResponse(facultyRepository.save(faculty));
    }

    @Transactional
    public FacultyResponse update(Long id, FacultyRequest request) {
        Faculty faculty = getFaculty(id);
        validateUnique(id, request.employeeId(), request.email());
        faculty.setEmployeeId(request.employeeId());
        faculty.setFirstName(request.firstName());
        faculty.setLastName(request.lastName());
        faculty.setEmail(request.email());
        faculty.setPhone(request.phone());
        faculty.setDateOfBirth(request.dateOfBirth());
        faculty.setGender(request.gender());
        faculty.setQualification(request.qualification());
        faculty.setExperienceYears(request.experienceYears());
        faculty.setDesignation(request.designation());
        faculty.setDepartment(getDepartment(request.departmentId()));
        faculty.setJoiningDate(request.joiningDate());
        faculty.setPhotoUrl(request.photoUrl());
        if (request.status() != null && !request.status().isBlank()) {
            faculty.setStatus(request.status());
        }
        return toResponse(facultyRepository.save(faculty));
    }

    @Transactional
    public void delete(Long id) {
        Faculty faculty = getFaculty(id);
        if (facultyRepository.existsByDepartmentId(faculty.getDepartment().getId())
                && isDepartmentHod(id)) {
            throw new BadRequestException(
                    "Faculty is the HOD of " + faculty.getDepartment().getDepartmentName()
                            + ". Assign a new HOD before deleting.");
        }
        facultyRepository.delete(faculty);
    }

    private boolean isDepartmentHod(Long facultyId) {
        return departmentRepository.findAll().stream()
                .anyMatch(d -> facultyId.equals(d.getHodId()));
    }

    private void validateUnique(Long id, String employeeId, String email) {
        if (id == null) {
            if (facultyRepository.existsByEmployeeIdIgnoreCase(employeeId)) {
                throw new DuplicateResourceException("Employee ID already exists: " + employeeId);
            }
            if (facultyRepository.existsByEmailIgnoreCase(email)) {
                throw new DuplicateResourceException("Faculty email already exists: " + email);
            }
        } else {
            if (facultyRepository.existsByEmployeeIdIgnoreCaseAndIdNot(employeeId, id)) {
                throw new DuplicateResourceException("Employee ID already exists: " + employeeId);
            }
            if (facultyRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                throw new DuplicateResourceException("Faculty email already exists: " + email);
            }
        }
    }

    private Faculty getFaculty(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id " + id));
    }

    private Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + id));
    }

    private FacultyResponse toResponse(Faculty f) {
        return new FacultyResponse(f.getId(), f.getEmployeeId(), f.getFirstName(), f.getLastName(),
                f.getFirstName() + " " + f.getLastName(), f.getEmail(), f.getPhone(),
                f.getDateOfBirth() == null ? null : f.getDateOfBirth().toString(), f.getGender(),
                f.getQualification(), f.getExperienceYears(), f.getDesignation(),
                f.getDepartment().getId(), f.getDepartment().getDepartmentName(),
                f.getJoiningDate() == null ? null : f.getJoiningDate().toString(),
                f.getPhotoUrl(), f.getStatus());
    }
}

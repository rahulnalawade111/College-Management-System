package com.college.sms.service;

import com.college.sms.dto.DepartmentRequest;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.CourseRepository;
import com.college.sms.repository.DepartmentRepository;
import com.college.sms.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private SubjectRepository subjectRepository;

    private DepartmentService service;

    @BeforeEach
    void setUp() {
        service = new DepartmentService(departmentRepository, courseRepository, subjectRepository);
    }

    @Test
    void createPersistsDepartment() {
        when(departmentRepository.existsByDepartmentCodeIgnoreCase("AI")).thenReturn(false);
        when(departmentRepository.existsByDepartmentNameIgnoreCase("Artificial Intelligence")).thenReturn(false);
        when(departmentRepository.save(any())).thenAnswer(inv -> {
            var d = (com.college.sms.entity.Department) inv.getArgument(0);
            d.setId(8L);
            return d;
        });

        var response = service.create(new DepartmentRequest("AI", "Artificial Intelligence", "ML and AI programs", null));

        assertThat(response.id()).isEqualTo(8L);
        assertThat(response.departmentCode()).isEqualTo("AI");
    }

    @Test
    void createDuplicateCodeThrowsConflict() {
        when(departmentRepository.existsByDepartmentCodeIgnoreCase("CS")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new DepartmentRequest("CS", "Computer Science", null, null)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void deleteReferencedDepartmentIsRefused() {
        when(departmentRepository.findById(1L)).thenReturn(java.util.Optional.of(
                com.college.sms.entity.Department.builder()
                        .id(1L).departmentCode("CS").departmentName("Computer Science").build()));
        when(courseRepository.existsByDepartmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("referenced by courses or subjects");
    }

    @Test
    void deleteUnreferencedDepartmentSucceeds() {
        when(departmentRepository.findById(2L)).thenReturn(java.util.Optional.of(
                com.college.sms.entity.Department.builder()
                        .id(2L).departmentCode("XX").departmentName("Obsolete Dept").build()));
        when(courseRepository.existsByDepartmentId(2L)).thenReturn(false);
        when(subjectRepository.existsByDepartmentId(2L)).thenReturn(false);

        service.delete(2L);
        org.mockito.Mockito.verify(departmentRepository).delete(any());
    }

    @Test
    void findByIdMissingThrowsNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

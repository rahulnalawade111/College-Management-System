package com.college.sms.service;

import com.college.sms.dto.FacultyRequest;
import com.college.sms.dto.FacultyResponse;
import com.college.sms.entity.Department;
import com.college.sms.entity.Faculty;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.DepartmentRepository;
import com.college.sms.repository.FacultyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacultyServiceTest {

    @Mock
    private FacultyRepository facultyRepository;
    @Mock
    private DepartmentRepository departmentRepository;

    private FacultyService service;

    private final Department cs = Department.builder().id(1L).departmentCode("CS")
            .departmentName("Computer Science").build();

    @BeforeEach
    void setUp() {
        service = new FacultyService(facultyRepository, departmentRepository);
    }

    private FacultyRequest request(String employeeId, String email) {
        return new FacultyRequest(employeeId, "Rajesh", "Kumar", email, "9876500001",
                null, "MALE", "Ph.D.", 15, "Professor", 1L, null, null, null);
    }

    @Test
    void createPersistsFaculty() {
        when(facultyRepository.existsByEmployeeIdIgnoreCase("FAC100")).thenReturn(false);
        when(facultyRepository.existsByEmailIgnoreCase("rajesh@college.edu")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(cs));
        when(facultyRepository.save(any(Faculty.class))).thenAnswer(inv -> inv.getArgument(0));

        FacultyResponse response = service.create(request("FAC100", "rajesh@college.edu"));

        assertThat(response.employeeId()).isEqualTo("FAC100");
        assertThat(response.fullName()).isEqualTo("Rajesh Kumar");
        assertThat(response.departmentName()).isEqualTo("Computer Science");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void createRejectsDuplicateEmployeeId() {
        when(facultyRepository.existsByEmployeeIdIgnoreCase("FAC100")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("FAC100", "rajesh@college.edu")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("FAC100");
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(facultyRepository.existsByEmployeeIdIgnoreCase("FAC100")).thenReturn(false);
        when(facultyRepository.existsByEmailIgnoreCase("rajesh@college.edu")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("FAC100", "rajesh@college.edu")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");
    }

    @Test
    void updateChangesFields() {
        Faculty existing = Faculty.builder().id(5L).employeeId("FAC001").firstName("Old")
                .lastName("Name").email("old@college.edu").department(cs).status("ACTIVE").build();
        when(facultyRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(facultyRepository.existsByEmployeeIdIgnoreCaseAndIdNot("FAC100", 5L)).thenReturn(false);
        when(facultyRepository.existsByEmailIgnoreCaseAndIdNot("rajesh@college.edu", 5L)).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(cs));
        when(facultyRepository.save(any(Faculty.class))).thenAnswer(inv -> inv.getArgument(0));

        FacultyResponse response = service.update(5L, request("FAC100", "rajesh@college.edu"));

        assertThat(response.employeeId()).isEqualTo("FAC100");
        assertThat(response.email()).isEqualTo("rajesh@college.edu");
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(facultyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllReturnsPagedResults() {
        Faculty f = Faculty.builder().id(1L).employeeId("FAC001").firstName("Rajesh")
                .lastName("Kumar").email("r@college.edu").department(cs).status("ACTIVE").build();
        when(facultyRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(f)));

        var page = service.findAll("rajesh", 1L, "ACTIVE", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).fullName()).isEqualTo("Rajesh Kumar");
    }

    @Test
    void deleteRemovesFaculty() {
        Faculty f = Faculty.builder().id(5L).employeeId("FAC001").firstName("A").lastName("B")
                .email("a@college.edu").department(cs).status("ACTIVE").build();
        when(facultyRepository.findById(5L)).thenReturn(Optional.of(f));

        service.delete(5L);

        verify(facultyRepository).delete(f);
    }
}

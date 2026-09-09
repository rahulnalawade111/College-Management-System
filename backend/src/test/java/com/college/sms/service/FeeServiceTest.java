package com.college.sms.service;

import com.college.sms.dto.FeeResponse;
import com.college.sms.entity.AcademicYear;
import com.college.sms.entity.Course;
import com.college.sms.entity.Fee;
import com.college.sms.entity.Student;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.AcademicYearRepository;
import com.college.sms.repository.FeeRepository;
import com.college.sms.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeeServiceTest {

    @Mock private FeeRepository feeRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private AcademicYearRepository academicYearRepository;

    @InjectMocks private FeeService feeService;

    private Student student;
    private LocalDate futureDue;
    private LocalDate pastDue;

    @BeforeEach
    void setUp() {
        Course course = Course.builder().id(1L).courseName("BCA").build();
        AcademicYear year = AcademicYear.builder().id(1L).yearName("2026-2027").build();
        student = Student.builder().id(1L).studentId("ABC2026CS001")
                .firstName("Aarav").lastName("Shah").course(course).build();
        futureDue = LocalDate.now().plusDays(30);
        pastDue = LocalDate.now().minusDays(10);

        lenient().when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        lenient().when(academicYearRepository.findById(1L)).thenReturn(Optional.of(year));
        lenient().when(feeRepository.save(any(Fee.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(feeRepository.findById(1L)).thenAnswer(inv -> Optional.of(fee(BigDecimal.ZERO, futureDue)));
    }

    private Fee fee(BigDecimal paid, LocalDate due) {
        return Fee.builder().id(1L).student(student).title("Tuition Fee — Sem 1").feeType("TUITION")
                .amount(new BigDecimal("45000")).paidAmount(paid).dueDate(due)
                .status(paid.signum() > 0 ? "PARTIALLY_PAID" : "PENDING").build();
    }

    private FeeResponse.FeeRequest req() {
        return new FeeResponse.FeeRequest(1L, "Tuition Fee — Sem 1", "TUITION",
                new BigDecimal("45000"), futureDue);
    }

    @Test
    void create_newFeeIsPending() {
        FeeResponse out = feeService.create(req());
        assertThat(out.status()).isEqualTo("PENDING");
        assertThat(out.balance()).isEqualByComparingTo("45000");
        assertThat(out.paidAmount()).isEqualByComparingTo("0");
    }

    @Test
    void create_pastDueIsOverdue() {
        FeeResponse.FeeRequest r = new FeeResponse.FeeRequest(1L, "Tuition", "TUITION",
                new BigDecimal("1000"), pastDue);
        assertThat(feeService.create(r).status()).isEqualTo("OVERDUE");
    }

    @Test
    void create_invalidFeeTypeRejected() {
        FeeResponse.FeeRequest r = new FeeResponse.FeeRequest(1L, "Tuition", "SUBSCRIPTION",
                new BigDecimal("1000"), futureDue);
        assertThatThrownBy(() -> feeService.create(r))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid fee type");
    }

    @Test
    void create_missingStudentThrows() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        FeeResponse.FeeRequest r = new FeeResponse.FeeRequest(99L, "Tuition", "TUITION",
                new BigDecimal("1000"), futureDue);
        assertThatThrownBy(() -> feeService.create(r))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void recordPayment_fullPaymentMarksPaid() {
        Fee f = fee(BigDecimal.ZERO, futureDue);
        when(feeRepository.findById(1L)).thenReturn(Optional.of(f));

        FeeResponse out = feeService.recordPayment(1L,
                new FeeResponse.PaymentRequest(new BigDecimal("45000"), "UPI"));
        assertThat(out.status()).isEqualTo("PAID");
        assertThat(out.balance()).isEqualByComparingTo("0");
        assertThat(out.paidAt()).isNotNull();
        assertThat(out.paymentMethod()).isEqualTo("UPI");
    }

    @Test
    void recordPayment_partialIsPartiallyPaid() {
        Fee f = fee(BigDecimal.ZERO, futureDue);
        when(feeRepository.findById(1L)).thenReturn(Optional.of(f));

        FeeResponse out = feeService.recordPayment(1L,
                new FeeResponse.PaymentRequest(new BigDecimal("20000"), "CASH"));
        assertThat(out.status()).isEqualTo("PARTIALLY_PAID");
        assertThat(out.balance()).isEqualByComparingTo("25000");
    }

    @Test
    void recordPayment_overpaymentRejected() {
        Fee f = fee(new BigDecimal("40000"), futureDue);
        when(feeRepository.findById(1L)).thenReturn(Optional.of(f));

        assertThatThrownBy(() -> feeService.recordPayment(1L,
                new FeeResponse.PaymentRequest(new BigDecimal("10000"), "CASH")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceeds outstanding balance");
    }

    @Test
    void recordPayment_alreadyPaidRejected() {
        Fee f = fee(new BigDecimal("45000"), futureDue);
        f.setStatus("PAID");
        when(feeRepository.findById(1L)).thenReturn(Optional.of(f));

        assertThatThrownBy(() -> feeService.recordPayment(1L,
                new FeeResponse.PaymentRequest(BigDecimal.ONE, "CASH")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already fully paid");
    }

    @Test
    void recordPayment_invalidMethodRejected() {
        assertThatThrownBy(() -> feeService.recordPayment(1L,
                new FeeResponse.PaymentRequest(BigDecimal.TEN, "CRYPTO")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid payment method");
    }

    @Test
    void update_withPaymentsRejected() {
        Fee f = fee(new BigDecimal("5000"), futureDue);
        when(feeRepository.findById(1L)).thenReturn(Optional.of(f));

        assertThatThrownBy(() -> feeService.update(1L, req()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already has payments");
    }

    @Test
    void summary_computesTotals() {
        when(feeRepository.totalsForStudent(1L)).thenReturn(
                List.<Object[]>of(new Object[]{new BigDecimal("46500"), new BigDecimal("45000")}));

        FeeResponse.FeeSummary s = feeService.summary(1L);
        assertThat(s.totalBilled()).isEqualByComparingTo("46500");
        assertThat(s.totalCollected()).isEqualByComparingTo("45000");
        assertThat(s.totalOutstanding()).isEqualByComparingTo("1500");
    }

    @Test
    void forStudent_mapsResponses() {
        Fee f = fee(BigDecimal.ZERO, futureDue);
        when(feeRepository.findByStudentIdOrderByDueDateAsc(1L)).thenReturn(List.of(f));

        List<FeeResponse> out = feeService.forStudent(1L);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).studentCode()).isEqualTo("ABC2026CS001");
        assertThat(out.get(0).studentName()).isEqualTo("Aarav Shah");
    }
}

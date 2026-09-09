package com.college.sms.service;

import com.college.sms.dto.FeeResponse;
import com.college.sms.entity.AcademicYear;
import com.college.sms.entity.Fee;
import com.college.sms.entity.Student;
import com.college.sms.exception.BadRequestException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.repository.AcademicYearRepository;
import com.college.sms.repository.FeeRepository;
import com.college.sms.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class FeeService {

    private static final Set<String> FEE_TYPES =
            Set.of("TUITION", "EXAM", "HOSTEL", "LIBRARY", "TRANSPORT", "MISC");
    private static final Set<String> PAYMENT_METHODS =
            Set.of("CASH", "UPI", "BANK_TRANSFER", "CARD", "CHEQUE");

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;
    private final AcademicYearRepository academicYearRepository;

    public FeeService(FeeRepository feeRepository,
                      StudentRepository studentRepository,
                      AcademicYearRepository academicYearRepository) {
        this.feeRepository = feeRepository;
        this.studentRepository = studentRepository;
        this.academicYearRepository = academicYearRepository;
    }

    // ------------------------------------------------------------------
    // admin fee management
    // ------------------------------------------------------------------

    @Transactional
    public FeeResponse create(FeeResponse.FeeRequest req) {
        if (!FEE_TYPES.contains(req.feeType())) {
            throw new BadRequestException("Invalid fee type: " + req.feeType()
                    + " — allowed: " + String.join(", ", FEE_TYPES));
        }
        Student student = studentRepository.findById(req.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + req.studentId()));

        Fee fee = Fee.builder()
                .student(student)
                .title(req.title())
                .feeType(req.feeType())
                .amount(req.amount())
                .paidAmount(BigDecimal.ZERO)
                .dueDate(req.dueDate())
                .status(computeStatus(BigDecimal.ZERO, req.amount(), req.dueDate()))
                .build();
        return toResponse(feeRepository.save(fee));
    }

    @Transactional
    public FeeResponse update(Long id, FeeResponse.FeeRequest req) {
        if (!FEE_TYPES.contains(req.feeType())) {
            throw new BadRequestException("Invalid fee type: " + req.feeType());
        }
        Fee fee = getFee(id);
        if (fee.getPaidAmount().signum() > 0) {
            throw new BadRequestException("Cannot edit a fee that already has payments — remove payments first");
        }
        Student student = studentRepository.findById(req.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + req.studentId()));
        fee.setStudent(student);
        fee.setTitle(req.title());
        fee.setFeeType(req.feeType());
        fee.setAmount(req.amount());
        fee.setDueDate(req.dueDate());
        fee.setStatus(computeStatus(fee.getPaidAmount(), fee.getAmount(), fee.getDueDate()));
        return toResponse(feeRepository.save(fee));
    }

    @Transactional
    public void delete(Long id) {
        feeRepository.delete(getFee(id));
    }

    /** Record a payment against a fee. Balance cannot go negative; PAID flips paidAt. */
    @Transactional
    public FeeResponse recordPayment(Long id, FeeResponse.PaymentRequest req) {
        if (req.paymentMethod() != null && !PAYMENT_METHODS.contains(req.paymentMethod())) {
            throw new BadRequestException("Invalid payment method: " + req.paymentMethod()
                    + " — allowed: " + String.join(", ", PAYMENT_METHODS));
        }
        Fee fee = getFee(id);
        BigDecimal balance = fee.balance();
        if (balance.signum() <= 0) {
            throw new BadRequestException("This fee is already fully paid");
        }
        if (req.amount().compareTo(balance) > 0) {
            throw new BadRequestException("Payment " + req.amount() + " exceeds outstanding balance " + balance);
        }
        fee.setPaidAmount(fee.getPaidAmount().add(req.amount()));
        fee.setPaymentMethod(req.paymentMethod());
        fee.setPaidAt(Instant.now());
        fee.setStatus(computeStatus(fee.getPaidAmount(), fee.getAmount(), fee.getDueDate()));
        return toResponse(feeRepository.save(fee));
    }

    // ------------------------------------------------------------------
    // reads
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<FeeResponse> list(Long studentId, Pageable pageable) {
        if (studentId != null) {
            return feeRepository.findByStudentId(studentId, pageable).map(this::toResponse);
        }
        return feeRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<FeeResponse> forStudent(Long studentId) {
        return feeRepository.findByStudentIdOrderByDueDateAsc(studentId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FeeResponse.FeeSummary summary(Long studentId) {
        List<Object[]> rows = studentId == null
                ? feeRepository.totalsAll()
                : feeRepository.totalsForStudent(studentId);
        Object[] r = rows.isEmpty() ? new Object[]{null, null} : rows.get(0);
        BigDecimal billed = asBigDecimal(r[0]);
        BigDecimal collected = asBigDecimal(r[1]);
        BigDecimal outstanding = billed.subtract(collected).max(BigDecimal.ZERO);
        return new FeeResponse.FeeSummary(billed, collected, outstanding);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static String computeStatus(BigDecimal paid, BigDecimal amount, LocalDate dueDate) {
        if (paid.compareTo(amount) >= 0) return "PAID";
        if (paid.signum() > 0) return "PARTIALLY_PAID";
        return LocalDate.now().isAfter(dueDate) ? "OVERDUE" : "PENDING";
    }

    private Fee getFee(Long id) {
        return feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee not found with id " + id));
    }

    private static BigDecimal asBigDecimal(Object o) {
        return o == null ? BigDecimal.ZERO
                : (o instanceof BigDecimal b ? b : new BigDecimal(o.toString()));
    }

    private FeeResponse toResponse(Fee f) {
        Student s = f.getStudent();
        return new FeeResponse(
                f.getId(),
                s.getId(),
                s.getStudentId(),
                s.getFirstName() + " " + s.getLastName(),
                f.getTitle(),
                f.getFeeType(),
                f.getAmount(),
                f.getPaidAmount(),
                f.balance().max(BigDecimal.ZERO),
                f.getDueDate(),
                f.getStatus(),
                f.getPaymentMethod(),
                f.getPaidAt());
    }
}

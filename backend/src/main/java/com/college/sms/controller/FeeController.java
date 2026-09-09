package com.college.sms.controller;

import com.college.sms.dto.FeeResponse;
import com.college.sms.service.AssignmentAccessGuard;
import com.college.sms.service.FeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fees")
public class FeeController {

    private final FeeService feeService;
    private final AssignmentAccessGuard accessGuard;

    public FeeController(FeeService feeService, AssignmentAccessGuard accessGuard) {
        this.feeService = feeService;
        this.accessGuard = accessGuard;
    }

    /** Paginated fee list, optional studentId filter. ADMIN only for the full ledger. */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public Page<FeeResponse> list(@RequestParam(required = false) Long studentId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return feeService.list(studentId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dueDate")));
    }

    /** Student's own fees. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<FeeResponse> mine() {
        return feeService.forStudent(accessGuard.currentStudentId());
    }

    /** Parent: linked child's fees. */
    @GetMapping("/my-child")
    @PreAuthorize("hasRole('PARENT')")
    public List<FeeResponse> myChild() {
        Long studentId = accessGuard.currentParentStudentId();
        accessGuard.guardStudentAccess(studentId);
        return feeService.forStudent(studentId);
    }

    /** ADMIN ledger totals; student/parent summary for own scope. */
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public FeeResponse.FeeSummary summary(@RequestParam(required = false) Long studentId) {
        String role = accessGuard.currentRole();
        if ("STUDENT".equals(role)) {
            return feeService.summary(accessGuard.currentStudentId());
        }
        if ("PARENT".equals(role)) {
            Long studentId2 = accessGuard.currentParentStudentId();
            accessGuard.guardStudentAccess(studentId2);
            return feeService.summary(studentId2);
        }
        return feeService.summary(studentId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public FeeResponse create(@Valid @RequestBody FeeResponse.FeeRequest req) {
        return feeService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public FeeResponse update(@PathVariable Long id, @Valid @RequestBody FeeResponse.FeeRequest req) {
        return feeService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        feeService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Fee deleted"));
    }

    /** Record a payment — ADMIN (fee counter). */
    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public FeeResponse pay(@PathVariable Long id, @Valid @RequestBody FeeResponse.PaymentRequest req) {
        return feeService.recordPayment(id, req);
    }
}

package org.thehartford.willowshield.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thehartford.willowshield.dto.CreatePolicyPlanDTO;
import org.thehartford.willowshield.dto.ReadPolicyPlanDTO;
import org.thehartford.willowshield.dto.ReadStaffDTO;
import org.thehartford.willowshield.entity.PolicyPlan;
import org.thehartford.willowshield.service.AdminService;
import org.thehartford.willowshield.dto.ReadVehicleApplicationDTO;
import org.thehartford.willowshield.dto.UpdateApplicationStatusDTO;
import org.thehartford.willowshield.dto.UnderwriterWorkloadDTO;
import org.thehartford.willowshield.dto.ReadClaimDTO;
import org.thehartford.willowshield.dto.ClaimsOfficerWorkloadDTO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.thehartford.willowshield.service.ExcelExportService;
import org.thehartford.willowshield.dto.ReadPaymentDTO;
import org.thehartford.willowshield.dto.ReadClaimsPaymentDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private ExcelExportService excelExportService;

    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, String>> createStaff(
            @Valid @RequestBody org.thehartford.willowshield.dto.CreateStaffDTO dto) {

        adminService.createStaff(dto);
        return ResponseEntity.ok(java.util.Map.of("message", "Staff created successfully"));
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<ReadStaffDTO> deactivateStaff(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(adminService.deactivateStaff(id));
    }

    @PutMapping("/staff/{id}/activate")
    public ResponseEntity<ReadStaffDTO> activateStaff(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(adminService.activateStaff(id));
    }

    @GetMapping("/staff")
    public ResponseEntity<List<ReadStaffDTO>> getAllStaff() {
        return ResponseEntity.ok(adminService.getAllStaff());
    }

    @GetMapping("/staff/{id}")
    public ResponseEntity<ReadStaffDTO> getStaffById(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(adminService.getStaffById(id));
    }

    @PostMapping("/policy-plans")
    public ResponseEntity<PolicyPlan> addPolicyPlan(
            @Valid @RequestBody CreatePolicyPlanDTO dto) {

        return ResponseEntity.ok(adminService.addPolicyPlan(dto));
    }

    @GetMapping("/policy-plans")
    public ResponseEntity<List<ReadPolicyPlanDTO>> getAllPolicyPlans() {
        return ResponseEntity.ok(adminService.getAllPolicyPlans());
    }

    @GetMapping("/policy-plans/{id}")
    public ResponseEntity<ReadPolicyPlanDTO> getPolicyPlanById(
            @PathVariable("id") Integer id) {

        return ResponseEntity.ok(adminService.getPolicyPlanById(id));
    }

    @DeleteMapping("/policy-plans/{id}")
    public ResponseEntity<ReadPolicyPlanDTO> deactivatePolicyPlan(
            @PathVariable("id") Integer id) {

        return ResponseEntity.ok(adminService.deactivatePolicyPlan(id));
    }

    // activate policy plan
    @PutMapping("/policy-plans/{id}/activate")
    public ResponseEntity<ReadPolicyPlanDTO> activatePolicyPlan(
            @PathVariable("id") Integer id) {

        return ResponseEntity.ok(adminService.activatePolicyPlan(id));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ReadVehicleApplicationDTO>> getAllApplications() {
        return ResponseEntity.ok(adminService.getAllApplications());
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<ReadVehicleApplicationDTO> updateApplicationStatus(
            @PathVariable("id") Integer id,
            @RequestBody UpdateApplicationStatusDTO dto) {

        return ResponseEntity.ok(adminService.updateApplicationStatus(id, dto));
    }

    @GetMapping("/underwriters/workload")
    public ResponseEntity<List<UnderwriterWorkloadDTO>> getUnderwritersByWorkload() {
        return ResponseEntity.ok(adminService.getUnderwritersByWorkload());
    }

    @PutMapping("/applications/{id}/assign/{underwriterId}")
    public ResponseEntity<ReadVehicleApplicationDTO> assignApplication(
            @PathVariable("id") Integer id,
            @PathVariable("underwriterId") Long underwriterId) {

        return ResponseEntity.ok(adminService.assignApplication(id, underwriterId));
    }

    @GetMapping("/claims")
    public ResponseEntity<List<ReadClaimDTO>> getAllClaims() {
        return ResponseEntity.ok(adminService.getAllClaims());
    }

    @GetMapping("/claims-officers/workload")
    public ResponseEntity<List<ClaimsOfficerWorkloadDTO>> getClaimsOfficerWorkload() {
        return ResponseEntity.ok(adminService.getClaimsOfficerWorkload());
    }

    @PutMapping("/claims/{id}/assign/{officerId}")
    public ResponseEntity<ReadClaimDTO> assignClaim(
            @PathVariable("id") Integer id,
            @PathVariable("officerId") Long officerId) {

        return ResponseEntity.ok(adminService.assignClaim(id, officerId));
    }

    @GetMapping("/claims/pending-payments")
    public ResponseEntity<List<ReadClaimDTO>> getPendingPayments() {
        return ResponseEntity.ok(adminService.getPendingPayments());
    }

    @GetMapping("/received-payments")
    public ResponseEntity<List<ReadPaymentDTO>> getReceivedPayments() {
        return ResponseEntity.ok(adminService.getAllReceivedPayments());
    }

    @GetMapping("/claims/payouts")
    public ResponseEntity<List<ReadClaimsPaymentDTO>> getAllPayouts() {
        return ResponseEntity.ok(adminService.getAllClaimPayouts());
    }

    @PostMapping("/claims/{id}/pay")
    public ResponseEntity<java.util.Map<String, String>> processClaimPayment(
            @PathVariable("id") Integer id) {

        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        Long adminId = Long.parseLong(authentication.getName());

        adminService.processClaimPayment(id, adminId);
        return ResponseEntity.ok(java.util.Map.of("message", "Payment processed successfully"));
    }

    @GetMapping("/export/received-payments")
    public ResponseEntity<InputStreamResource> exportReceivedPayments() throws IOException {
        List<ReadPaymentDTO> payments = adminService.getAllReceivedPayments();
        ByteArrayInputStream in = excelExportService.exportReceivedPaymentsToExcel(payments);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=received_payments.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/export/payout-history")
    public ResponseEntity<InputStreamResource> exportPayoutHistory() throws IOException {
        List<ReadClaimsPaymentDTO> payouts = adminService.getAllClaimPayouts();
        ByteArrayInputStream in = excelExportService.exportPayoutHistoryToExcel(payouts);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=payout_history.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}

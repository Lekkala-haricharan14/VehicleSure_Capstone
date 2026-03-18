package org.thehartford.willowshield.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.thehartford.willowshield.dto.ApproveClaimRequest;
import org.thehartford.willowshield.dto.ReadClaimDTO;
import org.thehartford.willowshield.dto.RejectClaimRequest;
import org.thehartford.willowshield.service.ClaimsOfficerService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/claims-officer")
@PreAuthorize("hasRole('CLAIMS_OFFICER')")
public class ClaimsOfficerController {

    @Autowired
    private ClaimsOfficerService claimsOfficerService;

    @GetMapping("/claims")
    public ResponseEntity<List<ReadClaimDTO>> getAssignedClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long officerId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(claimsOfficerService.getAssignedClaims(officerId));
    }

    @PostMapping("/claims/{id}/approve")
    public ResponseEntity<Void> approveClaim(@PathVariable("id") Integer id,
            @RequestBody ApproveClaimRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long officerId = Long.parseLong(authentication.getName());
        claimsOfficerService.approveClaim(id, officerId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/claims/{id}/payout")
    public ResponseEntity<BigDecimal> calculatePayment(@PathVariable("id") Integer id,
            @RequestBody ApproveClaimRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long officerId = Long.parseLong(authentication.getName());
        BigDecimal amount = claimsOfficerService.calculatePayment(id, officerId, request);
        return ResponseEntity.ok(amount);
    }

    @PostMapping("/claims/{id}/reject")
    public ResponseEntity<Void> rejectClaim(@PathVariable("id") Integer id,
            @RequestBody(required = false) RejectClaimRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long officerId = Long.parseLong(authentication.getName());
        String reason = (request != null) ? request.getReason() : null;
        claimsOfficerService.rejectClaim(id, officerId, reason);
        return ResponseEntity.ok().build();
    }
}

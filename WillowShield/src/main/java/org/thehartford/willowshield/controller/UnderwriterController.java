package org.thehartford.willowshield.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.thehartford.willowshield.dto.ReadVehicleApplicationDTO;
import org.thehartford.willowshield.service.UnderwriterService;

import java.util.List;

@RestController
@RequestMapping("/api/underwriter")
@PreAuthorize("hasRole('UNDERWRITER')")
public class UnderwriterController {

    @Autowired
    private UnderwriterService underwriterService;

    @GetMapping("/applications")
    public ResponseEntity<List<ReadVehicleApplicationDTO>> getAssignedApplications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long underwriterId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(underwriterService.getAssignedApplications(underwriterId));
    }

    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<Void> approveApplication(@PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long underwriterId = Long.parseLong(authentication.getName());
        underwriterService.approveApplication(id, underwriterId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<Void> rejectApplication(@PathVariable("id") Integer id,
            @RequestBody RejectionRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long underwriterId = Long.parseLong(authentication.getName());
        underwriterService.rejectApplication(id, underwriterId, request.getReason());
        return ResponseEntity.ok().build();
    }

    static class RejectionRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}

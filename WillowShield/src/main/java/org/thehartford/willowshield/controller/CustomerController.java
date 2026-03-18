package org.thehartford.willowshield.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thehartford.willowshield.dto.CreateVehicleApplicationDTO;
import org.thehartford.willowshield.dto.ReadVehicleApplicationDTO;
import org.thehartford.willowshield.service.CustomerService;
import org.thehartford.willowshield.service.ClaimService;
import org.thehartford.willowshield.dto.CreateClaimDTO;
import org.thehartford.willowshield.dto.ReadClaimDTO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import org.thehartford.willowshield.dto.ReadPolicyPlanDTO;
import org.thehartford.willowshield.dto.QuoteRequestDTO;
import org.thehartford.willowshield.dto.QuoteResponseDTO;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ClaimService claimService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/buy-policy", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> buyPolicy(
            @RequestParam("application") String applicationJson,
            @RequestParam("rcDocument") MultipartFile rcDocument,
            @RequestParam("invoiceDocument") MultipartFile invoiceDocument) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long customerId = Long.parseLong(authentication.getName());

        // Parse JSON into DTO
        CreateVehicleApplicationDTO dto = objectMapper.readValue(applicationJson, CreateVehicleApplicationDTO.class);

        customerService.submitVehicleApplication(customerId, dto, rcDocument, invoiceDocument);

        return ResponseEntity.ok("Application submitted successfully for review");
    }

    @GetMapping("/applications")
    public ResponseEntity<?> getMyApplications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long customerId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(customerService.getCustomerApplications(customerId));
    }

    @GetMapping("/policies")
    public ResponseEntity<?> getMyPolicies() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long customerId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(customerService.getCustomerPolicies(customerId));
    }

    // GET /policy-plans?type=CAR

    @GetMapping("/policy-plans")
    public ResponseEntity<List<ReadPolicyPlanDTO>> getActivePolicyPlans(
            @RequestParam(required = false, name = "type") String typeParam) {

        return ResponseEntity.ok(customerService.getActivePolicyPlans(typeParam));
    }

    // POST /quote
    @PostMapping("/quote")
    public ResponseEntity<List<QuoteResponseDTO>> getQuotes(@Valid @RequestBody QuoteRequestDTO request) {
        return ResponseEntity.ok(customerService.generateQuotes(request));
    }

    // ── Claims ──────────────────────────────────────────
    @PostMapping(value = "/submit-claim", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReadClaimDTO> submitClaim(
            @RequestPart("claim") String claimJson,
            @RequestPart(value = "doc1", required = false) MultipartFile doc1,
            @RequestPart(value = "doc2", required = false) MultipartFile doc2,
            @RequestPart(value = "doc3", required = false) MultipartFile doc3) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long customerId = Long.parseLong(authentication.getName());

        CreateClaimDTO claimDTO = objectMapper.readValue(claimJson, CreateClaimDTO.class);
        return ResponseEntity.ok(claimService.submitClaim(customerId, claimDTO, doc1, doc2, doc3));
    }

    @GetMapping("/my-claims")
    public ResponseEntity<?> getMyClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long customerId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(claimService.getCustomerClaims(customerId));
    }
}

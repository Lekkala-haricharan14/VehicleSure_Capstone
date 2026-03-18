package org.thehartford.willowshield.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thehartford.willowshield.dto.PaymentRequestDTO;
import org.thehartford.willowshield.entity.Policy;
import org.thehartford.willowshield.repository.PolicyRepository;
import org.thehartford.willowshield.service.PaymentService;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasRole('CUSTOMER')")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private org.thehartford.willowshield.service.RazorpayService razorpayService;

    @PostMapping("/create-order")
    public ResponseEntity<org.thehartford.willowshield.dto.RazorpayOrderResponseDTO> createOrder(@RequestBody PaymentRequestDTO request) throws Exception {
        return ResponseEntity.ok(razorpayService.createOrder(request.getPolicyId(), request.getAmount()));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody org.thehartford.willowshield.dto.RazorpayVerificationRequestDTO request) throws java.io.IOException {
        if (razorpayService.verifySignature(request)) {
            paymentService.finalizePayment(request);
            return ResponseEntity.ok("Payment verified and policy activated.");
        } else {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body("Invalid payment signature.");
        }
    }

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequestDTO request) throws java.io.IOException {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        Long customerId = Long.parseLong(auth.getName());

        paymentService.processPayment(request, customerId);
        return ResponseEntity.ok("Payment processed successfully and documents generated.");
    }

    @GetMapping("/download/invoice/{policyId}")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable("policyId") Integer policyId)
            throws MalformedURLException {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        if (policy.getInvoicePath() == null) {
            return ResponseEntity.notFound().build();
        }

        return downloadFile(policy.getInvoicePath());
    }

    @GetMapping("/download/policy/{policyId}")
    public ResponseEntity<Resource> downloadPolicy(@PathVariable("policyId") Integer policyId)
            throws MalformedURLException {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        if (policy.getPolicyDocumentPath() == null) {
            return ResponseEntity.notFound().build();
        }

        return downloadFile(policy.getPolicyDocumentPath());
    }

    private ResponseEntity<Resource> downloadFile(String filePath) throws MalformedURLException {
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());

        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

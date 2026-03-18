package org.thehartford.willowshield.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thehartford.willowshield.dto.PaymentRequestDTO;
import org.thehartford.willowshield.entity.Payment;
import org.thehartford.willowshield.entity.Policy;
import org.thehartford.willowshield.enums.PaymentStatus;
import org.thehartford.willowshield.enums.PolicyStatus;
import org.thehartford.willowshield.enums.VehicleApplicationStatus;
import org.thehartford.willowshield.exceptions.*;
import org.thehartford.willowshield.repository.PaymentRepository;
import org.thehartford.willowshield.repository.PolicyRepository;
import org.thehartford.willowshield.repository.VehicleApplicationRepository;
import org.thehartford.willowshield.utility.PdfGenerator;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private VehicleApplicationRepository vehicleApplicationRepository;

    @Autowired
    private PdfGenerator pdfGenerator;

    @Autowired
    private NotificationService notificationService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public void finalizePayment(org.thehartford.willowshield.dto.RazorpayVerificationRequestDTO request) throws IOException {
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new PolicyNotFoundException(request.getPolicyId()));

        // 1. Create and save Payment
        Payment payment = new Payment();
        payment.setAmount(policy.getPremiumAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionReference(request.getRazorpayPaymentId());
        payment.setPolicy(policy);
        paymentRepository.save(payment);

        // 2. Update Application Status to PAID
        if (policy.getVehicle() != null && policy.getVehicle().getVehicleApplication() != null) {
            policy.getVehicle().getVehicleApplication().setStatus(VehicleApplicationStatus.PAID);
            vehicleApplicationRepository.save(policy.getVehicle().getVehicleApplication());
        }

        // 3. Generate Documents
        String invoiceFilename = String.format("policy_%d_invoice_%s.pdf", policy.getPolicyId(),
                LocalDateTime.now().getNano());
        String policyFilename = String.format("policy_%d_document_%s.pdf", policy.getPolicyId(),
                LocalDateTime.now().getNano());

        String relativeInvoicePath = "uploads/invoices/" + invoiceFilename;
        String relativePolicyPath = "uploads/policies/" + policyFilename;

        pdfGenerator.generateInvoice(payment, relativeInvoicePath);
        pdfGenerator.generatePolicyDocument(policy, relativePolicyPath);

        // 4. Update Policy
        policy.setInvoicePath(relativeInvoicePath);
        policy.setPolicyDocumentPath(relativePolicyPath);
        policy.setStatus(PolicyStatus.ACTIVE);
        policyRepository.save(policy);

        notificationService.createNotification(policy.getCustomer(),
                "Premium payment for policy " + policy.getPolicyNumber() + " was successful via Razorpay. Your policy is now active.",
                "PAYMENT_SUCCESS");

        notificationService.notifyAdmins(
                "Premium paid via Razorpay for policy " + policy.getPolicyNumber() + " by " + policy.getCustomer().getUsername(),
                "PREMIUM_PAID");
    }

    @Transactional
    public void processPayment(PaymentRequestDTO request, Long customerId) throws IOException {
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new PolicyNotFoundException(request.getPolicyId()));

        // Security check: ensure policy belongs to the logged-in user
        if (!policy.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedAccessException("Unauthorized: You do not own this policy");
        }

        // 1. Create and save Payment
        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionReference(request.getTransactionReference());
        payment.setPolicy(policy);
        paymentRepository.save(payment);

        // 2. Update Application Status to PAID
        if (policy.getVehicle() != null && policy.getVehicle().getVehicleApplication() != null) {
            policy.getVehicle().getVehicleApplication().setStatus(VehicleApplicationStatus.PAID);
            vehicleApplicationRepository.save(policy.getVehicle().getVehicleApplication());
        }

        // 3. Generate Documents
        // Consistent with Vehicle Document storage: base/ID/Type_UUID.pdf
        String invoiceFilename = String.format("policy_%d_invoice_%s.pdf", policy.getPolicyId(),
                LocalDateTime.now().getNano());
        String policyFilename = String.format("policy_%d_document_%s.pdf", policy.getPolicyId(),
                LocalDateTime.now().getNano());

        String relativeInvoicePath = "uploads/invoices/" + invoiceFilename;
        String relativePolicyPath = "uploads/policies/" + policyFilename;

        pdfGenerator.generateInvoice(payment, relativeInvoicePath);
        pdfGenerator.generatePolicyDocument(policy, relativePolicyPath);

        // 4. Update Policy with document paths and status
        policy.setInvoicePath(relativeInvoicePath);
        policy.setPolicyDocumentPath(relativePolicyPath);
        policy.setStatus(PolicyStatus.ACTIVE);
        policyRepository.save(policy);
        
        notificationService.createNotification(policy.getCustomer(), 
            "Premium payment for policy " + policy.getPolicyNumber() + " was successful. Your policy is now active.", 
            "PAYMENT_SUCCESS");
            
        notificationService.notifyAdmins(
            "Premium paid for policy " + policy.getPolicyNumber() + " by " + policy.getCustomer().getUsername(), 
            "PREMIUM_PAID");
    }
}

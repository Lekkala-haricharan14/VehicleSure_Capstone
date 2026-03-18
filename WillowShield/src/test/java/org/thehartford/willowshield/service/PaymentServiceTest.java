package org.thehartford.willowshield.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thehartford.willowshield.dto.PaymentRequestDTO;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.entity.Payment;
import org.thehartford.willowshield.entity.Policy;
import org.thehartford.willowshield.enums.PolicyStatus;
import org.thehartford.willowshield.repository.PaymentRepository;
import org.thehartford.willowshield.repository.PolicyRepository;
import org.thehartford.willowshield.repository.VehicleApplicationRepository;
import org.thehartford.willowshield.utility.PdfGenerator;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private VehicleApplicationRepository vehicleApplicationRepository;

    @Mock
    private PdfGenerator pdfGenerator;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "uploadDir", "uploads");
    }

    @Test
    void processPayment_Success() throws IOException {
        Long customerId = 1L;
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setPolicyId(1);
        request.setAmount(BigDecimal.valueOf(15000));
        request.setTransactionReference("TXN123");

        MyUser customer = new MyUser();
        customer.setId(customerId);

        Policy policy = new Policy();
        policy.setPolicyId(1);
        policy.setCustomer(customer);
        policy.setStatus(PolicyStatus.PENDING_PAYMENT);

        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        paymentService.processPayment(request, customerId);

        assertEquals(PolicyStatus.ACTIVE, policy.getStatus());
        verify(paymentRepository).save(any(Payment.class));
        verify(pdfGenerator).generateInvoice(any(Payment.class), anyString());
        verify(pdfGenerator).generatePolicyDocument(any(Policy.class), anyString());
        verify(notificationService).createNotification(eq(customer), anyString(), eq("PREMIUM_PAID"));
        verify(notificationService).notifyAdmins(anyString(), eq("PREMIUM_PAID"));
    }

    @Test
    void processPayment_Unauthorized_ThrowsException() {
        Long customerId = 1L;
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setPolicyId(1);

        MyUser customer = new MyUser();
        customer.setId(2L); // Different ID

        Policy policy = new Policy();
        policy.setPolicyId(1);
        policy.setCustomer(customer);

        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));

        assertThrows(UnauthorizedAccessException.class, () -> paymentService.processPayment(request, customerId));
    }
}

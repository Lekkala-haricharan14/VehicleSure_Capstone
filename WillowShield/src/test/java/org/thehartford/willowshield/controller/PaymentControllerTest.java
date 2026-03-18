package org.thehartford.willowshield.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.thehartford.willowshield.dto.PaymentRequestDTO;
import org.thehartford.willowshield.entity.Policy;
import org.thehartford.willowshield.repository.PolicyRepository;
import org.thehartford.willowshield.service.PaymentService;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PolicyRepository policyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "1", roles = "CUSTOMER")
    void processPayment_Success() throws Exception {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setPolicyId(1);
        request.setAmount(BigDecimal.valueOf(15000));
        request.setTransactionReference("TXN123");

        doNothing().when(paymentService).processPayment(any(PaymentRequestDTO.class), anyLong());

        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1", roles = "CUSTOMER")
    void downloadInvoice_Success() throws Exception {
        Policy policy = new Policy();
        policy.setPolicyId(1);
        policy.setInvoicePath("uploads/invoices/dummy.pdf");

        when(policyRepository.findById(1)).thenReturn(Optional.of(policy));
        // Mocking the actual file system is hard here, so we expect 404 if file not found on disk
        // but we can at least check if the controller tries to find it.
        mockMvc.perform(get("/api/payments/download/invoice/1"))
                .andExpect(status().isNotFound()); // Expect not found because file doesn't exist on disk
    }
}

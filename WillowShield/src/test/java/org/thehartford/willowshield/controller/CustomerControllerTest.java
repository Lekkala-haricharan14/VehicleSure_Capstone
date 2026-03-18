package org.thehartford.willowshield.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.thehartford.willowshield.dto.CreateClaimDTO;
import org.thehartford.willowshield.dto.CreateVehicleApplicationDTO;
import org.thehartford.willowshield.dto.QuoteRequestDTO;
import org.thehartford.willowshield.dto.ReadClaimDTO;
import org.thehartford.willowshield.enums.ClaimType;
import org.thehartford.willowshield.service.ClaimService;
import org.thehartford.willowshield.service.CustomerService;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private ClaimService claimService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "1", roles = "CUSTOMER")
    void buyPolicy_Success() throws Exception {
        CreateVehicleApplicationDTO dto = new CreateVehicleApplicationDTO();
        dto.setRegistrationNumber("DL01AB1234");
        String json = objectMapper.writeValueAsString(dto);
        MockMultipartFile rcDocument = new MockMultipartFile("rcDocument", "rc.pdf", "application/pdf", "content".getBytes());
        MockMultipartFile invoiceDocument = new MockMultipartFile("invoiceDocument", "invoice.pdf", "application/pdf", "content".getBytes());

        mockMvc.perform(multipart("/api/customer/buy-policy")
                .file(rcDocument)
                .file(invoiceDocument)
                .param("application", json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1", roles = "CUSTOMER")
    void getMyApplications_Success() throws Exception {
        when(customerService.getCustomerApplications(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/customer/applications"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1", roles = "CUSTOMER")
    void getQuotes_Success() throws Exception {
        QuoteRequestDTO request = new QuoteRequestDTO();
        request.setYear(2022);
        request.setExShowroomPrice(BigDecimal.valueOf(1000000));

        when(customerService.generateQuotes(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/customer/quote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1", roles = "CUSTOMER")
    void submitClaim_Success() throws Exception {
        CreateClaimDTO dto = new CreateClaimDTO();
        dto.setPolicyId(1);
        dto.setClaimType(ClaimType.DAMAGE);
        String json = objectMapper.writeValueAsString(dto);

        MockMultipartFile doc1 = new MockMultipartFile("doc1", "doc1.pdf", "application/pdf", "content".getBytes());
        MockMultipartFile claimPart = new MockMultipartFile("claim", "", "application/json", json.getBytes());

        when(claimService.submitClaim(anyLong(), any(), any(), any(), any())).thenReturn(new ReadClaimDTO());

        mockMvc.perform(multipart("/api/customer/submit-claim")
                .file(doc1)
                .file(claimPart))
                .andExpect(status().isOk());
    }
}

package org.thehartford.willowshield.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.thehartford.willowshield.dto.ApproveClaimRequest;
import org.thehartford.willowshield.dto.RejectClaimRequest;
import org.thehartford.willowshield.service.ClaimsOfficerService;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class ClaimsOfficerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaimsOfficerService claimsOfficerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "2", roles = "CLAIMS_OFFICER")
    void getAssignedClaims_Success() throws Exception {
        when(claimsOfficerService.getAssignedClaims(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/claims-officer/claims"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "2", roles = "CLAIMS_OFFICER")
    void approveClaim_Success() throws Exception {
        ApproveClaimRequest request = new ApproveClaimRequest();
        request.setBillAmount(BigDecimal.valueOf(5000));

        doNothing().when(claimsOfficerService).approveClaim(anyInt(), anyLong(), any(ApproveClaimRequest.class));

        mockMvc.perform(post("/api/claims-officer/claims/1/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "2", roles = "CLAIMS_OFFICER")
    void calculatePayment_Success() throws Exception {
        ApproveClaimRequest request = new ApproveClaimRequest();
        when(claimsOfficerService.calculatePayment(anyInt(), anyLong(), any(ApproveClaimRequest.class)))
                .thenReturn(BigDecimal.valueOf(4000));

        mockMvc.perform(post("/api/claims-officer/claims/1/payout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("4000"));
    }

    @Test
    @WithMockUser(username = "2", roles = "CLAIMS_OFFICER")
    void rejectClaim_Success() throws Exception {
        RejectClaimRequest request = new RejectClaimRequest();
        request.setReason("Invalid claim");

        doNothing().when(claimsOfficerService).rejectClaim(anyInt(), anyLong(), any());

        mockMvc.perform(post("/api/claims-officer/claims/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}

package org.thehartford.willowshield.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.thehartford.willowshield.dto.CreateStaffDTO;
import org.thehartford.willowshield.dto.ReadStaffDTO;
import org.thehartford.willowshield.enums.UserRole;
import org.thehartford.willowshield.utility.JwtUtility;
import org.thehartford.willowshield.filters.JwtFilter;
import org.thehartford.willowshield.service.AdminService;
import org.thehartford.willowshield.service.ExcelExportService;
import org.thehartford.willowshield.dto.ReadPaymentDTO;
import org.thehartford.willowshield.dto.ReadClaimsPaymentDTO;

import java.io.ByteArrayInputStream;
import java.util.List;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)

public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private ExcelExportService excelExportService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtUtility jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllStaff_Success() throws Exception {
        ReadStaffDTO staff = new ReadStaffDTO();
        staff.setId(1L);
        staff.setUsername("staffuser");
        staff.setRole(UserRole.UNDERWRITER);

        when(adminService.getAllStaff()).thenReturn(List.of(staff));

        mockMvc.perform(get("/api/admin/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("staffuser"));
    }

    @Test
    void createStaff_Success() throws Exception {
        CreateStaffDTO dto = new CreateStaffDTO();
        dto.setUsername("newstaff");
        dto.setRole(UserRole.UNDERWRITER);

        mockMvc.perform(post("/api/admin/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Staff created successfully"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void processClaimPayment_Success() throws Exception {
        mockMvc.perform(post("/api/admin/claims/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Payment processed successfully"));
    }

    @Test
    void exportReceivedPayments_Success() throws Exception {
        when(adminService.getAllReceivedPayments()).thenReturn(List.of(new ReadPaymentDTO()));
        when(excelExportService.exportReceivedPaymentsToExcel(any())).thenReturn(new ByteArrayInputStream("test".getBytes()));

        mockMvc.perform(get("/api/admin/export/received-payments"))
                .andExpect(status().isOk())
                .andExpect(status().isOk());
    }

    @Test
    void exportPayoutHistory_Success() throws Exception {
        when(adminService.getAllClaimPayouts()).thenReturn(List.of(new ReadClaimsPaymentDTO()));
        when(excelExportService.exportPayoutHistoryToExcel(any())).thenReturn(new ByteArrayInputStream("test".getBytes()));

        mockMvc.perform(get("/api/admin/export/payout-history"))
                .andExpect(status().isOk());
    }
}

package org.thehartford.willowshield.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.thehartford.willowshield.service.UnderwriterService;


@SpringBootTest
@AutoConfigureMockMvc
public class UnderwriterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UnderwriterService underwriterService;

    @Test
    @WithMockUser(username = "3", roles = "UNDERWRITER")
    void getAssignedApplications_Success() throws Exception {
        when(underwriterService.getAssignedApplications(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/underwriter/applications"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "3", roles = "UNDERWRITER")
    void approveApplication_Success() throws Exception {
        doNothing().when(underwriterService).approveApplication(anyInt(), anyLong());

        mockMvc.perform(post("/api/underwriter/applications/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "3", roles = "UNDERWRITER")
    void rejectApplication_Success() throws Exception {
        // RejectionRequest is static inner class
        String json = "{\"reason\":\"High risk profile\"}";

        doNothing().when(underwriterService).rejectApplication(anyInt(), anyLong(), anyString());

        mockMvc.perform(post("/api/underwriter/applications/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }
}

package org.thehartford.willowshield.service;

import org.junit.jupiter.api.Test;
import org.thehartford.willowshield.dto.ReadClaimsPaymentDTO;
import org.thehartford.willowshield.dto.ReadPaymentDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExcelExportServiceTest {

    private final ExcelExportService excelExportService = new ExcelExportService();

    @Test
    void exportReceivedPaymentsToExcel_Success() throws IOException {
        ReadPaymentDTO p = new ReadPaymentDTO();
        p.setPaymentId(1);
        p.setPaymentDate(LocalDateTime.now());
        p.setTransactionReference("REF123");
        p.setAmount(BigDecimal.valueOf(100));
        p.setCustomerName("John Doe");

        ByteArrayInputStream result = excelExportService.exportReceivedPaymentsToExcel(List.of(p));

        assertNotNull(result);
        assertTrue(result.available() > 0);
    }

    @Test
    void exportPayoutHistoryToExcel_Success() throws IOException {
        ReadClaimsPaymentDTO p = new ReadClaimsPaymentDTO();
        p.setPaymentId(1);
        p.setPaymentDate(LocalDateTime.now());
        p.setClaimNumber("CLM-123");
        p.setAmountPaid(BigDecimal.valueOf(5000));
        p.setClaimsOfficerName("Officer Jane");

        ByteArrayInputStream result = excelExportService.exportPayoutHistoryToExcel(List.of(p));

        assertNotNull(result);
        assertTrue(result.available() > 0);
    }
}

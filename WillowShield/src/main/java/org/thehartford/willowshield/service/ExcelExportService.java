package org.thehartford.willowshield.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.thehartford.willowshield.dto.ReadClaimsPaymentDTO;
import org.thehartford.willowshield.dto.ReadPaymentDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public ByteArrayInputStream exportReceivedPaymentsToExcel(List<ReadPaymentDTO> payments) throws IOException {
        String[] columns = {"Date", "Transaction Ref", "Policy Number", "Customer Name", "Customer Email", "Vehicle Type", "Policy Type", "Amount", "Status"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Received Premiums");

            // Header Row
            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (ReadPaymentDTO p : payments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getPaymentDate().toString());
                row.createCell(1).setCellValue(p.getTransactionReference());
                row.createCell(2).setCellValue(p.getPolicyNumber());
                row.createCell(3).setCellValue(p.getCustomerName());
                row.createCell(4).setCellValue(p.getCustomerEmail());
                row.createCell(5).setCellValue(p.getVehicleType());
                row.createCell(6).setCellValue(p.getPolicyType());
                row.createCell(7).setCellValue(p.getAmount().doubleValue());
                row.createCell(8).setCellValue(p.getStatus());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream exportPayoutHistoryToExcel(List<ReadClaimsPaymentDTO> payouts) throws IOException {
        String[] columns = {"Date", "Claim Number", "Policy Number", "Customer Name", "Customer Email", "Vehicle Type", "Policy Type", "Amount Paid", "Claims Officer"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Payout History");

            // Header Row
            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (ReadClaimsPaymentDTO p : payouts) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getPaymentDate().toString());
                row.createCell(1).setCellValue(p.getClaimNumber());
                row.createCell(2).setCellValue(p.getPolicyNumber());
                row.createCell(3).setCellValue(p.getCustomerName());
                row.createCell(4).setCellValue(p.getCustomerEmail());
                row.createCell(5).setCellValue(p.getVehicleType());
                row.createCell(6).setCellValue(p.getPolicyType());
                row.createCell(7).setCellValue(p.getAmountPaid().doubleValue());
                row.createCell(8).setCellValue(p.getClaimsOfficerName());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}

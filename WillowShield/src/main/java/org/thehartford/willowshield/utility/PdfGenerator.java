package org.thehartford.willowshield.utility;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import org.thehartford.willowshield.entity.Payment;
import org.thehartford.willowshield.entity.Policy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class PdfGenerator {

        public void generateInvoice(Payment payment, String outputPath) throws IOException {
                ensureDirectoryExists(outputPath);
                Document document = new Document(PageSize.A4);
                try {
                        PdfWriter.getInstance(document, new FileOutputStream(outputPath));
                        document.open();

                        // Colors
                        java.awt.Color indigo = new java.awt.Color(79, 70, 229);

                        // Header - Branding
                        Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, indigo);
                        Paragraph brand = new Paragraph("VehicleSure", brandFont);
                        brand.setAlignment(Element.ALIGN_RIGHT);
                        document.add(brand);

                        Font subLabelFont = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY);
                        Paragraph subLabel = new Paragraph("Secure. Reliable. Instant.", subLabelFont);
                        subLabel.setAlignment(Element.ALIGN_RIGHT);
                        document.add(subLabel);

                        document.add(new Paragraph("\n"));

                        // Title
                        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
                        Paragraph title = new Paragraph("INVOICE", titleFont);
                        document.add(title);
                        document.add(new Paragraph("\n"));

                        // Main Info Table (2 Columns)
                        PdfPTable infoTable = new PdfPTable(2);
                        infoTable.setWidthPercentage(100);
                        infoTable.setSpacingBefore(10f);

                        // Bill To
                        PdfPCell billToCell = new PdfPCell();
                        billToCell.setBorder(Rectangle.NO_BORDER);
                        billToCell.addElement(new Paragraph("BILL TO:",
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.GRAY)));
                        billToCell.addElement(new Paragraph(payment.getPolicy().getCustomer().getUsername(),
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                        billToCell.addElement(new Paragraph(payment.getPolicy().getCustomer().getEmail(),
                                        FontFactory.getFont(FontFactory.HELVETICA, 10)));
                        infoTable.addCell(billToCell);

                        // Invoice Meta
                        PdfPCell metaCell = new PdfPCell();
                        metaCell.setBorder(Rectangle.NO_BORDER);
                        metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        metaCell.addElement(new Paragraph("Invoice ID: #" + payment.getPaymentId(),
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                        metaCell.addElement(new Paragraph("Date: " + payment.getPaymentDate().toLocalDate(),
                                        FontFactory.getFont(FontFactory.HELVETICA, 10)));
                        metaCell.addElement(new Paragraph("Status: " + payment.getStatus(),
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, indigo)));
                        infoTable.addCell(metaCell);

                        document.add(infoTable);
                        document.add(new Paragraph("\n\n"));

                        // Transaction Details Table
                        PdfPTable table = new PdfPTable(2);
                        table.setWidthPercentage(100);
                        table.setSpacingBefore(10f);

                        // Table Header
                        PdfPCell header1 = new PdfPCell(new Paragraph("Description",
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, java.awt.Color.WHITE)));
                        header1.setBackgroundColor(indigo);
                        header1.setPadding(8f);
                        table.addCell(header1);

                        PdfPCell header2 = new PdfPCell(new Paragraph("Details",
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, java.awt.Color.WHITE)));
                        header2.setBackgroundColor(indigo);
                        header2.setPadding(8f);
                        header2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        table.addCell(header2);

                        // Row 1: Policy
                        table.addCell(createStyledCell("Insurance Policy Number", false));
                        table.addCell(createStyledCell(payment.getPolicy().getPolicyNumber(), true));

                        // Row 2: Transaction Ref
                        table.addCell(createStyledCell("Transaction Reference", false));
                        table.addCell(createStyledCell(payment.getTransactionReference(), true));

                        // Row 3: Total
                        PdfPCell totalLabel = createStyledCell("TOTAL AMOUNT", false);
                        totalLabel.setPaddingTop(15f);
                        table.addCell(totalLabel);

                        PdfPCell totalVal = createStyledCell("₹ " + payment.getAmount(), true);
                        totalVal.setPaddingTop(15f);
                        totalVal.setPhrase(new Phrase("₹ " + payment.getAmount(),
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, indigo)));
                        table.addCell(totalVal);

                        document.add(table);

                        document.add(new Paragraph("\n\n\n"));
                        Paragraph footer = new Paragraph(
                                        "Thank you for choosing VehicleSure for your vehicle protection.",
                                        FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, java.awt.Color.GRAY));
                        footer.setAlignment(Element.ALIGN_CENTER);
                        document.add(footer);

                        document.close();
                } catch (DocumentException e) {
                        throw new IOException("Error creating PDF: " + e.getMessage());
                }
        }

        private PdfPCell createStyledCell(String text, boolean alignRight) {
                PdfPCell cell = new PdfPCell(new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 11)));
                cell.setPadding(8f);
                cell.setBorderColor(new java.awt.Color(226, 232, 240));
                if (alignRight) {
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                }
                return cell;
        }

        public void generatePolicyDocument(Policy policy, String outputPath) throws IOException {
                ensureDirectoryExists(outputPath);
                Document document = new Document(PageSize.A4);
                try {
                        PdfWriter.getInstance(document, new FileOutputStream(outputPath));
                        document.open();

                        // Colors
                        java.awt.Color indigo = new java.awt.Color(79, 70, 229);

                        // Header - Branding
                        Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, indigo);
                        Paragraph brand = new Paragraph("VehicleSure", brandFont);
                        brand.setAlignment(Element.ALIGN_RIGHT);
                        document.add(brand);

                        Font subLabelFont = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY);
                        Paragraph subLabel = new Paragraph("Secure. Reliable. Instant.", subLabelFont);
                        subLabel.setAlignment(Element.ALIGN_RIGHT);
                        document.add(subLabel);

                        document.add(new Paragraph("\n"));

                        // Title
                        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
                        Paragraph title = new Paragraph("INSURANCE POLICY SCHEDULE", titleFont);
                        title.setAlignment(Element.ALIGN_CENTER);
                        document.add(title);
                        document.add(new Paragraph("\n"));

                        // 1. Policy Summary Section
                        addSectionTitle(document, "1. POLICY SUMMARY", indigo);
                        PdfPTable summaryTable = new PdfPTable(2);
                        summaryTable.setWidthPercentage(100);

                        summaryTable.addCell(createStyledCell("Policy Number", false));
                        summaryTable.addCell(createStyledCell(policy.getPolicyNumber(), true));

                        summaryTable.addCell(createStyledCell("Policy Period", false));
                        summaryTable.addCell(
                                        createStyledCell(policy.getStartDate() + " to " + policy.getEndDate(), true));

                        document.add(summaryTable);
                        document.add(new Paragraph("\n"));

                        // 2. Policy Holder Details
                        addSectionTitle(document, "2. POLICY HOLDER DETAILS", indigo);
                        PdfPTable holderTable = new PdfPTable(2);
                        holderTable.setWidthPercentage(100);

                        holderTable.addCell(createStyledCell("Name", false));
                        holderTable.addCell(createStyledCell(policy.getCustomer().getUsername(), true));

                        holderTable.addCell(createStyledCell("Email Address", false));
                        holderTable.addCell(createStyledCell(policy.getCustomer().getEmail(), true));

                        holderTable.addCell(createStyledCell("Contact Number", false));
                        holderTable.addCell(createStyledCell(policy.getCustomer().getPhoneNumber(), true));

                        document.add(holderTable);
                        document.add(new Paragraph("\n"));

                        // 3. Vehicle Details
                        addSectionTitle(document, "3. VEHICLE DETAILS", indigo);
                        PdfPTable vehicleTable = new PdfPTable(2);
                        vehicleTable.setWidthPercentage(100);

                        vehicleTable.addCell(createStyledCell("Registration Number", false));
                        vehicleTable.addCell(createStyledCell(policy.getVehicle().getRegistrationNumber(), true));

                        vehicleTable.addCell(createStyledCell("Make & Model", false));
                        vehicleTable.addCell(
                                        createStyledCell(policy.getVehicle().getMake() + " "
                                                        + policy.getVehicle().getModel(), true));

                        vehicleTable.addCell(createStyledCell("Year of Manufacture", false));
                        vehicleTable.addCell(createStyledCell(String.valueOf(policy.getVehicle().getYear()), true));

                        vehicleTable.addCell(createStyledCell("Fuel Type", false));
                        vehicleTable.addCell(createStyledCell(policy.getVehicle().getFuelType(), true));

                        document.add(vehicleTable);
                        document.add(new Paragraph("\n"));

                        // 4. Premium & Coverage
                        addSectionTitle(document, "4. PREMIUM & COVERAGE", indigo);
                        PdfPTable premiumTable = new PdfPTable(2);
                        premiumTable.setWidthPercentage(100);

                        premiumTable.addCell(createStyledCell("Plan Selected", false));
                        premiumTable.addCell(createStyledCell(policy.getPlan().getPlanName(), true));

                        premiumTable.addCell(createStyledCell("Insured Declared Value (IDV)", false));
                        premiumTable.addCell(createStyledCell("₹ " + policy.getPlan().getMaxCoverageAmount(), true));

                        premiumTable.addCell(createStyledCell("Net Premium Paid", false));
                        PdfPCell premiumVal = createStyledCell("₹ " + policy.getPremiumAmount(), true);
                        premiumVal.setPhrase(new Phrase("₹ " + policy.getPremiumAmount(),
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, indigo)));
                        premiumTable.addCell(premiumVal);

                        document.add(premiumTable);

                        document.add(new Paragraph("\n\n"));
                        Paragraph disclaimer = new Paragraph(
                                        "This is a computer-generated document and does not require a physical signature. Subject to terms and conditions of VehicleSure General Insurance.",
                                        FontFactory.getFont(FontFactory.HELVETICA, 8, java.awt.Color.GRAY));
                        disclaimer.setAlignment(Element.ALIGN_CENTER);
                        document.add(disclaimer);

                        document.close();
                } catch (DocumentException e) {
                        throw new IOException("Error creating PDF: " + e.getMessage());
                }
        }

        private void addSectionTitle(Document document, String title, java.awt.Color color) throws DocumentException {
                Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, color);
                Paragraph p = new Paragraph(title, sectionFont);
                p.setSpacingAfter(5f);
                document.add(p);
        }

        private void ensureDirectoryExists(String filePath) {
                File file = new File(filePath);
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                }
        }
}

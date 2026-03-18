package org.thehartford.willowshield.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_documents")
@Getter
@Setter
public class VehicleDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer documentId;

    private String rcDocumentPath;
    private String invoiceDocumentPath;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_application_id", nullable = false)
    private VehicleApplication vehicleApplication;

    public String getRcDocumentPath() {
        return rcDocumentPath;
    }

    public void setRcDocumentPath(String rcDocumentPath) {
        this.rcDocumentPath = rcDocumentPath;
    }

    public String getInvoiceDocumentPath() {
        return invoiceDocumentPath;
    }

    public void setInvoiceDocumentPath(String invoiceDocumentPath) {
        this.invoiceDocumentPath = invoiceDocumentPath;
    }
}

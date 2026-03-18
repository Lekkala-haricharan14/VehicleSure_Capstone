package org.thehartford.willowshield.dto;

import lombok.Data;
import java.math.BigDecimal;
import org.thehartford.willowshield.enums.RiskLevel;
import java.util.List;

@Data
public class QuoteResponseDTO {

    private Integer planId;
    private String planName;
    private String policyType;
    private String description;
    
    private BigDecimal basePremium;
    private BigDecimal idv;
    private RiskLevel riskLevel;
    private List<QuoteOptionDTO> options;
}
  
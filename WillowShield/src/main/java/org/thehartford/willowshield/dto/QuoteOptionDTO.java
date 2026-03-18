package org.thehartford.willowshield.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QuoteOptionDTO {
    private Integer tenureYears;
    private BigDecimal calculatedPremium;
}

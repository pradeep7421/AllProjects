package com.winsupply.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * The {@code PromotionRequest} class represents model class
 * @author PRADEEP
 */
@Data
public class PromotionRequest {
    /**
     * The profit Margin Percentage takes values of type double
     */
    @Positive
    @Max(100)
    private Double profitMarginPercentage;

}

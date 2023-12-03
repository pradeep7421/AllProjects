package com.winsupply.builder;

import com.winsupply.response.Data;
import com.winsupply.response.PromotionResponse;
import org.springframework.stereotype.Component;

/**
 * The {@code PromotionResponse} class represents Promotion Response
 * @author PRADEEP
 */
@Component
public class ResponseBuilder {
    /**
     * this method created Promotion Response
     * @param pDiscountAmount- the value requires of type double
     * @return - Returns PromotionRespnse object to controller
     */
    public PromotionResponse createPromotionRespomse(Double pDiscountAmount) {
        Data lData = new Data();
        lData.setFinalDiscountAmount(pDiscountAmount);
        PromotionResponse lPromotionResponse = new PromotionResponse();
        lPromotionResponse.setData(lData);
        return lPromotionResponse;
    }

}

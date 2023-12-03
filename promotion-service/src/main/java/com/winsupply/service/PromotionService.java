package com.winsupply.service;

import com.winsupply.constants.Constants;
import com.winsupply.globalException.OfferNotValidException;
import com.winsupply.model.PromotionRequest;
import java.text.DecimalFormat;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The {@code PromotionService} class provides business logic for calculating
 * order amount
 * @author PRADEEP
 */
@Service
@Getter
public class PromotionService {

    /**
     * mProfitMarginPercentage - the profit margin percentage
     */
    private Double mProfitMarginPercentage = 15d;

    /**
     * mLogger - the Logger
     */
    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    /**
     * It calculates discount amount based on requests coming from different devices
     *
     * @param pOrderAmount - The amount for the order
     * @param pUserAgent   - The value of request header in string
     * @return - Returns the discount amount after rounding off up to two decimal
     *         places
     */
    public Double getDiscountAmount(Double pOrderAmount, String pUserAgent) {

        Double lFinalDiscountAmount;
        DecimalFormat lDecimalFormat = new DecimalFormat("#.##");
        Double lFinalOrderAmount = pOrderAmount + pOrderAmount * (mProfitMarginPercentage / 100);

        if (pUserAgent.contains(Constants.IPHONE) || pUserAgent.contains(Constants.IPAD) || pUserAgent.contains(Constants.MACINTOSH)) {
            Double lDiscountAmount = lFinalOrderAmount * (2.5 / 100);
            lFinalDiscountAmount = Double.parseDouble(lDecimalFormat.format(lDiscountAmount));

        } else if (pUserAgent.contains(Constants.WINDOWS) || pUserAgent.contains(Constants.ANDROID)) {
            Double lDiscountAmount = lFinalOrderAmount * (3.5 / 100);
            lFinalDiscountAmount = Double.parseDouble(lDecimalFormat.format(lDiscountAmount));

        } else {
            throw new OfferNotValidException(Constants.NO_PROPER_DEVICE_FOUND);
        }
        mLogger.debug("exiting from getDiscountAmount method");
        return lFinalDiscountAmount;
    }

    /**
     * It sets the profit margin percentage when it is overridden
     * @param pPromotionRequest - It contains default profit margin percentage for
     *                          the order amount
     * @throws Exception
     */
    public void updateProfitMarginPercentage(PromotionRequest pPromotionRequest) throws Exception {
        mLogger.info("Profit margin Percentage: {},service profit margin percentage: {}", pPromotionRequest.getProfitMarginPercentage(),
                mProfitMarginPercentage);
        if (pPromotionRequest.getProfitMarginPercentage() != null) {
            mProfitMarginPercentage = pPromotionRequest.getProfitMarginPercentage();
        }
        mLogger.info("service profit margin percentage: {}", this.mProfitMarginPercentage);
    }

}

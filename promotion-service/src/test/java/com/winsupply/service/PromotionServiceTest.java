package com.winsupply.service;

import com.winsupply.constants.Constants;
import com.winsupply.globalException.OfferNotValidException;
import com.winsupply.model.PromotionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {
    /**
     * mOrderService - the OrderService
     */
    @InjectMocks
    PromotionService mPromotionService;

    /**
     * This method is tests for iPhone device request
     */
    @Test
    void testCalculateDiscountAmount_forIphone() {
        Double lOrderAmount = 100000d;
        String lUserAgent = Constants.USER_AGENT_IPHONE;
        assertTrue(lUserAgent.contains("iPhone"));
        assertDoesNotThrow(() -> mPromotionService.getDiscountAmount(lOrderAmount, lUserAgent));
    }

    /**
     * This method is tests for iPad device request
     */
    @Test
    void testCalculateDiscountAmount_forIpad() {
        Double lOrderAmount = 100000d;
        String lUserAgent = Constants.USER_AGENT_IPAD;
        assertTrue(lUserAgent.contains("iPad"));
        assertDoesNotThrow(() -> mPromotionService.getDiscountAmount(lOrderAmount, lUserAgent));
    }

    /**
     * This method is tests for Macintosh device request
     */
    @Test
    void testCalculateDiscountAmount_forMacintosh() {
        Double lOrderAmount = 100000d;
        String lUserAgent = Constants.USER_AGENT_MACINTOSH;
        assertTrue(lUserAgent.contains("Macintosh"));
        assertDoesNotThrow(() -> mPromotionService.getDiscountAmount(lOrderAmount, lUserAgent));
    }

    /**
     * This method is tests for windows device request
     */
    @Test
    void testCalculateDiscountAmount_forWindows() {
        Double lOrderAmount = 100000d;
        String lUserAgent = Constants.USER_AGENT_WINDOWS;
        assertTrue(lUserAgent.contains("Windows"));
        assertDoesNotThrow(() -> mPromotionService.getDiscountAmount(lOrderAmount, lUserAgent));
    }

    /**
     * This method is tests for android device request
     */
    @Test
    void testCalculateDiscountAmount_forAndroid() {
        Double lOrderAmount = 100000d;
        String lUserAgent = Constants.USER_AGENT_ANDROID;
        assertTrue(lUserAgent.contains("Android"));
        assertDoesNotThrow(() -> mPromotionService.getDiscountAmount(lOrderAmount, lUserAgent));
    }

    /**
     * This method is tests for exception for other device request
     */
    @Test
    void testCalculateDiscountAmount_ForOther() {
        Double lOrderAmount = 100000d;
        String lUserAgent = Constants.USER_AGENT_OTHER;
        assertFalse(lUserAgent.contains("Android"));
        assertThrows(OfferNotValidException.class, () -> mPromotionService.getDiscountAmount(lOrderAmount, lUserAgent));
    }

    /**
     * This method is tests for valid ProfitMarginPercentage
     */
    @Test
    void testupdateProfitMarginPercentage() throws Exception {
        PromotionRequest lPromotionRequest = new PromotionRequest();
        lPromotionRequest.setProfitMarginPercentage(20d);
        mPromotionService.updateProfitMarginPercentage(lPromotionRequest);
        assertEquals(20.0, mPromotionService.getMProfitMarginPercentage(), 0.001);
    }

    /**
     * This method is tests for null ProfitMarginPercentage in request body
     * @throws Exception
     */
    @Test
    void testupdateProfitMarginPercentage_isNull() throws Exception {
        PromotionRequest lPromotionRequest = new PromotionRequest();
        lPromotionRequest.setProfitMarginPercentage(null);
        mPromotionService.updateProfitMarginPercentage(lPromotionRequest);
        assertEquals(15, mPromotionService.getMProfitMarginPercentage());
    }

}

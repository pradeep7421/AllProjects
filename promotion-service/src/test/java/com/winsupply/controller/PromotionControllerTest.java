package com.winsupply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winsupply.constants.Constants;
import com.winsupply.globalException.OfferNotValidException;
import com.winsupply.model.PromotionRequest;
import com.winsupply.response.SuccessResponse;
import com.winsupply.service.PromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * This is the documentation for the PromotionControllerTest It provides an
 * overview of the class unit Testing
 */
@SpringBootTest
public class PromotionControllerTest {
    /**
     * mPromotionController - the PromotionController
     */
    @InjectMocks
    PromotionController mPromotionController;

    /**
     * mPromotionService - the PromotionService
     */
    @Mock
    PromotionService mPromotionService;

    /**
     * mMockMvc - the MockMvc
     */
    @Mock
    private MockMvc mMockMvc;

    /**
     * mWebApplicationContext - the WebApplicationContext
     */
    @Autowired
    private WebApplicationContext mWebApplicationContext;

    /**
     * mObjectMapper - the ObjectMapper
     */
    ObjectMapper mObjectMapper;

    /**
     * Initiates this method before executing every method
     */
    @BeforeEach
    void init() {
        mMockMvc = MockMvcBuilders.webAppContextSetup(mWebApplicationContext).build();
        mObjectMapper = new ObjectMapper();
    }

    /**
     * This method is tests for iphone request
     */
    @Test
    void testCalculateDiscountAmount_RequestByIPhone() throws Exception {
        String lUserAgent = Constants.USER_AGENT_IPHONE;
        Double ldiscountAmount = 2.78d;
        when(mPromotionService.getDiscountAmount(anyDouble(), anyString())).thenReturn(ldiscountAmount);

        mMockMvc.perform(MockMvcRequestBuilders.get(Constants.POST_MAPPING).queryParam(Constants.QUERY_PARAM_ORDER_AMOUNT, "100").header(Constants.USER_AGENT, lUserAgent)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * This method is tests for iPad request
     */
    @Test
    void testCalculateDiscountAmount_RequestByIPad() throws Exception {
        Double lOrderAmount = 100d;
        String lUserAgent = Constants.IPAD;
        Double ldiscountAmount = 2.78d;
        when(mPromotionService.getDiscountAmount(lOrderAmount, lUserAgent)).thenReturn(ldiscountAmount);

        mMockMvc.perform(MockMvcRequestBuilders.get(Constants.POST_MAPPING).queryParam(Constants.QUERY_PARAM_ORDER_AMOUNT, "100").header(Constants.USER_AGENT, lUserAgent)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * This method is tests for Mac request
     */
    @Test
    void testCalculateDiscountAmount_RequestByMacintosh() throws Exception {
        String lUserAgent = Constants.MACINTOSH;
        Double ldiscountAmount = 2.78d;
        when(mPromotionService.getDiscountAmount(anyDouble(), anyString())).thenReturn(ldiscountAmount);

        mMockMvc.perform(MockMvcRequestBuilders.get(Constants.POST_MAPPING).queryParam(Constants.QUERY_PARAM_ORDER_AMOUNT, "100").header(Constants.USER_AGENT, lUserAgent)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * This method is tests for Windows request
     */
    @Test
    void testCalculateDiscountAmount_RequestByWindows() throws Exception {
        String lUserAgent = Constants.USER_AGENT_WINDOWS;
        Double ldiscountAmount = 2.78d;
        when(mPromotionService.getDiscountAmount(anyDouble(), anyString())).thenReturn(ldiscountAmount);

        mMockMvc.perform(MockMvcRequestBuilders.get(Constants.POST_MAPPING).queryParam(Constants.QUERY_PARAM_ORDER_AMOUNT, "100").header(Constants.USER_AGENT, lUserAgent)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * This method is tests for Android request
     */
    @Test
    void testCalculateDiscountAmount_RequestByAndroid() throws Exception {
        String lUserAgent = Constants.USER_AGENT_ANDROID;
        Double ldiscountAmount = 2.78d;
        when(mPromotionService.getDiscountAmount(anyDouble(), anyString())).thenReturn(ldiscountAmount);

        mMockMvc.perform(MockMvcRequestBuilders.get(Constants.POST_MAPPING).queryParam(Constants.QUERY_PARAM_ORDER_AMOUNT, "100").header(Constants.USER_AGENT, lUserAgent)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * This method is tests for other devices request other than
     * Apple,windows,android
     */
    @Test
    void testCalculateDiscountAmount_RequestByOtherDevice() throws Exception {
        String lUserAgent = Constants.USER_AGENT_OTHER;

        when(mPromotionService.getDiscountAmount(anyDouble(), anyString())).thenThrow(OfferNotValidException.class);

        mMockMvc.perform(MockMvcRequestBuilders.get(Constants.POST_MAPPING).queryParam(Constants.QUERY_PARAM_ORDER_AMOUNT, "100").header(Constants.USER_AGENT, lUserAgent)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    /**
     * This method is tests for updating profit margin percentage
     */
    @Test
    void updateProfitMarginPercentage_shouldUpdateMarginPercentage() throws Exception {
        PromotionRequest lPromotionRequest = new PromotionRequest();
        lPromotionRequest.setProfitMarginPercentage(20.0);

        Mockito.doNothing().when(mPromotionService).updateProfitMarginPercentage(lPromotionRequest);

        ResponseEntity<SuccessResponse> responseEntity = mPromotionController.updateProfitMarginPercentage(lPromotionRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("ProfitMarginPercentage successfully set", responseEntity.getBody().getSuccessMsg());
    }

    @Test
    void updateProfitMarginPercentage_shouldNotUpdateMarginPercentage() throws Exception {
        PromotionRequest lPromotionRequest = new PromotionRequest();
        lPromotionRequest.setProfitMarginPercentage(null);

        Mockito.doNothing().when(mPromotionService).updateProfitMarginPercentage(lPromotionRequest);

        ResponseEntity<SuccessResponse> responseEntity = mPromotionController.updateProfitMarginPercentage(lPromotionRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(Constants.PROFIT_MARGIN_PERCENTAGE_SUCCESSFULLY_SET, responseEntity.getBody().getSuccessMsg());
    }

}

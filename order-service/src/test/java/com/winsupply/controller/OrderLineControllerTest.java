package com.winsupply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winsupply.model.OrderLineRequest;
import com.winsupply.service.OrderLineService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * This is the documentation for the OrderLineControllerTest It provides an
 * overview of the class unit Testing
 */
@SpringBootTest
public class OrderLineControllerTest {

    /**
     * mOrderLineController - the OrderLineController
     */
    @InjectMocks
    OrderLineController mOrderLineController;

    /**
     * mOrderLineService - the OrderLineService
     */
    @Mock
    OrderLineService mOrderLineService;

    /**
     * mObjectMapper - the ObjectMapper
     */
    ObjectMapper mObjectMapper;

    /**
     * mMockMvc - the MockMvc
     */
    @Mock
    MockMvc mMockMvc;

    /**
     * mWebApplicationContext - the WebApplicationContext
     */
    @Autowired
    private WebApplicationContext mWebApplicationContext;

    /**
     * Initiates this method before executing every method
     */
    @BeforeEach
    void init() {
        mMockMvc = MockMvcBuilders.webAppContextSetup(mWebApplicationContext).build();
        mObjectMapper = new ObjectMapper();
    }

    /**
     * Tests a new order Line based on the provided order request with empty body
     */
    @Test
    void testCreateOrderLines() throws Exception {
        Integer lOrderId = 1;
        List<OrderLineRequest> lOrderLinesRequest = new ArrayList<>();
        OrderLineRequest lOrderLineRequest = new OrderLineRequest();
        lOrderLineRequest.setItemName("item a");
        lOrderLineRequest.setQuantity(12);
        lOrderLinesRequest.add(lOrderLineRequest);

        Mockito.doNothing().when(mOrderLineService).createOrderLines(lOrderLinesRequest, lOrderId);

        mMockMvc.perform(MockMvcRequestBuilders.post("/lines").queryParam("orderId", lOrderId.toString())
                .content(mObjectMapper.writeValueAsString(lOrderLinesRequest)).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Tests a new order Line based on the provided order request with invalid body
     */
    @Test
    void testCreateOrderLines_WithInvalidRequestBody() throws Exception {
        Integer lOrderId = 1;
        List<OrderLineRequest> lOrderLinesRequest = new ArrayList<>();

        Mockito.doNothing().when(mOrderLineService).createOrderLines(lOrderLinesRequest, lOrderId);

        mMockMvc.perform(MockMvcRequestBuilders.post("/lines").queryParam("orderId", lOrderId.toString())
                .content(mObjectMapper.writeValueAsString(lOrderLinesRequest)).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}

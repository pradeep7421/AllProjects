package com.winsupply.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winsupply.entity.Order;
import com.winsupply.model.OrderLineRequest;
import com.winsupply.model.OrderRequest;
import com.winsupply.model.response.OrderResponse;
import com.winsupply.model.response.OrderResponseData;
import com.winsupply.service.OrderService;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;

/**
 * This is the documentation for the OrderControllerTest It provides an overview
 * of the class unit Testing
 */
//@ExtendWith(MockitoExtension.class)
@SpringBootTest
class OrderControllerTest {

    /**
     * mOrderController - the OrderController
     */
    @InjectMocks
    OrderController mOrderController;

    /**
     * mOrderService - the OrderService
     */
    @Mock
    OrderService mOrderService;

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
     * Initiates init() before executing every method
     */
    @BeforeEach
    void init() {
        mMockMvc = MockMvcBuilders.webAppContextSetup(mWebApplicationContext).build();
    }

    /**
     * Tests a new order based on the provided order request
     * @throws Exception
     * @throws JsonProcessingException
     */
    @Test
    void testCreateOrder() throws JsonProcessingException, Exception {
        OrderRequest lOrderRequest = new OrderRequest();
        lOrderRequest.setOrderName("new order");
        lOrderRequest.setAmount(10.10);

        List<OrderLineRequest> lOrderLines = new ArrayList<>();
        OrderLineRequest lOrderLineRequest = new OrderLineRequest();
        lOrderLineRequest.setItemName("gold");
        lOrderLineRequest.setQuantity(10);

        lOrderLines.add(lOrderLineRequest);

        lOrderRequest.setOrderLines(lOrderLines);

        Mockito.doNothing().when(mOrderService).createOrder(lOrderRequest);

        mMockMvc.perform(MockMvcRequestBuilders.post("/orders").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(lOrderRequest))).andExpect(MockMvcResultMatchers.status().isCreated());

    }

    /**
     * Tests a new order based on the provided order request and throws Exception if
     * not created
     */
    @Test
    void testCreateOrderWithEmptyFields() throws Exception {
        OrderRequest lOrderRequest = new OrderRequest();
        lOrderRequest.setOrderName("order1 ");
        lOrderRequest.setAmount(10.00);

        Mockito.doNothing().when(mOrderService).createOrder(lOrderRequest);

        mMockMvc.perform(MockMvcRequestBuilders.post("/orders").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(lOrderRequest))).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * This method is tested based on the provided order Id
     */
    @Test
    void testGetOrderDetails() throws Exception {
        int lOrderId = 1;
        OrderResponse lOrderResponse = new OrderResponse();

        when(mOrderService.getOrderDetails(lOrderId)).thenReturn(lOrderResponse);

        mMockMvc.perform(
                MockMvcRequestBuilders.get("/orders/{orderId}", lOrderId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    /**
     * This method is tested based on the provided order Id and throws Exception for
     * incorrect OrderId
     */
    @Test
    void testGetOrderDetailsWithWrongOrderId() throws Exception {
        int lOrderId = 14545;
        OrderResponse lOrderResponse = new OrderResponse();
        when(mOrderService.getOrderDetails(lOrderId)).thenReturn(lOrderResponse);

        mMockMvc.perform(
                MockMvcRequestBuilders.get("/orders/{orderId}", lOrderId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    /**
     * This method tests for correct order Id ,orderLine Id and Quantity
     */
    @Test
    void testUpdateOrderLineQuantity() throws Exception {
        int lOrderId = 1;
        int lOrderLineId = 2202;
        int lQuantity = 3;

        Mockito.doNothing().when(mOrderService).updateOrderLineQuantity(lOrderId, lOrderLineId, lQuantity);
        mMockMvc.perform(
                MockMvcRequestBuilders.put("/orders/{orderId}/orderLines/{orderLineId}/quantity/{quantity}", lOrderId, lOrderLineId, lQuantity))
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    /**
     * Tests update Order Quantity based on the provided order Id ,orderLine Id and
     */
    @Test
    void testUpdateOrderLineQuantityWithInvalidQuantity() throws Exception {
        int lOrderId = 1;
        int lOrderLineId = 2202;
        int lQuantity = 266;

        Mockito.doNothing().when(mOrderService).updateOrderLineQuantity(lOrderId, lOrderLineId, lQuantity);
        mMockMvc.perform(
                MockMvcRequestBuilders.put("/orders/{orderId}/orderLines/{orderLineId}/quantity/{quantity}", lOrderId, lOrderLineId, lQuantity))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

    /**
     * This method tests with invalid OrderId or Invalid OrderLineId
     */
    @Test
    void testUpdateOrderLineQuantityWithInvalidId() throws Exception {
        int lOrderId = 1;
        int lOrderLineId = 22022;
        int lQuantity = 2;

        Mockito.doNothing().when(mOrderService).updateOrderLineQuantity(lOrderId, lOrderLineId, lQuantity);
        mMockMvc.perform(
                MockMvcRequestBuilders.put("/orders/{orderId}/orderLines/{orderLineId}/quantity/{quantity}", lOrderId, lOrderLineId, lQuantity))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    /**
     * This method tests to get all orders on the basis of information provided
     */
    @Test
    void testgetOrdersByPagination() throws Exception {
        Integer lPageNo = 1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderId";
        String lSortOrder = "asc";

        Page<Order> lpageOrder = new PageImpl(new ArrayList<Order>());
        when(mOrderService.getAllOrdersByPagination(lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder)).thenReturn(lpageOrder);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("page", lPageNo.toString());
        map.add("rpp", lResultsPerPage.toString());
        map.add("sortBy", lSortBy);
        map.add("sortOrder", lSortOrder);

        mMockMvc.perform(
                MockMvcRequestBuilders.get("/orders").queryParams(map).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * This method tests to check for exception with invalid details
     */
    @Test
    void testgetOrdersByInvalidPagination() throws Exception {
        Integer lPageNo = 1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderLineId";
        String lSortOrder = "asc";

        when(mOrderService.getAllOrdersByPagination(lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder)).thenThrow(ConstraintViolationException.class);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("page", lPageNo.toString());
        map.add("rpp", lResultsPerPage.toString());
        map.add("sortBy", lSortBy);
        map.add("sortOrder", lSortOrder);

        mMockMvc.perform(
                MockMvcRequestBuilders.get("/orders").queryParams(map).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * This method tests to check wheather all orders is finded with given search
     * term and other fields
     */
    @Test
    void testgetAllOrdersBySearch() throws Exception {
        String lSearchTerm = "up";
        Integer lPageNo = 1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderId";
        String lSortOrder = "asc";

        Page<Order> lpageOrder = new PageImpl(new ArrayList<Order>());
        when(mOrderService.getAllOrdersBySearch(lSearchTerm, lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder)).thenReturn(lpageOrder);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("searchTerm", lSearchTerm);
        map.add("page", lPageNo.toString());
        map.add("rpp", lResultsPerPage.toString());
        map.add("sortBy", lSortBy);
        map.add("sortOrder", lSortOrder);

        mMockMvc.perform(MockMvcRequestBuilders.get("/orders/search").queryParams(map).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * This method tests to check wheather all orders is finded with given search
     * term else throwing exception
     */
    @Test
    void testgetAllOrdersBySearch_InvalidSortBy() throws Exception {
        String lSearchTerm = "1";
        Integer lPageNo = 1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderLineId";
        String lSortOrder = "asc";

        OrderResponseData lOrderResponseData = new OrderResponseData();
        when(mOrderService.getAllOrdersBySearch(lSearchTerm, lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder))
                .thenThrow(ConstraintViolationException.class);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("searchTerm", lSearchTerm);
        map.add("page", lPageNo.toString());
        map.add("rpp", lResultsPerPage.toString());
        map.add("sortBy", lSortBy);
        map.add("sortOrder", lSortOrder);

        mMockMvc.perform(MockMvcRequestBuilders.get("/orders/search").queryParams(map).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}

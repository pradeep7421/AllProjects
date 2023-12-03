package com.winsupply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winsupply.constants.Constants;
import com.winsupply.entity.Order;
import com.winsupply.model.OrderLineRequest;
import com.winsupply.model.OrderRequest;
import com.winsupply.service.OrderService;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
     * Tests a new order based on the provided order request
     * @throws Exception
     */
    @Test
    void testCreateOrder() throws Exception, Exception {
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

        mMockMvc.perform(MockMvcRequestBuilders.post(Constants.POST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(mObjectMapper.writeValueAsString(lOrderRequest))).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    /**
     * Tests a new order based on the provided empty or invalid order request and
     * throws Exception if not created
     */
    @Test
    void testCreateOrder_WithEmptyOrInvalidOrderFields() throws Exception {
        OrderRequest lOrderRequest = new OrderRequest();

        List<OrderLineRequest> lOrderLinesRequest = new ArrayList<OrderLineRequest>();
        OrderLineRequest lOrderLineRequest = new OrderLineRequest();
        lOrderLineRequest.setItemName("Diamond");
        lOrderLineRequest.setQuantity(20);
        lOrderLinesRequest.add(lOrderLineRequest);

        lOrderRequest.setOrderLines(lOrderLinesRequest);

        Mockito.doNothing().when(mOrderService).createOrder(lOrderRequest);

        mMockMvc.perform(MockMvcRequestBuilders.post(Constants.POST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(mObjectMapper.writeValueAsString(lOrderRequest))).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * Tests a new order based on the provided with empty or invalid order Line
     * request and throws Exception if not created
     */
    @Test
    void testCreateOrder_WithEmptyOrInvalidOrderLinesFields() throws Exception {
        OrderRequest lOrderRequest = new OrderRequest();
        lOrderRequest.setOrderName("order1 ");
        lOrderRequest.setAmount(10.00);
        lOrderRequest.setOrderLines(new ArrayList<OrderLineRequest>());

        Mockito.doNothing().when(mOrderService).createOrder(lOrderRequest);

        mMockMvc.perform(MockMvcRequestBuilders.post(Constants.POST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(mObjectMapper.writeValueAsString(lOrderRequest))).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * This method is tested based on the provided order Id
     */
    @Test
    void testGetOrderDetails() throws Exception {
        int lOrderId = 1;
        String lUserAgent = "Mozilla/5.0 (iPhone; U; ru; CPU iPhone OS 4_2_1 like Mac OS X; ru) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148a Safari/6533.18.5";

        when(mOrderService.getOrderDetails(lOrderId, lUserAgent)).thenReturn(Optional.of(new Order()));

        mMockMvc.perform(MockMvcRequestBuilders.get(Constants.GET_URL, lOrderId).header(Constants.USER_AGENT, lUserAgent)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * This method is tested based on the provided order Id and throws Exception for
     * incorrect OrderId
     */
    @Test
    void testGetOrderDetails_WithWrongOrderId() throws Exception {
        int lOrderId = 14545;
        String lUserAgent = "Mozilla/5.0 (iPhone; U; ru; CPU iPhone OS 4_2_1 like Mac OS X; ru) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148a Safari/6533.18.5";

        when(mOrderService.getOrderDetails(lOrderId, lUserAgent)).thenReturn(Optional.empty());

        mMockMvc.perform(MockMvcRequestBuilders.get("/orders/{orderId}", lOrderId).header(Constants.USER_AGENT, lUserAgent)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isNotFound());
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
    void testUpdateOrderLineQuantity_WithInvalidQuantity() throws Exception {
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
    void testUpdateOrderLineQuantity_WithInvalidOrderLineId() throws Exception {
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

        Page<Order> lpageOrder = new PageImpl<>(new ArrayList<Order>());
        when(mOrderService.getAllOrdersByPagination(lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder)).thenReturn(lpageOrder);
        MultiValueMap<String, String> lParam = new LinkedMultiValueMap<>();
        lParam.add("page", lPageNo.toString());
        lParam.add("rpp", lResultsPerPage.toString());
        lParam.add("sortBy", lSortBy);
        lParam.add("sortOrder", lSortOrder);

        mMockMvc.perform(
                MockMvcRequestBuilders.get("/orders").queryParams(lParam).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * This method tests to check for exception with invalid page number
     */
    @Test
    void testgetOrdersByPagination_InvalidPageNo() throws Exception {
        Integer lPageNo = -1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderId";
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
     * This method tests to check for exception with invalid results per page
     */
    @Test
    void testgetOrdersByPagination_InvalidResultsPerPage() throws Exception {
        Integer lPageNo = 1;
        Integer lResultsPerPage = -2;
        String lSortBy = "orderId";
        String lSortOrder = "asc";

        when(mOrderService.getAllOrdersByPagination(lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder)).thenThrow(ConstraintViolationException.class);
        MultiValueMap<String, String> lParam = new LinkedMultiValueMap<>();
        lParam.add("page", lPageNo.toString());
        lParam.add("rpp", lResultsPerPage.toString());
        lParam.add("sortBy", lSortBy);
        lParam.add("sortOrder", lSortOrder);

        mMockMvc.perform(
                MockMvcRequestBuilders.get("/orders").queryParams(lParam).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * This method tests to check for exception with invalid sort by
     */
    @Test
    void testgetOrdersByPagination_InvalidSortBy() throws Exception {
        Integer lPageNo = 1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderLineId";
        String lSortOrder = "asc";

        when(mOrderService.getAllOrdersByPagination(lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder)).thenThrow(ConstraintViolationException.class);
        MultiValueMap<String, String> lParam = new LinkedMultiValueMap<>();
        lParam.add("page", lPageNo.toString());
        lParam.add("rpp", lResultsPerPage.toString());
        lParam.add("sortBy", lSortBy);
        lParam.add("sortOrder", lSortOrder);

        mMockMvc.perform(
                MockMvcRequestBuilders.get("/orders").queryParams(lParam).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * This method tests to check for exception with invalid sort order
     */
    @Test
    void testgetOrdersByPagination_InvalidSortOrder() throws Exception {
        Integer lPageNo = 1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderLineId";
        String lSortOrder = "increasing";

        when(mOrderService.getAllOrdersByPagination(lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder)).thenThrow(ConstraintViolationException.class);
        MultiValueMap<String, String> mlParam = new LinkedMultiValueMap<>();
        mlParam.add("page", lPageNo.toString());
        mlParam.add("rpp", lResultsPerPage.toString());
        mlParam.add("sortBy", lSortBy);
        mlParam.add("sortOrder", lSortOrder);

        mMockMvc.perform(
                MockMvcRequestBuilders.get("/orders").queryParams(mlParam).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * This method tests to check whether all orders is founded with given search
     * term
     */
    @Test
    void testgetAllOrdersBySearch() throws Exception {
        String lSearchTerm = "up";
        Integer lPageNo = 1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderId";
        String lSortOrder = "asc";

        Page<Order> lpageOrder = new PageImpl<>(new ArrayList<Order>());
        when(mOrderService.getAllOrdersBySearch(lSearchTerm, lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder)).thenReturn(lpageOrder);
        MultiValueMap<String, String> lParam = new LinkedMultiValueMap<>();
        lParam.add("searchTerm", lSearchTerm);
        lParam.add("page", lPageNo.toString());
        lParam.add("rpp", lResultsPerPage.toString());
        lParam.add("sortBy", lSortBy);
        lParam.add("sortOrder", lSortOrder);

        mMockMvc.perform(MockMvcRequestBuilders.get("/orders/search").queryParams(lParam).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * This method tests to get all orders with given blank search term and throwing
     * exception
     */
    @Test
    void testGetAllOrdersBySearch_WithBlankSearchTerm() throws Exception {
        String lSearchTerm = "";
        Integer lPageNo = 1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderId";
        String lSortOrder = "asc";

        when(mOrderService.getAllOrdersBySearch(lSearchTerm, lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder))
                .thenThrow(ConstraintViolationException.class);
        MultiValueMap<String, String> lParam = new LinkedMultiValueMap<>();
        lParam.add("searchTerm", lSearchTerm);
        lParam.add("page", lPageNo.toString());
        lParam.add("rpp", lResultsPerPage.toString());
        lParam.add("sortBy", lSortBy);
        lParam.add("sortOrder", lSortOrder);

        mMockMvc.perform(MockMvcRequestBuilders.get("/orders/search").queryParams(lParam).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * This method tests to get all orders with invalid page no and throwing
     * exception
     */
    @Test
    void testGetAllOrdersBySearch_WithInvalidPageNo() throws Exception {
        String lSearchTerm = "1";
        Integer lPageNo = -1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderId";
        String lSortOrder = "asc";

        when(mOrderService.getAllOrdersBySearch(lSearchTerm, lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder))
                .thenThrow(ConstraintViolationException.class);
        MultiValueMap<String, String> lParam = new LinkedMultiValueMap<>();
        lParam.add("searchTerm", lSearchTerm);
        lParam.add("page", lPageNo.toString());
        lParam.add("rpp", lResultsPerPage.toString());
        lParam.add("sortBy", lSortBy);
        lParam.add("sortOrder", lSortOrder);

        mMockMvc.perform(MockMvcRequestBuilders.get("/orders/search").queryParams(lParam).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * This method tests to get all orders with invalid results per page and
     * throwing exception
     */
    @Test
    void testGetAllOrdersBySearch_WithInvalidResultsPerPage() throws Exception {
        String lSearchTerm = "1";
        Integer lPageNo = 1;
        Integer lResultsPerPage = -2;
        String lSortBy = "orderId";
        String lSortOrder = "asc";

        when(mOrderService.getAllOrdersBySearch(lSearchTerm, lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder))
                .thenThrow(ConstraintViolationException.class);
        MultiValueMap<String, String> lParam = new LinkedMultiValueMap<>();
        lParam.add("searchTerm", lSearchTerm);
        lParam.add("page", lPageNo.toString());
        lParam.add("rpp", lResultsPerPage.toString());
        lParam.add("sortBy", lSortBy);
        lParam.add("sortOrder", lSortOrder);

        mMockMvc.perform(MockMvcRequestBuilders.get("/orders/search").queryParams(lParam).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * This method tests to get all orders with invalid sort by and throwing
     * exception
     */
    @Test
    void testGetAllOrdersBySearch_WithInvalidSortBy() throws Exception {
        String lSearchTerm = "1";
        Integer lPageNo = 1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderLineId";
        String lSortOrder = "asc";

        when(mOrderService.getAllOrdersBySearch(lSearchTerm, lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder))
                .thenThrow(ConstraintViolationException.class);
        MultiValueMap<String, String> lParam = new LinkedMultiValueMap<>();
        lParam.add("searchTerm", lSearchTerm);
        lParam.add("page", lPageNo.toString());
        lParam.add("rpp", lResultsPerPage.toString());
        lParam.add("sortBy", lSortBy);
        lParam.add("sortOrder", lSortOrder);

        mMockMvc.perform(MockMvcRequestBuilders.get("/orders/search").queryParams(lParam).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * This method tests to get all orders with invalid sort order and throwing
     * exception
     */
    @Test
    void testGetAllOrdersBySearch_WithInvalidSortOrder() throws Exception {
        String lSearchTerm = "1";
        Integer lPageNo = 1;
        Integer lResultsPerPage = 2;
        String lSortBy = "orderId";
        String lSortOrder = "increasing";

        when(mOrderService.getAllOrdersBySearch(lSearchTerm, lPageNo - 1, lResultsPerPage, lSortBy, lSortOrder))
                .thenThrow(ConstraintViolationException.class);
        MultiValueMap<String, String> lParam = new LinkedMultiValueMap<>();
        lParam.add("searchTerm", lSearchTerm);
        lParam.add("page", lPageNo.toString());
        lParam.add("rpp", lResultsPerPage.toString());
        lParam.add("sortBy", lSortBy);
        lParam.add("sortOrder", lSortOrder);

        mMockMvc.perform(MockMvcRequestBuilders.get("/orders/search").queryParams(lParam).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}

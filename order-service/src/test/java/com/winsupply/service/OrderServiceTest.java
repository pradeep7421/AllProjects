package com.winsupply.service;

import com.winsupply.entity.Order;
import com.winsupply.entity.OrderLine;
import com.winsupply.globalexception.DataNotFoundException;
import com.winsupply.model.OrderLineRequest;
import com.winsupply.model.OrderRequest;
import com.winsupply.repository.OrderLineRepository;
import com.winsupply.repository.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This is the documentation for the OrderServiceTest It provides an overview of
 * the class service unit Testing
 */
@SpringBootTest
public class OrderServiceTest {
    /**
     * mOrderService - the OrderService
     */
    @InjectMocks
    OrderService mOrderService;

    /**
     * mOrderRepository - the OrderRepository
     */
    @Mock
    OrderRepository mOrderRepository;

    /**
     * mOrderLineRepository - the OrderLineRepository
     */
    @Mock
    OrderLineRepository mOrderLineRepository;

    /**
     * Tests a new order based on the provided order request
     */
    @Test
    void testCreateOrder() throws Exception {
        OrderRequest lOrderRequest = new OrderRequest();
        lOrderRequest.setAmount(10000.00);
        lOrderRequest.setOrderName("ravi order");

        List<OrderLineRequest> lOrderLinesRequest = new ArrayList<OrderLineRequest>();
        OrderLineRequest lOrderLineRequest = new OrderLineRequest();
        lOrderLineRequest.setItemName("mobile");
        lOrderLineRequest.setQuantity(10);
        lOrderLinesRequest.add(lOrderLineRequest);

        lOrderRequest.setOrderLines(lOrderLinesRequest);

        Order lOrder = new Order();
        lOrder.setOrderName(lOrderRequest.getOrderName());
        lOrder.setAmount(lOrderRequest.getAmount());

        List<OrderLine> lOrderLines = new ArrayList<>();
        for (OrderLineRequest orderLineRequest : lOrderRequest.getOrderLines()) {
            OrderLine lOrderLine = new OrderLine();
            lOrderLine.setItemName(orderLineRequest.getItemName());
            lOrderLine.setQuantity(orderLineRequest.getQuantity());
            lOrderLine.setOrder(lOrder);
            lOrderLines.add(lOrderLine);
        }
        lOrder.setOrderLines(lOrderLines);

        when(mOrderRepository.save(lOrder)).thenReturn(lOrder);
        when(mOrderLineRepository.saveAll(lOrderLines)).thenReturn(lOrderLines);

        mOrderService.createOrder(lOrderRequest);
        verify(mOrderRepository).save(Mockito.any(Order.class));
        verify(mOrderLineRepository).saveAll(Mockito.anyList());
        assertEquals(10000.00, lOrder.getAmount());

    }

    /**
     * Tests create order based on the provided null order request
     */
    @Test
    void testCreateOrder_NullOrderRequest() throws Exception {

        assertThrows(Exception.class, () -> mOrderService.createOrder(null));
        verify(mOrderRepository, never()).save(Mockito.any(Order.class));
        verify(mOrderLineRepository, never()).saveAll(Mockito.anyList());

    }

    /**
     * Tests create order based on the provided null order line request
     */
    @Test
    void testCreateOrder_NullOrderLineRequest() throws Exception {
        OrderRequest lOrderRequest = new OrderRequest();
        assertThrows(Exception.class, () -> mOrderService.createOrder(lOrderRequest));
        verify(mOrderRepository, never()).save(Mockito.any(Order.class));
        verify(mOrderLineRepository, never()).saveAll(Mockito.anyList());

    }

    /**
     * tests wheather order is not successfully created then throws exception
     */
    @Test
    void testCreateOrderWithEmptyOrderLineFields() {
        OrderRequest lOrderRequest = new OrderRequest();
        List<OrderLineRequest> lOrderLinesRequest = new ArrayList<>();
        OrderLineRequest lOrderLineRequest = new OrderLineRequest();
        lOrderLinesRequest.add(lOrderLineRequest);
        lOrderRequest.setOrderLines(lOrderLinesRequest);

        assertThrows(NullPointerException.class, () -> {
            mOrderService.createOrder(lOrderRequest);
        });
        verify(mOrderRepository, never()).save(Mockito.any(Order.class));
        verify(mOrderLineRepository, never()).saveAll(Mockito.anyList());
    }

    /**
     * tests testGetOrderDetails method
     */
    @Test
    void testGetOrderDetails() {
        int lOrderId = 1;

        Order lOrder = new Order();
        lOrder.setOrderId(lOrderId);

        lOrder.setAmount(100000);
        lOrder.setOrderName("order abc");
        List<OrderLine> lOrderLines = new ArrayList<>();

        OrderLine lOrderLine = new OrderLine();
        lOrderLine.setOrderLineId(1001);

        lOrderLine.setItemName("item a");
        lOrderLine.setQuantity(5);
        lOrderLine.setOrder(lOrder);

        lOrderLines.add(lOrderLine);

        lOrder.setOrderLines(lOrderLines);

        when(mOrderRepository.findById(1)).thenReturn(Optional.of(lOrder));

        mOrderService.getOrderDetails(lOrderId);
        verify(mOrderRepository, times(1)).findById(lOrderId);
        assertEquals(100000, lOrder.getAmount());
    }

    /**
     * tests When Order Does Not Exist and throws exception
     */
    @Test()
    public void testGetOrderDetailsWhenOrderDoesNotExist() {

        int lOrderId = 12;

        when(mOrderRepository.findById(lOrderId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> {
            mOrderService.getOrderDetails(lOrderId);
        });

    }

    /**
     * Tests if it Updates the quantity of an order line
     */
    @Test
    public void testUpdateOrderLineQuantitySuccess() {

        int lOrderId = 1;
        int lOrderLineId = 1;
        int lNewQuantity = 5;

        OrderLine lExistingOrderLine = new OrderLine();
        lExistingOrderLine.setOrderLineId(lOrderLineId);
        lExistingOrderLine.setQuantity(3);

        when(mOrderLineRepository.findByOrderLineIdAndOrderOrderId(lOrderLineId, lOrderId)).thenReturn(Optional.of(lExistingOrderLine));

        mOrderService.updateOrderLineQuantity(lOrderId, lOrderLineId, lNewQuantity);

        verify(mOrderLineRepository, Mockito.times(1)).save(lExistingOrderLine);
    }

    /**
     * Tests if it Updates the quantity of an order line for wrong orderId or
     * OrderLine Id
     */
    @Test()
    public void testUpdateOrderLineQuantityOrderLineNotFound() {
        int lOrderId = 1;
        int lOrderLineId = 112;
        int lQuantity = 5;

        Mockito.when(mOrderLineRepository.findByOrderLineIdAndOrderOrderId(lOrderLineId, lOrderId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> {
            mOrderService.updateOrderLineQuantity(lOrderId, lOrderLineId, lQuantity);
        });

    }

    /**
     * Tests if it Updates the quantity of an order line for invalid quantity
     */
    @Test()
    public void testUpdateOrderLineQuantityInvalid() {
        int lOrderId = 1;
        int lOrderLineId = 2202;
        int lQuantity = 55;
        Order lOrder = new Order();
        lOrder.setOrderId(lOrderId);

        lOrder.setAmount(100000);
        lOrder.setOrderName("order abc");
        List<OrderLine> lOrderLines = new ArrayList<>();

        OrderLine lOrderLine = new OrderLine();
        lOrderLine.setOrderLineId(lOrderLineId);

        lOrderLine.setItemName("item a");
        lOrderLine.setQuantity(lQuantity);
        lOrderLine.setOrder(lOrder);

        lOrderLines.add(lOrderLine);

        lOrder.setOrderLines(lOrderLines);

        Mockito.when(mOrderLineRepository.findByOrderLineIdAndOrderOrderId(lOrderLineId, lOrderId)).thenReturn(Optional.of(lOrderLine));

        mOrderService.updateOrderLineQuantity(lOrderId, lOrderLineId, lQuantity);

        verify(mOrderLineRepository).save(lOrderLine);

    }

    /**
     * Tests if it gets all orders by pagination
     */
    @Test()
    public void testGetOrdersByPagination_withAsc() {
        int lPageNo = 1;
        int lResultsPerPage = 2;
        String lSortBy = "amount";
        String lSortOrder = "asc";

        Sort lSort = Sort.by(lSortBy).ascending();
        List<Order> lOrders = new ArrayList<>();
        Order lOrder = new Order();
        lOrder.setAmount(10.10);
        lOrder.setOrderId(100);
        lOrder.setOrderName("Order a");
        lOrders.add(lOrder);
        Pageable lPageable = PageRequest.of(lPageNo, lResultsPerPage, lSort);

        Page<Order> lpageOrder = new PageImpl(lOrders);

        when(mOrderRepository.findAll(lPageable)).thenReturn(lpageOrder);
        assertEquals(1, lpageOrder.getTotalElements());

        mOrderService.getAllOrdersByPagination(lPageNo, lResultsPerPage, lSortBy, lSortOrder);
        verify(mOrderRepository, times(1)).findAll(lPageable);
    }

    /**
     * Tests if it gets all orders by pagination with order by desc
     */
    @Test()
    public void testGetAllOrdersByPagination_withDesc() {
        int lPageNo = 1;
        int lResultsPerPage = 2;
        String lSortBy = "amount";
        String lSortOrder = "desc";

        Sort lSort = Sort.by(lSortBy).descending();
        List<Order> lOrders = new ArrayList<>();
        Order lOrder = new Order();
        lOrder.setAmount(1021.10);
        lOrder.setOrderId(10650);
        lOrder.setOrderName("Order b");
        lOrders.add(lOrder);
        Pageable lPageable = PageRequest.of(lPageNo, lResultsPerPage, lSort);

        Page<Order> lpageOrder = new PageImpl(lOrders);

        when(mOrderRepository.findAll(lPageable)).thenReturn(lpageOrder);
        assertEquals(1, lpageOrder.getTotalElements());

        mOrderService.getAllOrdersByPagination(lPageNo, lResultsPerPage, lSortBy, lSortOrder);
        verify(mOrderRepository, times(1)).findAll(lPageable);
    }

    /**
     * Tests if it gets all orders by pagination or throws exception
     */
    @Test()
    public void testGetOrdersByPagination_throwsExceptions() {
        int lPageNo = 155553;
        int lResultsPerPage = 2;
        String lSortBy = "amount";
        String lSortOrder = "asc";

        Sort lSort = Sort.by(lSortBy).ascending();

        Pageable lPageable = PageRequest.of(lPageNo, lResultsPerPage, lSort);

        Page<Order> lpageOrder = new PageImpl(new ArrayList<Order>());

        when(mOrderRepository.findAll(lPageable)).thenReturn(lpageOrder);
        assertDoesNotThrow(() -> mOrderService.getAllOrdersByPagination(lPageNo, lResultsPerPage, lSortBy, lSortOrder));

    }

    /**
     * Tests if it gets all orders by search
     */
    @Test()
    public void testGetOrdersBySearch() {
        String lSearchTerm = "1";
        int lPageNo = 1;
        int lResultsPerPage = 2;
        String lSortBy = "amount";
        String lSortOrder = "asc";

        Sort lSort = Sort.by(lSortBy).ascending();
        List<Order> lOrders = new ArrayList<>();
        Order lOrder = new Order();
        lOrder.setAmount(10.10);
        lOrder.setOrderId(100);
        lOrder.setOrderName("Order a");
        lOrders.add(lOrder);
        Pageable lPageable = PageRequest.of(lPageNo, lResultsPerPage, lSort);

        Page<Order> lpageOrder = new PageImpl(lOrders);

        when(mOrderRepository.findAllOrderBySearchTerm(lSearchTerm, lPageable)).thenReturn(lpageOrder);
        assertEquals(1, lpageOrder.getTotalElements());

        mOrderService.getAllOrdersBySearch(lSearchTerm, lPageNo, lResultsPerPage, lSortBy, lSortOrder);
        verify(mOrderRepository, times(1)).findAllOrderBySearchTerm(lSearchTerm, lPageable);
    }

    /**
     * Tests if it gets all orders by search
     */
    @Test()
    public void testGetOrdersBySearch_noOrdersSearched() {
        String lSearchTerm = "1";
        int lPageNo = 1;
        int lResultsPerPage = 2;
        String lSortBy = "amount";
        String lSortOrder = "asc";

        Sort lSort = Sort.by(lSortBy).ascending();
        List<Order> lOrders = new ArrayList<>();

        Pageable lPageable = PageRequest.of(lPageNo, lResultsPerPage, lSort);

        when(mOrderRepository.findAllOrderBySearchTerm(lSearchTerm, lPageable)).thenReturn(Page.empty());

        Assertions.assertDoesNotThrow(() -> mOrderService.getAllOrdersBySearch(lSearchTerm, lPageNo, lResultsPerPage, lSortBy, lSortOrder));
    }
}

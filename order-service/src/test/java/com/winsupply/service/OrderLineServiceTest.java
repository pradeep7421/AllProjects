package com.winsupply.service;

import com.winsupply.entity.Order;
import com.winsupply.globalexception.DataNotFoundException;
import com.winsupply.model.OrderLineRequest;
import com.winsupply.repository.OrderLineRepository;
import com.winsupply.repository.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This is the documentation for the OrderLineServiceTest It provides an
 * overview of the class service unit Testing
 */
@ExtendWith(MockitoExtension.class)
public class OrderLineServiceTest {

    /**
     * mOrderLineService - the OrderLineService
     */
    @InjectMocks
    OrderLineService mOrderLineService;

    /**
     * mOrderLineRepository - the OrderLineRepository
     */
    @Mock
    OrderLineRepository mOrderLineRepository;

    @Mock
    OrderRepository mOrderRepository;

    /**
     * Tests a new order based on the provided with valid order request
     */
    @Test
    void testCreateOrderLinesWithValidData() {
        Integer lOrderId = 1;
        Integer lMaxOrderSequence = 3;

        Order lOrder = createOrder();

        List<OrderLineRequest> lOrderLinesRequest = new ArrayList<>();

        OrderLineRequest lOrderLineRequest = createValidOrderLineRequest();
        lOrderLinesRequest.add(lOrderLineRequest);

        when(mOrderRepository.findById(lOrderId)).thenReturn(Optional.of(lOrder));
        when(mOrderLineRepository.getMaxOrderSequenceByOrderId(lOrderId)).thenReturn(lMaxOrderSequence);

        mOrderLineService.createOrderLines(lOrderLinesRequest, lOrderId);

        verify(mOrderRepository, times(1)).findById(lOrderId);
        verify(mOrderLineRepository, times(1)).getMaxOrderSequenceByOrderId(lOrderId);
        verify(mOrderLineRepository, times(1)).saveAll(any());
        assertDoesNotThrow(() -> mOrderLineService.createOrderLines(lOrderLinesRequest, lOrderId));

    }

    private Order createOrder() {
        Order lOrder = new Order();
        lOrder.setOrderId(1);
        lOrder.setAmount(1000);
        lOrder.setOrderName("order a");
        return lOrder;
    }

    /**
     * returns List<OrderLineRequest> when called by
     * testCreateOrderLinesWithDataNotFoundException
     */
    private OrderLineRequest createValidOrderLineRequest() {
        OrderLineRequest lOrderLineRequest = new OrderLineRequest();

        lOrderLineRequest.setItemName("item A");
        lOrderLineRequest.setQuantity(20);

        return lOrderLineRequest;
    }

    /**
     * Tests a new order based on the provided with Invalid order request
     */
    @Test
    void testCreateOrderLinesWithWrongOrderId() {
        Integer lOrderId = 155555; // invalid lOrderId

        List<OrderLineRequest> orderLinesRequest = new ArrayList<>();
        OrderLineRequest lOrderLineRequest = new OrderLineRequest();
        lOrderLineRequest.setItemName("item a");
        lOrderLineRequest.setQuantity(10);
        orderLinesRequest.add(lOrderLineRequest);

        when(mOrderRepository.findById(lOrderId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> mOrderLineService.createOrderLines(orderLinesRequest, lOrderId));
    }

}

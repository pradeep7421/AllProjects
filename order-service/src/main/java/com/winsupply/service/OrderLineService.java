package com.winsupply.service;

import com.winsupply.entity.Order;
import com.winsupply.entity.OrderLine;
import com.winsupply.globalexception.DataNotFoundException;
import com.winsupply.model.OrderLineRequest;
import com.winsupply.repository.OrderLineRepository;
import com.winsupply.repository.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * The {@code OrderService} class provides business logic for managing order line
 * @author PRADEEP
 */
@Service
public class OrderLineService {
    /**
     * mOrderLineRepository - the OrderLineRepository
     */
    private OrderLineRepository mOrderLineRepository;

    /**
     * mOrderRepository - the OrderRepository
     */
    private OrderRepository mOrderRepository;

    /**
     * Creates a new instance of the OrderService
     *
     * @param - pOrderLineRepository The repository for accessing order lines
     */
    public OrderLineService(OrderLineRepository pOrderLineRepository, OrderRepository pOrderRepository) {
        this.mOrderLineRepository = pOrderLineRepository;
        this.mOrderRepository = pOrderRepository;
    }

    /**
     * Creates a new orderLine based on the provided order Lines request
     *
     * @param pOrderLinesRequest - The request containing List of orderLine
     *                           information
     * @param pOrderId           - The unique value of the order
     */
    public void createOrderLines(List<OrderLineRequest> pOrderLinesRequest, Integer pOrderId) {
        // Integer maxOrderSequence =
        // mOrderLineRepository.getMaxOrderSequence(pOrderId);

        Optional<Order> lOrderOptional = mOrderRepository.findById(pOrderId);
        if (lOrderOptional.isPresent()) {
            Integer lMaxOrderSequence = mOrderLineRepository.getMaxOrderSequenceByOrderId(pOrderId);
            List<OrderLine> lOrderLines = new ArrayList<>();
            for (OrderLineRequest lOrderLineRequest : pOrderLinesRequest) {
                OrderLine lOrderLine = createOrderLine(lMaxOrderSequence, lOrderOptional, lOrderLineRequest);
                lMaxOrderSequence++;
                lOrderLines.add(lOrderLine);
            }
            mOrderLineRepository.saveAll(lOrderLines);

        } else {
            throw new DataNotFoundException("Order Details not found");
        }
    }

    /**
     * Creates a new orderLine based on the provided order Lines request
     *
     * @param pMaxOrderSequence - The maximum order Sequence from orderLine table
     * @param pOrderOptional    - The Optional Object of Order type
     * @param pOrderLineRequest - The OrderLineRequest to map with OrderLine fields
     * @return - Returns OrderLine Object
     */
    private OrderLine createOrderLine(Integer pMaxOrderSequence, Optional<Order> pOrderOptional, OrderLineRequest pOrderLineRequest) {
        OrderLine lOrderLine = new OrderLine();
        lOrderLine.setItemName(pOrderLineRequest.getItemName());
        lOrderLine.setQuantity(pOrderLineRequest.getQuantity());
        lOrderLine.setOrderSequence(pMaxOrderSequence + 1);
        lOrderLine.setOrder(pOrderOptional.get());
        return lOrderLine;
    }

}

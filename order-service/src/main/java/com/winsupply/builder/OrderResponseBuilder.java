package com.winsupply.builder;

import com.winsupply.entity.Order;
import com.winsupply.entity.OrderLine;
import com.winsupply.model.response.Meta;
import com.winsupply.model.response.MetaData;
import com.winsupply.model.response.OrderLineResponse;
import com.winsupply.model.response.OrderResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * The response builder responsible for building the response
 */
@Component
public class OrderResponseBuilder {

    /**
     * creates order response data summary from list of orders
     *
     * @param pOrderList       - A list of Order entities to be converted into
     *                         OrderResponse
     * @param pTotalOrderCount - total order count for orders
     * @return - A list of OrderLineResponse representing the summary details of
     *         each order line
     */
    public MetaData createMetaDataByPagination(Page<Order> pPageOrder) {
        Long lTotalOrderCount = pPageOrder.getTotalElements();
        List<Order> lOrderList = pPageOrder.getContent();
        List<OrderResponse> lOrderResponses = new ArrayList<>();

        for (Order lOrder : lOrderList) {
            OrderResponse lOrderResponse = new OrderResponse();
            lOrderResponse.setOrderId(lOrder.getOrderId());
            lOrderResponse.setOrderAmount(lOrder.getAmount());
            lOrderResponse.setOrderName(lOrder.getOrderName());
            lOrderResponses.add(lOrderResponse);
        }
        Meta lMeta = new Meta();
        lMeta.setTotalOrderCount(lTotalOrderCount);

        MetaData lMetaData = new MetaData();
        lMetaData.setMeta(lMeta);
        lMetaData.setData(lOrderResponses);

        return lMetaData;
    }

    /**
     * creates the Meta data summary from provided search term, page no,results per
     * page,sort by and sorting Order
     *
     * @param pOrderList       - A list of Order entities to be converted into
     *                         OrderResponse
     * @param pTotalOrderCount - total order count for orders
     * @return - Returns OrderResponsData class which contains meta and data fields
     */
    public MetaData createMetaDataBySearchTerm(Page<Order> pPageOrders) {
        Long lTotalOrderCount = pPageOrders.getTotalElements();
        List<Order> lOrderList = pPageOrders.getContent();
        List<OrderResponse> lOrderResponses = new ArrayList<>();
        for (Order lOrder : lOrderList) {
            OrderResponse lOrderResponse = new OrderResponse();
            lOrderResponse.setOrderId(lOrder.getOrderId());
            lOrderResponse.setOrderAmount(lOrder.getAmount());
            lOrderResponse.setOrderName(lOrder.getOrderName());
            lOrderResponses.add(lOrderResponse);
        }

        Meta lMeta = new Meta();
        lMeta.setTotalOrderCount(lTotalOrderCount);
        MetaData lMetaData = new MetaData();
        lMetaData.setMeta(lMeta);
        lMetaData.setData(lOrderResponses);
        return lMetaData;

    }

    /**
     * creates the order response data summary from provided search term, page
     * no,results per page,sort by and sorting Order
     *
     * @param pOrderList       - A list of Order entities to be converted into
     *                         OrderResponse
     * @param pTotalOrderCount - total order count for orders
     * @return - Returns OrderResponsData class which contains meta and data fields
     */
    public OrderResponse createOrderResponseForGet(Optional<Order> pOrderOptional) {
        Order lGetOrder = pOrderOptional.get();
        List<OrderLine> lOrderLinesList = lGetOrder.getOrderLines();

        OrderResponse lOrderResponse = createOrderResponse(lGetOrder);

        List<OrderLineResponse> lOrderLinesResponse = createOrderLinesResponse(lOrderLinesList);
        lOrderResponse.setOrderLinesResponseList(lOrderLinesResponse);

        return lOrderResponse;
    }

    private OrderResponse createOrderResponse(Order lGetOrder) {
        OrderResponse lOrderResponse = new OrderResponse();
        lOrderResponse.setOrderId(lGetOrder.getOrderId());
        lOrderResponse.setOrderAmount(lGetOrder.getAmount());
        lOrderResponse.setOrderName(lGetOrder.getOrderName());
        return lOrderResponse;
    }

    /**
     * creates a list of OrderLine entities to a list of OrderLineResponse DTOs.
     *
     * @param pOrderLines - A list of OrderLine entities to be converted into
     *                    OrderLineResponse DTOs
     * @return - A list of OrderLineResponse representing the summary details of
     *         each order line
     */
    private List<OrderLineResponse> createOrderLinesResponse(final List<OrderLine> pOrderLines) {
        List<OrderLineResponse> lOrderLinesResponse = new ArrayList<>();

        for (OrderLine lOrderLine : pOrderLines) {
            OrderLineResponse lOrderLineResponse = new OrderLineResponse();
            lOrderLineResponse.setOrderLineId(lOrderLine.getOrderLineId());
            lOrderLineResponse.setItemName(lOrderLine.getItemName());
            lOrderLineResponse.setQuantity(lOrderLine.getQuantity());
            lOrderLinesResponse.add(lOrderLineResponse);
        }
        return lOrderLinesResponse;
    }

}

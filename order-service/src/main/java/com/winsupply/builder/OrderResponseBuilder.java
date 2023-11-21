package com.winsupply.builder;

import com.winsupply.entity.Order;
import com.winsupply.globalexception.DataNotFoundException;
import com.winsupply.model.response.Meta;
import com.winsupply.model.response.OrderResponse;
import com.winsupply.model.response.OrderResponseData;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * The response builder responsible for building the response
 *
 * @param pOrderList       - A list of Order entities to be converted into
 *                         OrderResponse
 * @param pTotalOrderCount - total order count for orders
 * @return - A list of OrderLineResponse representing the summary details of
 *         each order line
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
    public OrderResponseData createOrderResponseData(Long pTotalOrderCount, List<Order> pOrderList) {
        List<OrderResponse> lOrderResponses = new ArrayList<>();

        for (Order lOrder : pOrderList) {
            OrderResponse lOrderResponse = new OrderResponse();
            lOrderResponse.setOrderId(lOrder.getOrderId());
            lOrderResponse.setOrderAmount(lOrder.getAmount());
            lOrderResponse.setOrderName(lOrder.getOrderName());
            lOrderResponses.add(lOrderResponse);
        }
        Meta lMeta = new Meta();
        lMeta.setTotalOrderCount(pTotalOrderCount);

        OrderResponseData lOrderResponseData = new OrderResponseData();
        lOrderResponseData.setMeta(lMeta);
        lOrderResponseData.setData(lOrderResponses);

        return lOrderResponseData;
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
    public OrderResponseData createOrderResponseDataSearch(Long pTotalOrderCount, List<Order> pOrderList) {
        List<OrderResponse> lOrderResponses = new ArrayList<>();
        for (Order lOrder : pOrderList) {
            OrderResponse lOrderResponse = new OrderResponse();
            lOrderResponse.setOrderId(lOrder.getOrderId());
            lOrderResponse.setOrderAmount(lOrder.getAmount());
            lOrderResponse.setOrderName(lOrder.getOrderName());
            lOrderResponses.add(lOrderResponse);
        }

        Meta lMeta = new Meta();
        lMeta.setTotalOrderCount(pTotalOrderCount);
        OrderResponseData lOrderResponseData = new OrderResponseData();
        lOrderResponseData.setMeta(lMeta);
        lOrderResponseData.setData(lOrderResponses);
        if (lMeta.getTotalOrderCount() == 0) {
            throw new DataNotFoundException("data not found in database with given search term");
        } else {
            return lOrderResponseData;
        }
    }
}

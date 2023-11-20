package com.winsupply.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;

/**
 * The {@code OrderLineRequest} class represents Order Response
 * @author PRADEEP
 */
@Data
@JsonInclude(Include.NON_NULL) // or Include.NON_EMPTY
public class OrderResponse {
    /**
     * The orderId takes values of type int
     */
    private int orderId;

    /**
     * The orderAmount takes values of type double
     */
    private double orderAmount;

    /**
     * The orderName takes values of type int
     */
    private String orderName;

    /**
     * The orderLinesList takes information of type List of OrderLines
     */
    private List<OrderLineResponse> orderLinesResponseList;

}

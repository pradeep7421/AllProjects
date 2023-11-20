package com.winsupply.model.response;

import lombok.Data;

/**
 * The {@code OrderLineRequest} class represents OrderLine Response
 * @author PRADEEP
 */
@Data
public class OrderLineResponse {
    /**
     * The orderLineId takes values of type int
     */
    private int orderLineId;
    /**
     * The itemName takes values of type string
     */
    private String itemName;
    /**
     * The quantity takes values of type int
     */
    private int quantity;

}

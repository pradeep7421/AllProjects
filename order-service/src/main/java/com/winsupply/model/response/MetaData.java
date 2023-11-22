package com.winsupply.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Data;

/**
 * The {@code OrderResponseData} class represents format of response
 * @author PRADEEP
 */
@Data
@JsonInclude(Include.NON_NULL)
public class MetaData {
    /**
     * The meta takes information from meta Object
     */
    private Meta meta;
    /**
     * The data takes values of type List of Order Responses
     */
    private List<OrderResponse> data;
}

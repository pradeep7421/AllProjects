package com.winsupply.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The {@code OrderRequest} class represents error response details
 * @author PRADEEP
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse {
    /**
     * The success takes values of type boolean
     */
    private boolean success;
    /**
     * The successMsg takes values of type string
     */
    private String successMsg;

}

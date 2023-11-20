package com.winsupply.model.response;

import lombok.Data;

/**
 * The {@code OrderRequest} class represents error response details
 * @author PRADEEP
 */
@Data
public class SuccessResponse {
    /**
     * The success takes values of type boolean
     */
    private boolean success;
    /**
     * The successMsg takes values of type string
     */
    private String successMsg;

    /**
     * Creates a new instance of the SuccessResponse
     *
     * @param pSuccess  - The pSuccess takes boolean type values
     * @param pSuccessMsg - The pErrorMsg takes string type error message
     */
    public SuccessResponse(boolean pSuccess, String pSuccessMsg) {
        super();
        this.success = pSuccess;
        this.successMsg = pSuccessMsg;
    }

}

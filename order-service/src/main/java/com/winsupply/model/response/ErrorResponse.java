package com.winsupply.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The {@code ErrorResponse} class represents Error Responses
 * @author PRADEEP
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    /**
     * The success takes values of type boolean
     */
    private boolean success;
    /**
     * The errorMessage takes values of type string
     */
    private String errorMessage;
    /**
     * The fieldErrors takes values of type List
     */
    private List<Map<String, String>> fieldErrors;
}

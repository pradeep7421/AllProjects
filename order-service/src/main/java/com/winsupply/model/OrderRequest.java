package com.winsupply.model;

import com.winsupply.constants.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * The {@code OrderRequest} class represents model class
 * @author PRADEEP
 */
@Data
public class OrderRequest {
    /**
     * The amount takes values of type double
     */
    @Min(value = 1, message = Constants.MIN_VALUE)
    @NotNull(message = Constants.NULL_VALUE)
    private Double amount;

    /**
     * The orderName takes values of type string
     */
    @Pattern(regexp = Constants.ORDER_NAME_REGEX_PATTERN, message = Constants.ORDER_NAME_SIZE)
    @NotBlank(message = Constants.ORDER_NAME_NOT_BLANK)
    private String orderName;

    /**
     * The orderLines takes values of type List
     */
    @Valid
    @NotNull(message = Constants.ORDERLINES_NOT_NULL)
    @Size(min = 1, message = Constants.MIN_ORDERLINES)
    private List<OrderLineRequest> orderLines;

}

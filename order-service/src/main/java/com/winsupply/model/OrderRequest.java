package com.winsupply.model;

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
    @Min(value = 1, message = "The minimum value must be 1")
    @NotNull(message = "Order amount must not be blank")
    private Double amount;

    /**
     * The orderName takes values of type string
     */
    @Pattern(regexp = "^[a-zA-Z ]{1,200}+$", message = "order name must contain Only alphabetic characters and spaces are allowed and must have atmost 200 characters")
    @NotBlank(message = "Order name must not be blank")
    private String orderName;

    /**
     * The orderLines takes values of type List
     */
    @Valid
    @NotNull(message = "orderlines must not be null")
    @Size(min = 1, message = "minimum OrderLine must be 1")
    private List<OrderLineRequest> orderLines;

}

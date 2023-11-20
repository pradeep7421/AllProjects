package com.winsupply.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * The {@code OrderLineRequest} class represents model class
 * @author PRADEEP
 */
@Data
public class OrderLineRequest {
    /**
     * The itemName takes values of type string
     */
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "item Name must contain Only alphabetic characters and spaces are allowed")
    @NotBlank(message = "Item name must not be blank")
    private String itemName;

    /**
     * The quantity takes values of type int
     */
    @Min(value = 1, message = "Quantity must be at least 1 and should not be blank")
    @Max(value = 25, message = "Quantity must not exceed 25")
    private int quantity;

}

package com.winsupply.model;

import com.winsupply.constants.Constants;
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
    @Pattern(regexp = Constants.ORDER_NAME_REGEX, message = Constants.ALPHABETIC_CHARACTER)
    @NotBlank(message = Constants.ITEM_NAME_NOT_BLANK)
    private String itemName;

    /**
     * The quantity takes values of type int
     */
    @Min(value = 1, message = Constants.QUANTITY_NOT_BLANK)
    @Max(value = 25, message = Constants.QUANTITY_NOT_EXCEEDS_25)
    private int quantity;

}

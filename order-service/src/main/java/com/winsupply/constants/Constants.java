package com.winsupply.constants;

/**
 * Constants
 *
 * @author Pradeep
 */
public class Constants {
    public static final String REQUEST_VALIDATION_ERRORS = "Request validation errors";
    public static final String ORDER_CREATED_SUCCESSFULLY = "Order created successfully";
    public static final String ORDER_UPDATED_SUCCESSFULLY = "order updated successfully";
    public static final String SORT_BY_REGEX_EXP = "^(orderId|amount|orderName)$";
    public static final String SORT_ORDER_REGEX_EXP = "^(asc|desc)$";
    public static final String INCORRECT_ID = "incorrect orderLineId or orderId";
    public static final String INPUT_MISMATCHED = "input type mismached in url please enter correct input";
    public static final String ORDERLINE_UPDATED_SUCCESSFULLY = "orderLine created successfully";
    public static final String ORDER_NOT_FOUND = "Order Details not found";
    public static final String ALPHABETIC_CHARACTER = "item Name must contain Only alphabetic characters and spaces";
    public static final String ORDER_NAME_REGEX = "^[a-zA-Z ]+$";
    public static final String ITEM_NAME_NOT_BLANK = "Item name must not be blank";
    public static final String QUANTITY_NOT_BLANK = "Quantity must be at least 1 and should not be blank";
    public static final String QUANTITY_NOT_EXCEEDS_25 = "Quantity must not exceed 25";
    public static final String MIN_VALUE = "The minimum value must be 1";
    public static final String NULL_VALUE = "Order amount must not be blank";
    public static final String ORDER_NAME_REGEX_PATTERN = "^[a-zA-Z ]{1,200}+$";
    public static final String ORDER_NAME_SIZE = "order name must contain Only alphabetic characters and spaces are allowed and must have atmost 200 characters";
    public static final String ORDER_NAME_NOT_BLANK = "Order name must not be blank";
    public static final String ORDERLINES_NOT_NULL = "orderlines must not be null";
    public static final String MIN_ORDERLINES = "minimum OrderLine must be 1";
    public static final String USER_AGENT = "User-Agent";
    public static final String BASE_URL = "http://localhost:7070";
    public static final String URL_FOR_GET = "/promotion-service/promotions?orderAmount=";
    public static final String POST_URL = "/orders";
    public static final String GET_URL = "/orders/{orderId}";
}

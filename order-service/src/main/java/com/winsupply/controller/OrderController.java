package com.winsupply.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.winsupply.builder.OrderResponseBuilder;
import com.winsupply.constants.Constants;
import com.winsupply.entity.Order;
import com.winsupply.model.OrderRequest;
import com.winsupply.model.response.MetaData;
import com.winsupply.model.response.OrderResponse;
import com.winsupply.model.response.SuccessResponse;
import com.winsupply.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The {@code OrderController} class defines the REST API End points for
 * managing orders
 * @author PRADEEP
 */
@RestController
@RequestMapping(path = "/orders")
@Validated
public class OrderController {

    /**
     * mLogger - the Logger
     */
    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    /**
     * mOrderService - the OrderService
     */
    private final OrderService mOrderService;

    private final OrderResponseBuilder mOrderResponseBuilder;

    /**
     * Creates a new instance of the OrderController
     *
     * @param pOrderService - The service responsible for order management
     */
    public OrderController(OrderService pOrderService, OrderResponseBuilder pOrderResponseBuilder) {
        this.mOrderService = pOrderService;
        this.mOrderResponseBuilder = pOrderResponseBuilder;
    }

    /**
     * End point to create a new order.
     *
     * @param pOrderRequest - The request body containing order information
     * @return ResponseEntity<ApiResponse> - A response indicating the success or
     *         failure of the operation
     */
    @PostMapping
    public ResponseEntity<SuccessResponse> createOrder(@Valid @RequestBody OrderRequest pOrderRequest) {
        mLogger.info("Payload -> pOrderRequest: {}", pOrderRequest);
        mOrderService.createOrder(pOrderRequest);
        mLogger.info("exiting createOrder method");

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse(true, Constants.ORDER_CREATED_SUCCESSFULLY));
    }

    /**
     * End point to retrieve order details by ID
     *
     * @param pOrderId - The unique Id for the order
     * @return - The order details or an error response if the order is not found
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Object> getOrderDetails(@NotNull @Min(value = 1) @PathVariable("orderId") int pOrderId,
            @RequestHeader("User-Agent") String pUserAgent) throws JsonMappingException, JsonProcessingException {
        mLogger.info("Order Id -> pOrderId: {}", pOrderId);
        Optional<Order> lOrderOptional = mOrderService.getOrderDetails(pOrderId, pUserAgent);
        OrderResponse lOrderResponse = mOrderResponseBuilder.createOrderResponseForGet(lOrderOptional);
        mLogger.debug("exiting getOrderDetails method");
        return ResponseEntity.status(HttpStatus.OK).body(lOrderResponse);
    }

    /**
     * End point to update the quantity of an order line
     *
     * @param pOrderId     - The unique id of the order
     * @param pOrderLineId - The unique id of the order line
     * @param pQuantity    - The quantity to set for the order line
     * @return - A response indicating the success or failure of the operation
     */
    @PutMapping("/{orderId}/orderLines/{orderLineId}/quantity/{quantity}")
    public ResponseEntity<SuccessResponse> updateOrderLineQuantity(@NotNull @Min(value = 1) @PathVariable(name = "orderId") int pOrderId,
            @NotNull @Min(value = 1) @PathVariable(name = "orderLineId") int pOrderLineId,
            @NotNull @Min(value = 1) @Max(value = 25) @PathVariable(name = "quantity") int pQuantity) {
        mLogger.info("PayLoad ->OrderId: {},OrderLineId: {},Quantity: {}", pOrderId, pOrderLineId, pQuantity);
        mOrderService.updateOrderLineQuantity(pOrderId, pOrderLineId, pQuantity);
        mLogger.info("Exiting from updateOrderLineQuantity method ");
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse(true, Constants.ORDER_UPDATED_SUCCESSFULLY));
    }

    /**
     ** End point to get all orders by page no,results per page,sort by and sorting
     * Order
     *
     * @param pPageNo         - Requires page number to view by page numbers
     * @param pResultsPerPage - Requires integer value to view by number of orders
     *                        per page
     * @param pSortBy         - Requires sort to sort by order id,order name or
     *                        amount
     * @param pSortOrder      - Requires sort orders by ascending and descending
     *                        order
     * @return - A response indicating the status code
     */
    @GetMapping
    public ResponseEntity<Object> getAllOrdersByPagination(@RequestParam(name = "page", defaultValue = "1") @Min(1) Integer pPageNo,
            @RequestParam(name = "rpp", defaultValue = "10") @Min(0) Integer pResultsPerPage,
            @RequestParam(name = "sortBy", defaultValue = "orderId") @Pattern(regexp = Constants.SORT_BY_REGEX_EXP) String pSortBy,
            @RequestParam(name = "sortOrder", defaultValue = "asc") @Pattern(regexp = Constants.SORT_ORDER_REGEX_EXP) String pSortOrder) {

        mLogger.info("Query Parameter -> PageNo: {},ResultsPerPage: {},SortBy: {},SortOrder: {}", pPageNo, pResultsPerPage, pSortBy, pSortOrder);

        Page<Order> lPageOrder = mOrderService.getAllOrdersByPagination(pPageNo - 1, pResultsPerPage, pSortBy, pSortOrder);

        MetaData lMetaData = mOrderResponseBuilder.createMetaDataByPagination(lPageOrder);
        return ResponseEntity.status(HttpStatus.OK).body(lMetaData);

    }

    /**
     * End point to get all orders by search term, page no,results per page,sort by
     * and sorting Order
     *
     * @param pSearchTerm     - Requires string to search by search term
     * @param pPageNo         - Requires page number to view by page numbers
     * @param pResultsPerPage - Requires integer value to view by number of orders
     *                        per page
     * @param pSortBy         - Requires sort to sort by order id,order name or
     *                        amount
     * @param pSortOrder      - Requires sort orders by ascending and descending
     *                        order
     * @return - A response indicating the status code
     */
    @GetMapping("/search")
    public ResponseEntity<Object> getAllOrdersBySearch(
            @RequestParam(name = "searchTerm") @NotBlank(message = "Search term must not be blank") String pSearchTerm,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) Integer pPageNo,
            @RequestParam(name = "rpp", defaultValue = "10") @Min(0) Integer pResultsPerPage,
            @RequestParam(name = "sortBy", defaultValue = "orderId") @Pattern(regexp = Constants.SORT_BY_REGEX_EXP) String pSortBy,
            @RequestParam(name = "sortOrder", defaultValue = "asc") @Pattern(regexp = Constants.SORT_ORDER_REGEX_EXP) String pSortOrder) {
        mLogger.info("Query Parameter -> SearchTerm:{}, PageNo: {},ResultsPerPage: {},SortBy: {},SortOrder", pSearchTerm, pPageNo, pResultsPerPage,
                pSortBy, pSortOrder);
        Page<Order> lPageOrders = mOrderService.getAllOrdersBySearch(pSearchTerm, pPageNo - 1, pResultsPerPage, pSortBy, pSortOrder);
        MetaData lMetaData = mOrderResponseBuilder.createMetaDataBySearchTerm(lPageOrders);
        return ResponseEntity.status(HttpStatus.OK).body(lMetaData);
    }
}

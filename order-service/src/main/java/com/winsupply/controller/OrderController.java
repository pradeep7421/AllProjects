package com.winsupply.controller;

import com.winsupply.entity.Order;
import com.winsupply.globalexception.DataNotFoundException;
import com.winsupply.model.OrderRequest;
import com.winsupply.model.response.Meta;
import com.winsupply.model.response.OrderResponse;
import com.winsupply.model.response.OrderResponseData;
import com.winsupply.model.response.SuccessResponse;
import com.winsupply.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Creates a new instance of the OrderController
     *
     * @param pOrderService - The service responsible for order management
     */
    public OrderController(OrderService pOrderService) {

        this.mOrderService = pOrderService;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse(true, "Order created successfully"));
    }

    /**
     * End point to retrieve order details by ID
     *
     * @param pOrderId - The unique Id for the order
     * @return - The order details or an error response if the order is not found
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Object> getOrderDetails(@NotNull @Min(value = 1) @PathVariable("orderId") int pOrderId) {
        mLogger.info("Order Id -> pOrderId: {}", pOrderId);
        OrderResponse lOrderResponse = mOrderService.getOrderDetails(pOrderId);
        mLogger.debug("exiting getOrderDetails method");
        return ResponseEntity.status(HttpStatus.OK).body(lOrderResponse);
    }

    /**
     * End point to update the quantity of an order line
     *
     * @param pOrderId     - The unique identifier of the order
     * @param pOrderLineId - The unique identifier of the order line
     * @param pQuantity    - The new quantity to set for the order line
     * @return - A response indicating the success or failure of the operation
     */
    @PutMapping("/{orderId}/orderLines/{orderLineId}/quantity/{quantity}")
    public ResponseEntity<SuccessResponse> updateOrderLineQuantity(@NotNull @Min(value = 1) @PathVariable(name = "orderId") int pOrderId,
            @NotNull @Min(value = 1) @PathVariable(name = "orderLineId") int pOrderLineId,
            @NotNull @Min(value = 1) @Max(value = 25) @PathVariable(name = "quantity") int pQuantity) {
        mLogger.info("PayLoad ->OrderId: {},OrderLineId: {},Quantity: {}", pOrderId, pOrderLineId, pQuantity);
        mOrderService.updateOrderLineQuantity(pOrderId, pOrderLineId, pQuantity);
        mLogger.info("Exiting from updateOrderLineQuantity method ");
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse(true, "order updated successfully"));
    }

    /**
     ** End point to get all orders by page no,results per page,sort by and sorting
     * Order
     *
     * @param pPageNo         - To get orders by page numbers
     * @param pResultsPerPage - To get number of orders per page
     * @param pSortBy         - To sort orders by order id,order name or amount
     * @param pSortOrder      - To sort orders by ascending and descending order
     * @return - A response indicating the status code
     */
    @GetMapping
    public ResponseEntity<Object> getAllOrdersByPagination(@RequestParam(name = "page", defaultValue = "1") @Min(1) Integer pPageNo,
            @RequestParam(name = "rpp", defaultValue = "10") Integer pResultsPerPage,
            @RequestParam(name = "sortBy", defaultValue = "orderId") @Pattern(regexp = "^(orderId|amount|orderName)$") String pSortBy,
            @RequestParam(name = "sortOrder", defaultValue = "asc") @Pattern(regexp = "^(asc|desc)$") String pSortOrder) {

        mLogger.info("Query Parameter -> PageNo: {},ResultsPerPage: {},SortBy: {},SortOrder: {}", pPageNo, pResultsPerPage, pSortBy, pSortOrder);

        Page<Order> lPageOrder = mOrderService.getAllOrdersByPagination(pPageNo - 1, pResultsPerPage, pSortBy, pSortOrder);
        Long lTotalOrderCount = lPageOrder.getTotalElements();

        if (lTotalOrderCount == 0) {
            mLogger.debug("Exiting getAllOrdersByPagination method");
            throw new DataNotFoundException("Data is not available in database");
        } else {
            List<Order> lOrderList = lPageOrder.getContent();
            OrderResponseData lOrderResponseData = createOrderResponseData(lTotalOrderCount, lOrderList);

            return ResponseEntity.status(HttpStatus.OK).body(lOrderResponseData);
        }
    }

    /**
     * End point to get all orders by search term, page no,results per page,sort by
     * and sorting Order
     *
     * @param pSearchTerm     - To get orders by search term
     * @param pPageNo         - To get orders by page numbers
     * @param pResultsPerPage - To get number of orders per page
     * @param pSortBy         - To sort orders by order id,order name or amount
     * @param pSortOrder      - To sort orders by ascending and descending order
     * @return - A response indicating the status code
     */
    @GetMapping("/search")
    public ResponseEntity<Object> getAllOrdersBySearch(
            @RequestParam(name = "searchTerm") @NotBlank(message = "Search term must not be blank") String pSearchTerm,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) Integer pPageNo,
            @RequestParam(name = "rpp", defaultValue = "10") Integer pResultsPerPage,
            @RequestParam(name = "sortBy", defaultValue = "orderId") @Pattern(regexp = "^(orderId|amount|orderName)$") String pSortBy,
            @RequestParam(name = "sortOrder", defaultValue = "asc") @Pattern(regexp = "^(asc|desc)$") String pSortOrder) {
        mLogger.info("Query Parameter -> SearchTerm:{}, PageNo: {},ResultsPerPage: {},SortBy: {},SortOrder", pSearchTerm, pPageNo, pResultsPerPage,
                pSortBy, pSortOrder);
        Page<Order> lPageOrders = mOrderService.getAllOrdersBySearch(pSearchTerm, pPageNo - 1, pResultsPerPage, pSortBy, pSortOrder);
        Long lTotalOrderCount = lPageOrders.getTotalElements();
        List<Order> lOrderList = lPageOrders.getContent();
        OrderResponseData lOrderResponseData = createOrderResponseDataSearch(lTotalOrderCount, lOrderList);
        return ResponseEntity.status(HttpStatus.OK).body(lOrderResponseData);
    }

    /**
     * creates order response data summary from list of orders
     *
     * @param pOrderList       - A list of Order entities to be converted into
     *                         OrderResponse
     * @param pTotalOrderCount - total order count for orders
     * @return - A list of OrderLineResponse representing the summary details of
     *         each order line
     */
    private OrderResponseData createOrderResponseData(Long pTotalOrderCount, List<Order> pOrderList) {
        List<OrderResponse> lOrderResponses = new ArrayList<>();

        for (Order lOrder : pOrderList) {
            OrderResponse lOrderResponse = new OrderResponse();
            lOrderResponse.setOrderId(lOrder.getOrderId());
            lOrderResponse.setOrderAmount(lOrder.getAmount());
            lOrderResponse.setOrderName(lOrder.getOrderName());
            lOrderResponses.add(lOrderResponse);
        }
        Meta lMeta = new Meta();
        lMeta.setTotalOrderCount(pTotalOrderCount);

        OrderResponseData lOrderResponseData = new OrderResponseData();
        lOrderResponseData.setMeta(lMeta);
        lOrderResponseData.setData(lOrderResponses);

        return lOrderResponseData;
    }

    /**
     * creates the order response data summary from provided search term, page
     * no,results per page,sort by and sorting Order
     *
     * @param pOrderList       - A list of Order entities to be converted into
     *                         OrderResponse
     * @param pTotalOrderCount - total order count for orders
     * @return - Returns OrderResponsData class which contains meta and data fields
     */
    private OrderResponseData createOrderResponseDataSearch(Long pTotalOrderCount, List<Order> pOrderList) {
        List<OrderResponse> lOrderResponses = new ArrayList<>();
        for (Order lOrder : pOrderList) {
            OrderResponse lOrderResponse = new OrderResponse();
            lOrderResponse.setOrderId(lOrder.getOrderId());
            lOrderResponse.setOrderAmount(lOrder.getAmount());
            lOrderResponse.setOrderName(lOrder.getOrderName());
            lOrderResponses.add(lOrderResponse);
        }

        Meta lMeta = new Meta();
        lMeta.setTotalOrderCount(pTotalOrderCount);
        OrderResponseData lOrderResponseData = new OrderResponseData();
        lOrderResponseData.setMeta(lMeta);
        lOrderResponseData.setData(lOrderResponses);
        if (lMeta.getTotalOrderCount() == 0) {
            throw new DataNotFoundException("data not found in database with given search term");
        } else {
            System.out.println("Success");
            return lOrderResponseData;
        }
    }
}

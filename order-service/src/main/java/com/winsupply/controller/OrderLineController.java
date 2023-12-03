package com.winsupply.controller;

import com.winsupply.constants.Constants;
import com.winsupply.model.OrderLineRequest;
import com.winsupply.model.response.SuccessResponse;
import com.winsupply.service.OrderLineService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The {@code OrderLineController} class defines the REST API endpoints for
 * managing ordersLine
 * @author PRADEEP
 */
@RestController
@RequestMapping(path = "/lines")
@Validated
public class OrderLineController {
    /**
     * mOrderLineService - the OrderLineService
     */
    private OrderLineService mOrderLineService;

    /**
     * mLogger - the Logger
     */
    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    /**
     * Creates a new instance of the OrderLineController
     *
     * @param pOrderLineService - The service responsible for OrderLine management
     */
    public OrderLineController(OrderLineService pOrderLineService) {
        this.mOrderLineService = pOrderLineService;
    }

    /**
     * EndPoint to create new orderLines
     *
     * @param pOrderLinesRequest - The request containing List of orderLine
     *                           information
     * @return ResponseEntity<ApiResponse> - A response indicating the success or
     *         failure of the operation
     */
    @PostMapping
    public ResponseEntity<SuccessResponse> createOrderLines(@Valid @RequestBody @NotEmpty List<OrderLineRequest> pOrderLinesRequest,
            @RequestParam(name = "orderId") Integer pOrderId) {
        mLogger.info("payload -> pOrderLineItems :{} ", pOrderLinesRequest);
        mOrderLineService.createOrderLines(pOrderLinesRequest, pOrderId);
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse(true, Constants.ORDERLINE_UPDATED_SUCCESSFULLY));
    }

}

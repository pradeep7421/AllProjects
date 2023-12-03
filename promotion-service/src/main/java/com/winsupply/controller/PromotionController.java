package com.winsupply.controller;

import com.winsupply.builder.ResponseBuilder;
import com.winsupply.constants.Constants;
import com.winsupply.model.PromotionRequest;
import com.winsupply.response.PromotionResponse;
import com.winsupply.response.SuccessResponse;
import com.winsupply.service.PromotionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The {@code PromotionController} class defines the REST API End points for
 * managing promotions
 * @author PRADEEP
 */
@RestController
@RequestMapping(path = "/promotions")
@Validated
public class PromotionController {
    /**
     * mPromotionService - the PromotionService
     */
    private PromotionService mPromotionService;

    /**
     * mLogger - the Logger
     */
    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());
    ResponseBuilder mResponseBuilder;

    /**
     * Creates a new instance of the PromotionController
     *
     * @param pPromotionService - The service responsible for promotion management
     */
    public PromotionController(PromotionService pPromotionService, ResponseBuilder pResponseBuilder) {
        this.mPromotionService = pPromotionService;
        this.mResponseBuilder = pResponseBuilder;
    }

    /**
     * End point to calculate discount amount
     *
     * @param pOrderAmount            - The value of amount for the order
     * @param pProfitMarginPercentage - The value for profit margin percentage
     * @param pUserAgent              - The value for request header in string
     * @return - Returns Data which contains discount amount
     */
    @GetMapping
    public ResponseEntity<Object> getDiscountAmount(
            @RequestParam(name = "orderAmount") @Positive(message = Constants.POSITIVE_ORDER_AMOUNT) Double pOrderAmount,
            @RequestHeader("User-Agent")@NotBlank String pUserAgent) {
        mLogger.info("OrderAmount: {},UserAgent: {}", pOrderAmount, pUserAgent);
        Double lDiscountAmount = mPromotionService.getDiscountAmount(pOrderAmount, pUserAgent);
        PromotionResponse lPromotionResponse = mResponseBuilder.createPromotionRespomse(lDiscountAmount);
        mLogger.info("exiting getDiscountAmount method");
        return ResponseEntity.status(HttpStatus.OK).body(lPromotionResponse);

    }

    /**
     * End point to calculate discount amount
     *
     * @param pOrderAmount            - The value of amount for the order
     * @param pProfitMarginPercentage - The value for profit margin percentage
     * @param pUserAgent              - The value for request header in string
     * @return - Returns Data which contains discount amount
     * @throws Exception
     */
    @PostMapping
    public ResponseEntity<SuccessResponse> updateProfitMarginPercentage(@RequestBody @Valid PromotionRequest pPromotionRequest) throws Exception {
        mLogger.info("Payload -> pPromotionRequest: {}, profit margin percentage: {}", pPromotionRequest);
        mPromotionService.updateProfitMarginPercentage(pPromotionRequest);
        mLogger.info("exiting setProfitMarginPercentage method");
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessResponse(true, Constants.PROFIT_MARGIN_PERCENTAGE_SUCCESSFULLY_SET));
    }

}

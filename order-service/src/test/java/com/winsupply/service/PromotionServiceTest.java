package com.winsupply.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winsupply.promotion.Data;
import com.winsupply.promotion.PromotionResponse;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.Assert.assertEquals;


/**
 * This is the documentation for the PromotionServiceTest It provides an
 * overview of the class unit Testing
 */
public class PromotionServiceTest {
    /**
     * mPromotionService - the PromotionService
     */
    PromotionService mPromotionService;

    /**
     * mMockWebServer - the MockWebServer
     */
    private MockWebServer mMockWebServer;

    /**
     * mObjectMapper - the ObjectMapper
     */
    private ObjectMapper mObjectMapper = new ObjectMapper();

    /**
     * Initiates this method before executing every method
     */
    @BeforeEach
    void setUp() throws IOException {
        mMockWebServer = new MockWebServer();
        mMockWebServer.start();
        WebClient webClient = WebClient.builder().build();
        mPromotionService = new PromotionService(webClient);
        ReflectionTestUtils.setField(mPromotionService, "mBaseUrl", String.format("http://localhost:%s", mMockWebServer.getPort()));

    }

    /**
     * Initiates this method after executing every method
     */
    @AfterEach
    void tearDown() throws Exception {
        mMockWebServer.shutdown();
    }

    /**
     * This method is tests for Apple device request
     */
    @Test
    void testGetPromotionDetails() throws Exception {
        String userAgent = "Mozilla/5.0 (iPhone; U; ru; CPU iPhone OS 4_2_1 like Mac OS X; ru) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148a Safari/6533.18.5";
        double orderAmount = 100.0;
        Data lData = new Data();
        lData.setFinalDiscountAmount(1000d);
        PromotionResponse lPromotionResponse = new PromotionResponse();
        lPromotionResponse.setData(lData);

        mMockWebServer
                .enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value()).setBody(mObjectMapper.writeValueAsString(lPromotionResponse)));

        ResponseEntity<String> result = mPromotionService.getPromotionDetails(userAgent, orderAmount);
        PromotionResponse lPromotionResponses = mObjectMapper.readValue(result.getBody(), PromotionResponse.class);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(lPromotionResponse.getData().getFinalDiscountAmount(), lPromotionResponses.getData().getFinalDiscountAmount());

        RecordedRequest request = mMockWebServer.takeRequest();

        assertEquals("/promotion-service/promotions?orderAmount=100.0", request.getPath());
        assertEquals("GET", request.getMethod());
        assertEquals(userAgent, request.getHeader("User-Agent"));
    }

}

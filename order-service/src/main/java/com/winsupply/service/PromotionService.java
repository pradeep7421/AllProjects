package com.winsupply.service;

import com.winsupply.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PromotionService {

    /**
     * mWebClient - the WebClient
     */
    WebClient mWebClient;

    /**
     * mLogger - the Logger
     */
    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    /**
     * mBaseUrl - the Base Url
     */
    String mBaseUrl = "http://localhost:7070";

    /**
     * Creates a new instance of the PromotionService
     *
     * @param - pWebClient The WebClient for calling another api
     */
    public PromotionService(WebClient pWebClient) {
        this.mWebClient = pWebClient;
    }

    /**
     * Retrieves the response from other api
     *
     * @param pUserAgent       - The Request header when calling other api
     * @param pOrderAmount-The order amount to calculate final discount amount
     * @return - The ResponseEntity details for the called api
     */
    public ResponseEntity<String> getPromotionDetails(String pUserAgent, double pOrderAmount) {

        ResponseEntity<String> lResponseEntity = mWebClient.get().uri(Constants.BASE_URL + Constants.URL_FOR_GET + pOrderAmount)
                .header(Constants.USER_AGENT, pUserAgent).retrieve().toEntity(String.class).block();

        mLogger.info("ResponseEntity body: {}, ResponseEntity status code: {}", lResponseEntity.getBody(), lResponseEntity.getStatusCode());

        return lResponseEntity;
    }
}

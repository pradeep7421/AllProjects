package com.winsupply.mdmcustomertoecomsubscriber.listener;

import com.winsupply.mdmcustomertoecomsubscriber.service.CustomerSubscriberService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

/**
 * Listens to the messages from queue
 *
 * @author Amritanshu
 *
 */
@Component
public class MQListener {

    /**
     * the logger
     */
    private Logger mLogger = LogManager.getLogger(MQListener.class);

    private final CustomerSubscriberService mCustomerSubscriberService;

    public MQListener(final CustomerSubscriberService pCustomerSubscriberService) {
        this.mCustomerSubscriberService = pCustomerSubscriberService;
    }

    /**
     * <b>receiveEcomQuotesWiseMsg</b> - Receives the WISE quote messages
     *
     * @param pMessage - the Message
     */
    @JmsListener(destination = "${winsupply.mq.topicName}", containerFactory = "mdmCustomerJmsListenerContainerFactory",
            subscription = "${winsupply.mq.subscriberName}")
    public void receiveEcomQuotesWiseMsg(final Message<?> pMessage) {
        final String lPayload = (String) pMessage.getPayload();
        final MessageHeaders lMessageHeaders = pMessage.getHeaders();

        mLogger.debug("MDM Customer Payload :: {}", lPayload);

        mCustomerSubscriberService.processCustomerMessage(lPayload, lMessageHeaders);
    }

}

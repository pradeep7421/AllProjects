package com.winsupply.mdmcustomertoecomsubscriber.listener;

import com.winsupply.mdmcustomertoecomsubscriber.service.CustomerSubscriberService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

/**
 * Listens to the messages from topic
 *
 * @author Amritanshu
 *
 */
@Component
@RequiredArgsConstructor
public class MQListener {

    /**
     * the logger
     */
    private Logger mLogger = LogManager.getLogger(MQListener.class);

    private final CustomerSubscriberService mCustomerSubscriberService;

    /**
     * <b>receiveEcomCustomerMdmMsg</b> - Receives the MDM customer messages
     *
     * @param pMessage - the Message
     */
    @JmsListener(destination = "${winsupply.mq.topicName}", containerFactory = "mdmCustomerJmsListenerContainerFactory",
                subscription = "${winsupply.mq.subscriberName}")
    public void receiveEcomCustomerMdmMsg(final Message<?> pMessage) {
        final String lPayload = (String) pMessage.getPayload();
        final MessageHeaders lMessageHeaders = pMessage.getHeaders();

        mLogger.debug("MDM Customer Payload :: {}", lPayload);

        mCustomerSubscriberService.processCustomerMessage(lPayload, lMessageHeaders);
    }

}

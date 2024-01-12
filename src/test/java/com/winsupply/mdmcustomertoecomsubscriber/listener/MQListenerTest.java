package com.winsupply.mdmcustomertoecomsubscriber.listener;

import com.winsupply.common.utils.Utils;
import com.winsupply.mdmcustomertoecomsubscriber.service.CustomerSubscriberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MQListenerTest {

    @InjectMocks
    private MQListener mMQListener;

    @Mock
    private CustomerSubscriberService mCustomerSubscriberService;

    @Test
    void testReceiveEcomCustomerMdmMsg() {
        String lReadFileUtilityPayload = Utils.readFile("customerPayload.json");

        MessageHeaders lMessageHeaders = Utils.getMessageHeaders("update");

        Mockito.doNothing().when(mCustomerSubscriberService).processCustomerMessage(lReadFileUtilityPayload, lMessageHeaders);
        Message<?> lMessage = new GenericMessage<>(lReadFileUtilityPayload, lMessageHeaders);
        mMQListener.receiveEcomCustomerMdmMsg(lMessage);
        verify(mCustomerSubscriberService, times(1)).processCustomerMessage((String) lMessage.getPayload(), lMessage.getHeaders());
    }

}

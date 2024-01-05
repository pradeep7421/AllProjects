package com.winsupply.mdmcustomertoecomsubscriber.listener;

import com.winsupply.mdmcustomertoecomsubscriber.service.CustomerSubscriberService;
import com.winsupply.readfile.PayLoadReadFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
public class MQListenerTest {
    @InjectMocks
    MQListener mMQListener;

    @Mock
    CustomerSubscriberService mCustomerSubscriberService;

    @Test
    void receiveEcomCustomerMdmMsgTest() throws IOException {
        String lPayLoad = PayLoadReadFile.readFile("payLoad.json");

        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders lMessageHeaders = new MessageHeaders(lHeaders);

        Mockito.doNothing().when(mCustomerSubscriberService).processCustomerMessage(lPayLoad, lMessageHeaders);
        Message lMessage = new GenericMessage<String>(lPayLoad, lMessageHeaders);
        mMQListener.receiveEcomCustomerMdmMsg(lMessage);
        verify(mCustomerSubscriberService, times(1)).processCustomerMessage((String) lMessage.getPayload(), lMessage.getHeaders());
    }
}

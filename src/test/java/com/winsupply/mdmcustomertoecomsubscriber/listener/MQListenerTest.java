package com.winsupply.mdmcustomertoecomsubscriber.listener;

import com.winsupply.mdmcustomertoecomsubscriber.service.CustomerSubscriberService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.ResourceUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MQListenerTest {
    @InjectMocks
    MQListener mMQListener;
    
    @Mock
    CustomerSubscriberService mCustomerSubscriberService;
    
    public static String readFile(final String pFilePath) throws IOException {
        final File lFile = ResourceUtils.getFile("classpath:" + pFilePath);
        final String lContent = new String(Files.readAllBytes(lFile.toPath()));
        return lContent;
    }
    @Test
    void receiveEcomCustomerMdmMsgTest() throws IOException {
        String lPayLoad = readFile("payLoad.json");
        
        String lActionCode = "not_delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code",lActionCode );
        MessageHeaders lMessageHeaders = new MessageHeaders(lHeaders);
        
        Mockito.doNothing().when(mCustomerSubscriberService).processCustomerMessage(lPayLoad, lMessageHeaders);
        Message lMessage = new GenericMessage<String>(lPayLoad, lMessageHeaders);
        mMQListener.receiveEcomCustomerMdmMsg(lMessage);
        verify(mCustomerSubscriberService,times(1)).processCustomerMessage((String)lMessage.getPayload(), lMessage.getHeaders());
    }
}

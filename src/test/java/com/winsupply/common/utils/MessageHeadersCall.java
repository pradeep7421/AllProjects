package com.winsupply.common.utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.messaging.MessageHeaders;

/**
 * MessageHeadersCall
 *
 * @author Pradeep
 */
public class MessageHeadersCall {

    /**
     * getMessageHeaders
     *
     * @return - MessageHeaders
     */
    public static MessageHeaders getMessageHeaders() {
        String lActionCode = "update";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders lMessageHeaders = new MessageHeaders(lHeaders);
        return lMessageHeaders;
    }
    public static MessageHeaders getMessageHeadersWithDelete() {
        String lActionCode = "delete";
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", lActionCode);
        MessageHeaders lMessageHeaders = new MessageHeaders(lHeaders);
        return lMessageHeaders;
    }
}

package com.winsupply.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.ResourceUtils;

/**
 * Utility
 *
 * @author Pradeep
 */
public class Utils {

    private static final Logger mLogger = LoggerFactory.getLogger(Utils.class);

    /**
     * Reads File from FilePath
     *
     * String pFilePath - the File path in String
     * @return - returns data in string
     */
    public static final String readFile(final String pFilePath) {
        String lContent = null;
        try {
            final File lFile = ResourceUtils.getFile("classpath:" + pFilePath);
            lContent = new String(Files.readAllBytes(lFile.toPath()));
        } catch (IOException pException) {
            mLogger.warn("File not found", pException);
        }
        return lContent;
    }

    /**
     * getMessageHeaders -Getting message headers
     *
     * @return - MessageHeaders
     */
    public static final MessageHeaders getMessageHeaders(final String pActionCode) {
        Map<String, Object> lHeaders = new HashMap<>();
        lHeaders.put("action_code", pActionCode);
        MessageHeaders lMessageHeaders = new MessageHeaders(lHeaders);
        return lMessageHeaders;
    }

}

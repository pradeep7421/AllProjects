package com.winsupply.mdmcustomertoecomsubscriber.common;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 * Utility
 *
 * @author Amritanshu
 *
 */
public class Utility {

    private Utility() {
    }

    private static final ObjectMapper mObjectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * <b>unmarshallData</b> - Unmarshall data
     *
     * @param pData  - String data
     * @param pClazz Class
     * @return an object of the give class
     * @throws IOException
     * @throws DatabindException
     * @throws StreamReadException
     */
    public static <T> T unmarshallData(final String pData, final Class<T> pClazz) throws StreamReadException, DatabindException, IOException {
        return mObjectMapper.readValue(pData, pClazz);
    }
}

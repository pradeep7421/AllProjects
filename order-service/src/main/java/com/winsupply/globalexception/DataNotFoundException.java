package com.winsupply.globalexception;

/**
 * An exception class representing a "Data Not Found" exception This exception
 * is thrown when an attempt to retrieve data from a source (e.g., a database)
 * results in no data being found for the specified criteria
 *
 * @see RuntimeException
 * @author PRADEEP
 */
public class DataNotFoundException extends RuntimeException {

    /**
     * Unique value for serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new `DataNotFoundException` with the specified detail message.
     *
     * @param pMessage A descriptive message indicating the cause of the exception.
     */
    public DataNotFoundException(String pMessage) {
        super(pMessage);
    }

}

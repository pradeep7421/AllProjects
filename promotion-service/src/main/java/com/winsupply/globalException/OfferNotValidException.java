package com.winsupply.globalException;

/**
 * An exception class representing a "Offer Not valid" exception This exception
 * is thrown when the http request is coming from device other then
 * Apple,windows or android
 *
 * @see RuntimeException
 * @author PRADEEP
 */
public class OfferNotValidException extends RuntimeException {

    /**
     * Unique value for serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new `OfferNotValidException` with the specified detail message.
     *
     * @param pMessage A descriptive message indicating the cause of the exception.
     */
    public OfferNotValidException(String pMessage) {
        super(pMessage);
    }

}

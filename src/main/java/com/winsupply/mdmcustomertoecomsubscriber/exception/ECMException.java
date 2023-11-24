package com.winsupply.mdmcustomertoecomsubscriber.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * ECM Exception
 *
 * @author Amritanshu
 */
@Getter
@Setter
public class ECMException extends Exception {

    /**
     * Build the exception with a specific message
     */
    public ECMException(final String pMessage) {
        super(pMessage);
    }
}

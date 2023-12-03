package com.winsupply.globalException;

import com.winsupply.constants.Constants;
import com.winsupply.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

/**
 * The {@code GlobalExceptionHandler} class is a centralized exception handler
 * for handling custom exceptions and mapping them to appropriate HTTP responses
 * @author PRADEEP
 */
@RestControllerAdvice
public class GlobalException extends ResponseEntityExceptionHandler {
    /**
     * mLogger - the Logger
     */
    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    /**
     * Handle the {@link DataNotFoundException} exception by returning a NOT_FOUND
     * response
     *
     * @param pException - The OfferNotValidException instance
     * @return A response entity with an error message and HTTP status NOT_FOUND
     */
    @ExceptionHandler(OfferNotValidException.class)
    public ResponseEntity<ErrorResponse> handleOfferNotValidException(final OfferNotValidException pException) {
        mLogger.error(pException.getMessage(), pException);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(false, pException.getMessage(), null));
    }

    /**
     * It handles the MethodArgumentNotValidException.
     *
     * @param pException - the MethodArgumentNotValidException
     * @return - ResponseEntity<Object>
     */
    @ExceptionHandler({ MethodArgumentNotValidException.class })
    public ResponseEntity<Object> handleMethodArgumentNotValidException(final MethodArgumentNotValidException pException) {
        mLogger.error("MethodArgumentNotValidException -> ", pException);
        final List<Map<String, String>> lFieldValidationErrors = pException.getBindingResult().getAllErrors().stream()
                .filter(FieldError.class::isInstance).map(FieldError.class::cast).map(lFieldError -> {
                    final Map<String, String> lFieldValidationErrorsMap = new HashMap<>();
                    lFieldValidationErrorsMap.put(lFieldError.getField(), lFieldError.getDefaultMessage());
                    return lFieldValidationErrorsMap;
                }).toList();
        return new ResponseEntity<>(new ErrorResponse(false, Constants.REQUEST_VALIDATION_ERRORS, lFieldValidationErrors), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle the {@link ConstraintViolationException} exception by returning a
     * BAD_REQUEST response
     *
     * @param pException - The ConstraintViolationException instance
     * @return A response entity with an error message and HTTP status BAD_REQUEST
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(final ConstraintViolationException pException) {
        mLogger.error(pException.getMessage(), pException);
        return new ResponseEntity<>(new ErrorResponse(false, pException.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle the {@link MissingServletRequestParameterException} exception by
     * returning a BAD_REQUEST response
     *
     * @param pException - The MissingServletRequestParameterException instance
     * @return A response entity with an error message and HTTP status BAD_REQUEST
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(final MissingServletRequestParameterException pException) {
        mLogger.error(pException.getMessage(), pException);
        return new ResponseEntity<>(new ErrorResponse(false, "request parameter value cannot be blank", null), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle the {@link MethodArgumentTypeMismatchException} exception by returning
     * a BAD_REQUEST response
     *
     * @param pException - The MethodArgumentTypeMismatchException instance
     * @return A response entity with an error message and HTTP status BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException pException) {
        mLogger.error(pException.getMessage(), pException);
        return new ResponseEntity<>(new ErrorResponse(false, "input type mismached in url please enter correct input type", null),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle the {@link HttpMessageNotReadableException} exception by returning a
     * BAD_REQUEST response
     *
     * @param pException - The HttpMessageNotReadableException instance
     * @return A response entity with an error message and HTTP status BAD_REQUEST
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(final HttpMessageNotReadableException pException) {
        mLogger.error(pException.getMessage(), pException);
        return new ResponseEntity<>(new ErrorResponse(false, Constants.REQUEST_BODY_MISSING, null), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle the {@link UnexpectedTypeException} exception by returning a
     * BAD_REQUEST response
     *
     * @param pException - The UnexpectedTypeException instance
     * @return A response entity with an error message and HTTP status BAD_REQUEST
     */
    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedTypeException(final UnexpectedTypeException pException) {

        mLogger.error(pException.getMessage(), pException);
        return new ResponseEntity<>(new ErrorResponse(false, pException.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

}

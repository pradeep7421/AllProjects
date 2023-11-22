package com.winsupply.globalexception;

import com.winsupply.model.response.ErrorResponse;
import com.winsupply.model.response.SuccessResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * mLogger - the Logger
     */
    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    /**
     * Handle the {@link DataNotFoundException} exception by returning a NOT_FOUND
     * response
     *
     * @param pDataNotFoundException - The DataNotFoundException instance
     * @return A response entity with an error message and HTTP status NOT_FOUND
     */
    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<SuccessResponse> handleDataNotFoundException(final DataNotFoundException pDataNotFoundException) {
        mLogger.error(pDataNotFoundException.getMessage(), pDataNotFoundException);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SuccessResponse(false, pDataNotFoundException.getMessage()));
    }

    /**
     * Handle MethodArgumentNotValid Exception by returning a BAD_REQUEST response
     *
     * @param pException The MethodArgumentNotValid Exception instance.
     * @return A response entity with an error message and HTTP status BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(final MethodArgumentNotValidException pException) {
        final List<Map<String, String>> lFiledValidationErrors = pException.getBindingResult().getAllErrors().stream()
                .filter(FieldError.class::isInstance).map(FieldError.class::cast).map(lFieldError -> {
                    final Map<String, String> lErrorsMap = new HashMap<>();
                    lErrorsMap.put(lFieldError.getField(), lFieldError.getDefaultMessage());
                    return lErrorsMap;
                }).toList();
        mLogger.error(pException.getMessage(), pException);
        return new ResponseEntity<>(new ErrorResponse(false, "Validation Errors", lFiledValidationErrors), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ConstraintViolation Exception by returning a BAD_REQUEST response
     *
     * @param pException The ConstraintViolation Exception instance.
     * @return A response entity with an error message and HTTP status BAD_REQUEST
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(final ConstraintViolationException pException) {

        mLogger.error(pException.getMessage(), pException);
        return new ResponseEntity<>(new ErrorResponse(false, pException.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle UnexpectedTypeException by returning a BAD_REQUEST response
     *
     * @param pException The UnexpectedType Exception instance.
     * @return A response entity with an error message and HTTP status BAD_REQUEST
     */
    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedTypeException(final UnexpectedTypeException pException) {

        mLogger.error(pException.getMessage(), pException);
        return new ResponseEntity<>(new ErrorResponse(false, pException.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle NullPointerException by returning a BAD_REQUEST response
     *
     * @param pException The NullPointer Exception instance
     * @return A response entity with an error message and HTTP status BAD_REQUEST
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(final NullPointerException pException) {

        mLogger.error(pException.getMessage(), pException);
        return new ResponseEntity<>(new ErrorResponse(false, pException.getMessage(), null), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException pException) {
        mLogger.error(pException.getMessage(), pException);
        return new ResponseEntity<>(new ErrorResponse(false, "input type mismached in url please enter correct input", null), HttpStatus.BAD_REQUEST);
    }

}

package it.pipodi.naspi.exceptions.handlers;

import it.pipodi.naspi.exceptions.ErrorResponse;
import it.pipodi.naspi.exceptions.NASPiRuntimeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class NASPiExceptionHandler {

    /*
        Handles the NASPiRuntimeException
     */
    @ExceptionHandler(value = NASPiRuntimeException.class)
    public ResponseEntity<Object> handleException(NASPiRuntimeException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorMessage(e.getMessage());
        errorResponse.setCause(e.toString());
        return new ResponseEntity<>(errorResponse, e.getStatus());
    }
}

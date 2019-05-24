package it.pipodi.naspi.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class NASPiRuntimeException extends RuntimeException {

    private String message;
    private HttpStatus status;
}

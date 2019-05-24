package it.pipodi.naspi.exceptions;

import lombok.Data;

@Data
public class ErrorResponse {

    private String errorMessage;
    private String cause;

}

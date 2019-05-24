package it.pipodi.naspi.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UploadResponse {

    private String fileName;
    private BigDecimal size;
}

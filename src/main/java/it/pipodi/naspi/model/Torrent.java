package it.pipodi.naspi.model;

import lombok.Data;

@Data
public class Torrent {
    private String id;
    private String name;
    private String progress;
    private String downloadSpeed;
    private String uploadSpeed;
}

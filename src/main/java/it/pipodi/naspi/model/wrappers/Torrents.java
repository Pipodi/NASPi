package it.pipodi.naspi.model.wrappers;

import it.pipodi.naspi.model.Torrent;
import lombok.Data;

import java.util.List;

@Data
public class Torrents {
    private List<Torrent> torrents;
}

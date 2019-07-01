package it.pipodi.naspi.model;

import lombok.Data;

@Data
public class TorrentRequest {

	private TorrentType type;

	private String title;
}

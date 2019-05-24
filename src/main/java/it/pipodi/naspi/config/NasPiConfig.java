package it.pipodi.naspi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class NasPiConfig {

    @Value("${path.base}")
    private String rootFolder;

    @Value("${path.torrent.files}")
    private String torrentFileFolder;

    @Value("${path.torrent.downloads}")
    private String torrentDownloadsFolder;

}

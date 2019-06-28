package it.pipodi.naspi.config;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
@Getter
public class NasPiConfig {

	private final static Logger logger = LoggerFactory.getLogger(NasPiConfig.class);

	@Value("${path.base}")
	private String rootFolder;

	@Value("${path.torrent.files}")
	private String torrentFileFolder;

}

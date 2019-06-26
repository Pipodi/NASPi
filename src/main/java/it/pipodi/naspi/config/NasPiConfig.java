package it.pipodi.naspi.config;

import it.pipodi.naspi.exceptions.NASPiRuntimeException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


@Configuration
@Getter
public class NasPiConfig {

	private final static Logger logger = LoggerFactory.getLogger(NasPiConfig.class);

	@Value("${path.base}")
	private String rootFolder;

	@Value("${path.torrent.files}")
	private String torrentFileFolder;

	@Value("${path.torrent.downloads}")
	private String torrentDownloadsFolder;

	@Value("${path.db}")
	private String dbPath;


	@Bean
	public Connection sqliteDB() {
		logger.debug("Creating SQLite database connection");
		try {
			String dbLink = String.format("jdbc:sqlite:%s", this.dbPath);
			return DriverManager.getConnection(dbLink);
		} catch (SQLException e) {
			throw new NASPiRuntimeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

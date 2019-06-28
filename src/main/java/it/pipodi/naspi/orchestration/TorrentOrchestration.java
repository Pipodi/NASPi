package it.pipodi.naspi.orchestration;

import it.pipodi.naspi.config.NasPiConfig;
import it.pipodi.naspi.exceptions.NASPiRuntimeException;
import it.pipodi.naspi.model.Torrent;
import it.pipodi.naspi.model.TorrentDownloadResponse;
import it.pipodi.naspi.model.TorrentRequest;
import it.pipodi.naspi.model.wrappers.Torrents;
import it.pipodi.naspi.services.FileManagerService;
import it.pipodi.naspi.utils.LinuxBashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Service
public class TorrentOrchestration {

	private static final Logger logger = LoggerFactory.getLogger(TorrentOrchestration.class);

	@Autowired
	private FileManagerService fileManagerService;

	@Autowired
	private NasPiConfig config;

	@Autowired
	private Connection db;

	/**
	 * Stores the file in the watch folder of transmission
	 *
	 * @param file torrent file to be stored
	 * @return response containing the path of
	 */
	public TorrentDownloadResponse startTorrentDownload(MultipartFile file, TorrentRequest infos) {
		logger.debug("startTorrentDownload() method called");
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		String initialFolder = String.format("%s/%s/%s", config.getRootFolder(),
				config.getTorrentDownloadsFolder(), fileName);
		TorrentDownloadResponse torrentDownloadResponse = new TorrentDownloadResponse();
		torrentDownloadResponse.setInitialPath(initialFolder);
		StringBuilder finalFolder = new StringBuilder();
		finalFolder.append(config.getRootFolder());
		String insertQuery;
		switch (infos.getType()) {
			case SERIES:
				finalFolder.append("/TVSeries/");
				insertQuery = "INSERT INTO series(title, final_folder, initial_folder) VALUES (?, ?, ?)";
				break;
			case MOVIE:
				finalFolder.append("/Movies/");
				insertQuery = "INSERT INTO movies(title, final_folder, initial_folder) VALUES (?, ?, ?)";
				break;
			default:
				logger.error("Wrong type in the request: {}", infos.getType());
				throw new NASPiRuntimeException("Wrong type in the request", HttpStatus.BAD_REQUEST);
		}
		finalFolder.append(infos.getTitle());

		torrentDownloadResponse.setFinalPath(finalFolder.toString());
		/*try {
			PreparedStatement statement = db.prepareStatement(insertQuery);
			statement.setString(1, infos.getTitle());
			statement.setString(2, finalFolder.toString());
			statement.setString(3, initialFolder);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new NASPiRuntimeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}*/
		String torrentPath = this.fileManagerService.uploadTorrent(file);
		String cmd = String.format("transmission-cli -w %s %s", finalFolder.toString(), torrentPath);
		LinuxBashUtils.executeBashCommand(cmd);
		return torrentDownloadResponse;
	}

	/**
	 * Get status of a given torrent
	 *
	 * @param id the id of the torrent
	 * @return the status
	 */
	public Torrent getTorrentStatus(long id) {
		logger.debug("getTorrentStatus() method called");
		Torrent torrent = new Torrent();
		String idString = LinuxBashUtils.executeBashCommand(String.format("transmission-remote -t %d -l | awk '{print" +
				" $1}'", id)).split("\n")[1];
		String progress = LinuxBashUtils.executeBashCommand(String.format("transmission-remote -t %d -l | awk '{print" +
				" $2}'", id)).split("\n")[1];
		String downloadSpeed = LinuxBashUtils.executeBashCommand(String.format("transmission-remote -t %d -l | awk '{print" +
				" $7}'", id)).split("\n")[1];
		String uploadSpeed = LinuxBashUtils.executeBashCommand(String.format("transmission-remote -t %d -l | awk " +
				"'{print $8}'", id)).split("\n")[1];
		String name = LinuxBashUtils.executeBashCommand(String.format("transmission-remote -t %d -l | awk '{print" +
				" $13}'", id)).split("\n")[1];
		torrent.setId(idString);
		torrent.setProgress(progress);
		torrent.setDownloadSpeed(downloadSpeed);
		torrent.setUploadSpeed(uploadSpeed);
		torrent.setName(name);
		return torrent;
	}

	/**
	 * Returns the list of all torrents currently downloading
	 *
	 * @return the list
	 */
	public Torrents getAllTorrents() {
		logger.debug("getAllTorrents() method called");
		Torrents torrents = new Torrents();
		List<Torrent> torrentList = new ArrayList<>();

		String id = LinuxBashUtils.executeBashCommand("transmission-remote -l | awk '{print $1}'");
		List<String> ids = new LinkedList<>(Arrays.asList(id.split("\n")));
		logger.debug(String.format("Raw IDs value: %s", String.valueOf(ids.size())));
		if (ids.size() == 2) {
			torrents.setTorrents(torrentList);
			return torrents;
		}
		ids.remove(0);
		ids.remove(ids.size() - 1);

		String progress = LinuxBashUtils.executeBashCommand("transmission-remote -l | awk '{print $2}'");
		List<String> progresses = new LinkedList<>(Arrays.asList(progress.split("\n")));
		logger.debug(String.format("Raw progresses value: %s", String.valueOf(progresses.size())));
		progresses.remove(0);
		progresses.remove(progresses.size() - 1);


		String dlSpeed = LinuxBashUtils.executeBashCommand("transmission-remote -l | awk '{print $7}'");
		List<String> dlSpeeds = new LinkedList<>(Arrays.asList(dlSpeed.split("\n")));
		logger.debug(String.format("Raw dlSpeeds value: %s", String.valueOf(dlSpeeds.size())));
		dlSpeeds.remove(0);


		String upSpeed = LinuxBashUtils.executeBashCommand("transmission-remote -l | awk '{print $8}'");
		List<String> upSpeeds = new LinkedList<>(Arrays.asList(upSpeed.split("\n")));
		logger.debug(String.format("Raw upSpeeds value: %s", String.valueOf(upSpeeds.size())));
		upSpeeds.remove(0);

		String name = LinuxBashUtils.executeBashCommand("transmission-remote -l | awk '{print $13}'");
		List<String> names = new LinkedList<>(Arrays.asList(name.split("\n")));
		logger.debug(String.format("Raw names value: %s", String.valueOf(names.size())));
		names.remove(0);


		logger.debug(String.format("IDs: %d", ids.size()));

		for (int i = 0; i < ids.size(); i++) {
			Torrent torrent = new Torrent();
			torrent.setId(ids.get(i));
			torrent.setDownloadSpeed(dlSpeeds.get(i));
			torrent.setName(names.get(i));
			torrent.setUploadSpeed(upSpeeds.get(i));
			torrent.setProgress(progresses.get(i));

			torrentList.add(torrent);
		}

		torrents.setTorrents(torrentList);

		return torrents;
	}

	/**
	 * Moves the file into the proper folder
	 *
	 * @param fileName name of the directory
	 */

	public void moveTorrent(String fileName) {
		logger.debug("moveTorrent() method called");
		logger.debug("Moving {}", fileName);

		String selectQueryMovies = "SELECT final_folder FROM movies WHERE initial_folder LIKE ?";

		String selectQuerySeries = "SELECT final_folder FROM series WHERE initial_folder LIKE ?";

		try {
			PreparedStatement statement = db.prepareStatement(selectQueryMovies);
			statement.setString(1, "%" + fileName + "%");
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				logger.debug("{} is not a movie, querying series table", fileName);
				statement = db.prepareStatement(selectQuerySeries);
				statement.setString(1, "%" + fileName + "%");
				ResultSet resultSetSeries = statement.executeQuery();
				if (!resultSetSeries.next()) {
					throw new NASPiRuntimeException(String.format("%s is neither a series nor a movie", fileName), HttpStatus.NOT_FOUND);
				} else {
					logger.debug("{} is a series. Moving to series directory.", fileName);
					moveFolder(fileName, resultSetSeries);
				}
			} else {
				logger.debug("{} is a movie. Moving to movies directory.", fileName);
				moveFolder(fileName, resultSet);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new NASPiRuntimeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private void moveFolder(String fileName, ResultSet resultSet) {
		String final_folder = null;
		try {
			final_folder = resultSet.getString("final_folder");
			String initial_folder = String.format("%s/%s/%s", config.getRootFolder(), config.getTorrentDownloadsFolder(), fileName);
			Files.move(Paths.get(initial_folder), Paths.get(final_folder), StandardCopyOption.REPLACE_EXISTING);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new NASPiRuntimeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new NASPiRuntimeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
}

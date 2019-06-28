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


	/**
	 * Stores the file in the watch folder of transmission
	 *
	 * @param file torrent file to be stored
	 * @return response containing the path of
	 */
	public TorrentDownloadResponse startTorrentDownload(MultipartFile file, TorrentRequest infos) {
		logger.debug("startTorrentDownload() method called");
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		TorrentDownloadResponse torrentDownloadResponse = new TorrentDownloadResponse();
		StringBuilder finalFolder = new StringBuilder();
		finalFolder.append(config.getRootFolder());
		switch (infos.getType()) {
			case SERIES:
				finalFolder.append("/TVSeries/");
				break;
			case MOVIE:
				finalFolder.append("/Movies/");
				break;
			default:
				logger.error("Wrong type in the request: {}", infos.getType());
				throw new NASPiRuntimeException("Wrong type in the request", HttpStatus.BAD_REQUEST);
		}

		String torrentPath = this.fileManagerService.uploadTorrent(file);
		String cmd = String.format("transmission-remote -a %s -w %s", torrentPath, finalFolder.toString());
		LinuxBashUtils.executeBashCommand(cmd);
		finalFolder.append(fileName);
		torrentDownloadResponse.setFinalPath(finalFolder.toString());
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
}

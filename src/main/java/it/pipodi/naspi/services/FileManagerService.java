package it.pipodi.naspi.services;

import it.pipodi.naspi.config.NasPiConfig;
import it.pipodi.naspi.exceptions.NASPiRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class FileManagerService {

	@Autowired
	private NasPiConfig config;

	private static final Logger logger = LoggerFactory.getLogger(FileManagerService.class);


	/**
	 * Reads a file from the filesystem and sends it back to the user
	 *
	 * @param fileName  the name of the file, including the extension
	 * @param subFolder the subfolder containing the file
	 * @return the file as Resource
	 */
	public Resource loadFileAsResource(String fileName, String subFolder) {
		logger.debug("loadFileAsResource() method called");
		try {
			Path filePath =
					Path.of(String.format("%s/%s/%s", config.getRootFolder(), subFolder, fileName)).normalize();
			Resource fileResource = new UrlResource(filePath.toUri());
			if (fileResource.exists()) {
				return fileResource;
			}
		} catch (MalformedURLException e) {
			logger.error("Exception in loadFileAsResource() method: {}", e.getMessage());
			throw new NASPiRuntimeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		throw new NASPiRuntimeException(String.format("%s was not found.", fileName), HttpStatus.NOT_FOUND);
	}

	/**
	 * Parses the extension of a given file name
	 *
	 * @param fileName name of the file, including the extension
	 * @return the extension
	 */
	public static String parseExtension(String fileName) {
		logger.debug("parseExtension() method called");
		return fileName.substring(fileName.lastIndexOf("."));
	}

	/**
	 * Stores a file on the filesystem
	 *
	 * @param file      file to be stored
	 * @param subfolder subfolder where the file will be stored
	 */
	public void uploadFile(MultipartFile file, String subfolder) {
		logger.debug("uploadFile() method called");
		try {
			Path targetLocation = Path.of(String.format("%s/%s", config.getRootFolder(), subfolder))
					.resolve(file.getOriginalFilename());
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error("Exception in uploadFile() method: {}", e.getMessage());
			throw new NASPiRuntimeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * Stores a torrent file in the watch folder of transmission
	 *
	 * @param file torrent file to be started
	 */
	public void uploadTorrent(MultipartFile file) {
		logger.debug("uploadTorrent() method called");
		try {
			Path targetLocation = Path.of(String.format("%s/%s", config.getRootFolder(),
					config.getTorrentFileFolder())).resolve(file.getOriginalFilename());
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error("Exception in uploadTorrent() method: {}", e.getMessage());
			throw new NASPiRuntimeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

package it.pipodi.naspi.orchestration;

import it.pipodi.naspi.config.NasPiConfig;
import it.pipodi.naspi.exceptions.NASPiRuntimeException;
import it.pipodi.naspi.model.Series;
import it.pipodi.naspi.model.UploadResponse;
import it.pipodi.naspi.model.wrappers.TVSeries;
import it.pipodi.naspi.services.FileManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TVSeriesOrchestration {

	private static final Logger logger = LoggerFactory.getLogger(TVSeriesOrchestration.class);

	@Autowired
	private NasPiConfig config;

	@Autowired
	private FileManagerService fileManagerService;


	/**
	 * Returns the movies list (with the filenames) in the Movies folder of the NAS
	 *
	 * @return the list
	 */
	public TVSeries getSeriesList() {
		logger.info("getSeriesList() method called");
		String seriesFolder = String.format("%s/%s", config.getRootFolder(), "Series");
		try {
			TVSeries tvSeries = new TVSeries();
			List<Series> seriesList = new ArrayList<>();
			Stream<Path> walk = Files.walk(Paths.get(seriesFolder));
			List<File> files = walk.filter(Files::isRegularFile).map(x -> x.toFile()).collect(Collectors.toList());
			for (File file : files) {
				Series series = new Series();
				series.setFilename(file.getName());
				series.setName(parseName(file.getName()));
				seriesList.add(series);
			}
			tvSeries.setTvSeries(seriesList);
			return tvSeries;
		} catch (IOException e) {
			logger.error("Exception in getMoviesList() method: {}", e.getMessage());
			throw new NASPiRuntimeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Loads the file and sends to the endpoint for the download
	 *
	 * @param fileName the file name, including the extension
	 * @return the file as Resource
	 */
	public Resource downloadFile(String fileName) {
		logger.info("downloadFile() method called");
		return fileManagerService.loadFileAsResource(fileName, "TvSeries");
	}

	/**
	 * Gets the file from the endpoint and stores it into the Movies folder
	 *
	 * @param file file to be stored
	 * @return response containing the name and the size of the file
	 */
	public UploadResponse uploadFile(MultipartFile file) {
		logger.info("uploadFile() method called");
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		this.fileManagerService.uploadFile(file, "TVSeries");
		UploadResponse uploadResponse = new UploadResponse();
		uploadResponse.setFileName(fileName);
		uploadResponse.setSize(byte2MegaByte(file.getSize()));
		return uploadResponse;
	}

	/**
	 * Converts the size of a file, from byte to Megabyte
	 *
	 * @param size size in byte
	 * @return the size in megabyte
	 */
	private BigDecimal byte2MegaByte(long size) {
		BigDecimal sizeInMB = new BigDecimal(size);
		return sizeInMB.divide(new BigDecimal(1000)).setScale(2, RoundingMode.UP);
	}

	/**
	 * Gets the file name without extension
	 *
	 * @param fileName the file name
	 * @return the file name without extension
	 */
	private String parseName(String fileName) {
		logger.info("parseName() method called");
		return fileName.substring(0, fileName.lastIndexOf("."));
	}


}

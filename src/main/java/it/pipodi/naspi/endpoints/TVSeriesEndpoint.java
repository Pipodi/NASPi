package it.pipodi.naspi.endpoints;

import it.pipodi.naspi.config.NasPiConfig;
import it.pipodi.naspi.model.UploadResponse;
import it.pipodi.naspi.model.wrappers.TVSeries;
import it.pipodi.naspi.orchestration.TVSeriesOrchestration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("tvseries")
public class TVSeriesEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(TVSeriesEndpoint.class);

	@Autowired
	private TVSeriesOrchestration orchestration;

	@Autowired
	private NasPiConfig config;

	/**
	 * GET endpoint that retrieves the list of the files in the TVSeries folder.
	 *
	 * @return ResponseEntity containing the list of files
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<TVSeries> tvSeriesList() {
		logger.debug("tvSeriesList() method called");
		return ResponseEntity.ok(this.orchestration.getSeriesList());
	}

	/**
	 * GET endpoint that downloads a specific file from TVSeries directory.
	 *
	 * @param fileName the file name, including the extension
	 * @return the file
	 */
	@RequestMapping(value = "/{fileName}", method = RequestMethod.GET)
	public ResponseEntity<Resource> downloadTvSeries(@PathVariable String fileName) {
		logger.debug("downloadTVSeries() method called");
		Resource file = this.orchestration.downloadFile(fileName);

		return ResponseEntity.ok()
				.contentType(MediaTypeFactory.getMediaType(file).get())
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	/**
	 * POST endpoint that uploads a file in the Movies directory
	 *
	 * @param file the file to be uploaded
	 * @return response indicating the name and the size of the uploaded file
	 */
	@RequestMapping(value = "/", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
		logger.debug("uploadFile() method called");
		return ResponseEntity.ok(this.orchestration.uploadFile(file));
	}

}

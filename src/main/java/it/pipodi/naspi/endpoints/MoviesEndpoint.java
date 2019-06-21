package it.pipodi.naspi.endpoints;

import it.pipodi.naspi.config.NasPiConfig;
import it.pipodi.naspi.model.UploadResponse;
import it.pipodi.naspi.model.wrappers.Movies;
import it.pipodi.naspi.orchestration.MoviesOrchestration;
import it.pipodi.naspi.services.MultipartFileSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("movies")
public class MoviesEndpoint {

	@Autowired
	private MoviesOrchestration moviesOrchestration;

	@Autowired
	private NasPiConfig config;

	private static final Logger logger = LoggerFactory.getLogger(MoviesEndpoint.class);

	/**
	 * GET endpoint that retrieves the list of the files in the Movies folder.
	 *
	 * @return ResponseEntity containing the list of files
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Movies> moviesList() {
		logger.debug("moviesList() method called");
		return ResponseEntity.ok(this.moviesOrchestration.getMoviesList());
	}

	/**
	 * GET endpoint that downloads a specific file from Movies directory.
	 *
	 * @param fileName the file name, including the extension
	 * @param request
	 * @return the file
	 */
	@RequestMapping(value = "/{fileName}", method = RequestMethod.GET)
	public ResponseEntity<Resource> downloadMovie(@PathVariable String fileName, HttpServletRequest request) {
		logger.debug("downloadMovie() method called");
		Resource file = this.moviesOrchestration.downloadFile(fileName);
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(file.getFile().getAbsolutePath());
		} catch (IOException e) {
			logger.error("Unable to determine the content type from the file.");
		}

		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
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
		return ResponseEntity.ok(this.moviesOrchestration.uploadFile(file));
	}

	/**
	 * GET endpoint that streams a movie saved on the filesystem
	 *
	 * @param request  httpservletrequest
	 * @param response httpservletresponse
	 * @param filename the name of the file, including the extension
	 */
	@RequestMapping(value = "/stream/{filename}", method = RequestMethod.GET)
	public void stream(HttpServletRequest request,
	                   HttpServletResponse response, @PathVariable String filename) {
		logger.debug("Starting streaming for file: {}", filename);
		String filepath = String.format("%s/Movies/%s", config.getRootFolder(), filename);
		Path path = Paths.get(filepath);
		try {
			MultipartFileSender.fromPath(path)
					.with(request)
					.with(response)
					.serveResource();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}
}

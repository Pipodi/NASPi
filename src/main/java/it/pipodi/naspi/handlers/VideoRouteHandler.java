package it.pipodi.naspi.handlers;

import it.pipodi.naspi.exceptions.NASPiRuntimeException;
import it.pipodi.naspi.services.VideoService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class VideoRouteHandler {

	private final VideoService videoService;

	@Autowired
	public VideoRouteHandler(VideoService videoService) {
		this.videoService = videoService;
	}

	public Mono<ServerResponse> returnPath(ServerRequest request) {
		return ServerResponse.ok().body(Mono.just(request.path()), String.class);
	}

	public Mono<ServerResponse> getPartialVideoByNameMovies(ServerRequest request) {

		String name = request.pathVariable("name");
		HttpHeaders requestHeaders = request.headers().asHttpHeaders();
		UrlResource video = videoService.getResourceByName(name, "Movies");

		return handleResponse(name, video, requestHeaders);
	}

	public Mono<ServerResponse> getPartialVideoByNameSeries(ServerRequest request) {
		String name = request.pathVariable("name");
		HttpHeaders requestHeaders = request.headers().asHttpHeaders();
		UrlResource video = videoService.getResourceByName(name, "Series");
		return handleResponse(name, video, requestHeaders);
	}

	private Mono<ServerResponse> handleResponse(String name, UrlResource video, HttpHeaders requestHeaders) {
		String extension = FilenameUtils.getExtension(name);
		MediaType mediaType = null;
		if (extension.equals("mkv")) {
			mediaType = MediaType.parseMediaType("video/mp4");
		} else {
			mediaType = MediaTypeFactory.getMediaType(video).get();
		}
		ResourceRegion resourceRegion = videoService.getRegion(video, requestHeaders);

		return ServerResponse
				.status(HttpStatus.PARTIAL_CONTENT)
				.contentType(mediaType)
				.contentLength(resourceRegion.getCount())
				.headers(headers -> headers.setCacheControl(CacheControl.noCache()))
				.body(Mono.just(resourceRegion), ResourceRegion.class)
				.flatMap(response -> {
					if (response.headers().getContentLength() == 0) {
						return Mono.error(new NASPiRuntimeException("Video not found", HttpStatus.NOT_FOUND));
					}
					return Mono.just(response);
				});
	}
}

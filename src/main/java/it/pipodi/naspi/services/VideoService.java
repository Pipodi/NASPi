package it.pipodi.naspi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;

import static java.lang.Long.min;

@Service
public class VideoService {
	private static int byteLength = 1024;
	private static long CHUNK_SIZE_LOW = byteLength * 256;
	private static long CHUNK_SIZE_MED = byteLength * 512;
	private static long CHUNK_SIZE_HIGH = byteLength * 1024;
	private static long CHUNK_SIZE_VERY_HIGH = CHUNK_SIZE_HIGH * 2;

	@Value("${path.base}")
	private String videoLocation;

	public ResourceRegion getRegion(UrlResource resource, HttpHeaders headers) {

		long contentLength = 0;
		try {
			contentLength = resource.contentLength();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpRange range = headers.getRange().size() != 0 ? headers.getRange().get(0) : null;

		if (range != null) {
			long start = range.getRangeStart(contentLength);
			long end = range.getRangeEnd(contentLength);
			long resourceLength = end - start + 1;
			long rangeLength = min(CHUNK_SIZE_MED, resourceLength);

			return new ResourceRegion(resource, start, rangeLength);
		} else {
			long rangeLength = min(CHUNK_SIZE_MED, contentLength);
			return new ResourceRegion(resource, 0, rangeLength);
		}
	}

	public UrlResource getResourceByName(String name, String folder) {
		UrlResource video = null;
		try {
			video = new UrlResource("file:" + videoLocation + '/' + folder + '/' + name);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return video;
	}
}

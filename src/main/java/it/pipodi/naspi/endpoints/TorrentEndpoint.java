package it.pipodi.naspi.endpoints;

import it.pipodi.naspi.model.Torrent;
import it.pipodi.naspi.model.TorrentDownloadResponse;
import it.pipodi.naspi.model.wrappers.Torrents;
import it.pipodi.naspi.orchestration.TorrentOrchestration;
import it.pipodi.naspi.services.FileManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("torrent")
public class TorrentEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(TorrentEndpoint.class);

    @Autowired
    private TorrentOrchestration torrentOrchestration;

    /**
     * POST endpoint that uploads a torrent file in order to start downloading it
     *
     * @param file torrent file to be downloaded on the NAS
     * @return response indicating the path of the downloaded file
     */
    @RequestMapping(value = "/", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TorrentDownloadResponse> startTorrentDownload(@RequestParam("file") MultipartFile file) {
        logger.info("startTorrentDownload() method called");
        if (!FileManagerService.parseExtension(file.getOriginalFilename()).equals(".torrent")) {
            TorrentDownloadResponse torrentDownloadResponse = new TorrentDownloadResponse();
            torrentDownloadResponse.setPath("");
            return ResponseEntity.badRequest().body(torrentDownloadResponse);
        }
        return ResponseEntity.ok(this.torrentOrchestration.startTorrentDownload(file));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Torrent> getTorrentStatus(@PathVariable long id) {
        logger.info("getTorrentStatus() method called");
        return ResponseEntity.ok(this.torrentOrchestration.getTorrentStatus(id));
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Torrents> getTorrentList() {
        logger.info("getTorrentStatus() method called");
        return ResponseEntity.ok(this.torrentOrchestration.getAllTorrents());
    }
}

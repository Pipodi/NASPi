package it.pipodi.nascontroller.endpoints;

import it.pipodi.nascontroller.orchestration.HealthcheckOrchestration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("healthcheck")
public class HealthcheckEndpoint {

    @Autowired
    private HealthcheckOrchestration orchestration;

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String>healthcheck(){
        return ResponseEntity.ok().body(this.orchestration.generateHealthcheck());
    }
}

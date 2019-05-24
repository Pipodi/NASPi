package it.pipodi.naspi.orchestration;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HealthcheckOrchestration {

    /**
     * Generates an healthcheck string
     *
     * @return healthcheck string
     */
    public String generateHealthcheck() {
        LocalDateTime today = LocalDateTime.now();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("NASPi - NAS Controller for RPi3\n");
        stringBuilder.append("Alive and kicking, server time: \n");
        stringBuilder.append(today.toString());

        return stringBuilder.toString();
    }
}

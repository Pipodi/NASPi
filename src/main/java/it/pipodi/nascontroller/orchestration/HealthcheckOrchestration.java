package it.pipodi.nascontroller.orchestration;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HealthcheckOrchestration {

    public String generateHealthcheck(){
        LocalDateTime today = LocalDateTime.now();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("NAS Controller - v0.1 \n");
        stringBuilder.append("Alive and kicking, server time: \n");
        stringBuilder.append(today.toString());

        return stringBuilder.toString();
    }
}

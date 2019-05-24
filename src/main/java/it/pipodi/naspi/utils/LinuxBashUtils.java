package it.pipodi.naspi.utils;

import it.pipodi.naspi.exceptions.NASPiRuntimeException;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LinuxBashUtils {

    public static String executeBashCommand(String bashCommand) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", bashCommand);

        try {
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return output.toString();
            } else {
                throw new NASPiRuntimeException(String.format("Process execution exited with code: %d", exitCode),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (IOException e) {
            throw new NASPiRuntimeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}

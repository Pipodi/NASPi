package it.pipodi.naspi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NasPi implements ApplicationRunner {

	private static Logger logger = LoggerFactory.getLogger(NasPi.class);


	public static void main(String[] args) {
		SpringApplication.run(NasPi.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

	}
}

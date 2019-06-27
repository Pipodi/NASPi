package it.pipodi.naspi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.content.commons.repository.Store;
import org.springframework.content.rest.StoreRestResource;

@SpringBootApplication
public class NasPi {

	private static Logger logger = LoggerFactory.getLogger(NasPi.class);


	public static void main(String[] args) {
		SpringApplication.run(NasPi.class, args);
	}

	@StoreRestResource(path = "stream")
	public interface VideoStore extends Store<String> {
	}
}

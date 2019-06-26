package it.pipodi.naspi.handlers;

import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class ErrorHandler {

	public static Mono<ServerResponse> handleError(Throwable throwable) {
		return ServerResponse.badRequest().build();
	}
}

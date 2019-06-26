package it.pipodi.naspi.config;

import it.pipodi.naspi.exceptions.NASPiRuntimeException;
import it.pipodi.naspi.handlers.ErrorHandler;
import it.pipodi.naspi.handlers.VideoRouteHandler;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.thymeleaf.spring5.ISpringWebFluxTemplateEngine;
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Getter
@EnableWebFlux
public class NasPiConfig implements WebFluxConfigurer {

	private final static Logger logger = LoggerFactory.getLogger(NasPiConfig.class);

	@Value("${path.base}")
	private String rootFolder;

	@Value("${path.torrent.files}")
	private String torrentFileFolder;

	@Value("${path.torrent.downloads}")
	private String torrentDownloadsFolder;

	@Value("${path.db}")
	private String dbPath;

	private final ISpringWebFluxTemplateEngine templateEngine;

	public NasPiConfig(ISpringWebFluxTemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	@Bean
	public ThymeleafReactiveViewResolver thymeleafChunkAndDataDrivenViewResolver() {
		final ThymeleafReactiveViewResolver viewResolver = new ThymeleafReactiveViewResolver();
		viewResolver.setTemplateEngine(templateEngine);
		viewResolver.setOrder(1);
		viewResolver.setResponseMaxChunkSizeBytes(8192);
		return viewResolver;
	}

	@Bean
	public Connection sqliteDB() {
		logger.debug("Creating SQLite database connection");
		try {
			String dbLink = String.format("jdbc:sqlite:%s", this.dbPath);
			return DriverManager.getConnection(dbLink);
		} catch (SQLException e) {
			throw new NASPiRuntimeException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Bean
	RouterFunction<ServerResponse> videoEndPoint(VideoRouteHandler videoRouteHandler) {


		return route(GET("/fra"), videoRouteHandler::returnPath)
				.andRoute(GET("/movies/stream/{name}"), videoRouteHandler::getPartialVideoByNameMovies)
				.andRoute(GET("/series/stream/{name}"), videoRouteHandler::getPartialVideoByNameSeries)
				.filter((request, next) -> next.handle(request)
						.onErrorResume(ErrorHandler::handleError));
	}

	@Override
	public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
		configurer.customCodecs().writer(new ResourceRegionMessageWriter());
	}

}

package pl.edu.agh.to.reaktywni;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.edu.agh.to.reaktywni.model.Image;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;


@Component
public class ServerClient {

    private static final Logger logger = Logger.getLogger(ServerClient.class.getName());

    private final WebClient webClient;

    public ServerClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public Mono<Image> getFullImage(int id) {
        return webClient.get()
            .uri("/images/{id}", id)
            .retrieve()
            .bodyToMono(Image.class)
            .doOnError(e -> logger.warning("getFullImageError: " + e.getMessage()));
    }

    public Flux<Image> sendImages(Flux<Image> images, String thumbnailSize) {
        return webClient.post()
                .uri("/images?size={size}", thumbnailSize)
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(images, Image.class)
                .retrieve()
                .bodyToFlux(Image.class);
    }

    public Flux<Image> getThumbnails(String thumbnailSize) {
        return webClient.get()
                .uri("/thumbnails?size={size}", thumbnailSize)
                .retrieve()
                .bodyToFlux(Image.class)
                .doOnError(e -> logger.warning("getThumbnailsError: " + e.getMessage()));
    }

    public Mono<Long> getThumbnailsCount(String thumbnailSize) {
        return webClient.get()
                .uri("/thumbnails/count?size={size}", thumbnailSize)
                .retrieve()
                .bodyToMono(Long.class)
                .doOnError(e -> logger.warning("getThumbnailsCountError: " + e.getMessage()));
    }
}




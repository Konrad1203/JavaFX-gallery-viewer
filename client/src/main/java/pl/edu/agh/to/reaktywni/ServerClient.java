package pl.edu.agh.to.reaktywni;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.edu.agh.to.reaktywni.model.Image;
import pl.edu.agh.to.reaktywni.util.Base64ImageDataEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ServerClient {

    private final WebClient webClient;

    public ServerClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public Mono<Image> getFullImage(int id) {
        return webClient.get()
            .uri("/images/{id}", id)
            .retrieve()
            .bodyToMono(Image.class)
            .doOnError(e -> System.err.println("Error: " + e.getMessage()))
            // Symulowanie opóźnienia sieciowego
            .delayElement(java.time.Duration.ofMillis(1000));
    }

    public Flux<Image> sendImages(Flux<Image> images) {
        images.map(Base64ImageDataEncoder::encode)
                .doOnNext(image -> System.out.println("Send: " + image.getName() + " | GridID: " + image.getGridPlacementId() + " | DB_ID: " + image.getDatabaseId()));

        return webClient.post()
                .uri("/images")
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(images, Image.class)
                .retrieve()
                .bodyToFlux(Image.class)
                .doOnError(e -> System.err.println("Error: " + e.getMessage()));
    }

    public Flux<Image> getThumbnails() {
        return webClient.get()
                .uri("/images")
                .retrieve()
                .bodyToFlux(Image.class)
                .doOnError(e -> System.err.println("Error: " + e.getMessage()))
                // Symulowanie opóźnienia sieciowego
                .delayElements(java.time.Duration.ofMillis(500));
    }

    public Mono<Long> getImagesCount() {
        return webClient.get()
                .uri("/images/count")
                .retrieve()
                .bodyToMono(Long.class)
                .doOnError(e -> System.err.println("Error: " + e.getMessage()));
    }
}




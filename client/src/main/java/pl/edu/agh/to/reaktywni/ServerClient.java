package pl.edu.agh.to.reaktywni;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.edu.agh.to.reaktywni.model.ImageDTO;
import pl.edu.agh.to.reaktywni.util.Base64ImageDataEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ServerClient {

    private final WebClient webClient;

    public ServerClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public ImageDTO getFullImage(int id) {
        ImageDTO imageDTO = webClient.get()
            .uri("/images/{id}", id)
            .retrieve()
            .bodyToMono(ImageDTO.class)
            .doOnError(e -> System.err.println("Error: " + e.getMessage()))
            .block();

        if (imageDTO == null) {
            throw new RuntimeException("Image not found");
        }
        System.out.println("Received image: " + imageDTO.getName() + " Size: " + imageDTO.getWidth() + "x" + imageDTO.getHeight());
        return imageDTO;
    }

    public Flux<ImageDTO> sendImages(Flux<ImageDTO> images) {
        images.map(Base64ImageDataEncoder::encode);

        return webClient.post()
                .uri("/images")
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(images, ImageDTO.class)
                .retrieve()
                .bodyToFlux(ImageDTO.class)
                .doOnError(e -> System.err.println("Error: " + e.getMessage()));
    }

    public Flux<ImageDTO> getThumbnails() {
        return webClient.get()
                .uri("/images")
                .retrieve()
                .bodyToFlux(ImageDTO.class)
                .doOnError(e -> System.err.println("Error: " + e.getMessage()));
    }

    public Mono<Long> getImagesCount() {
        return webClient.get()
                .uri("/images/count")
                .retrieve()
                .bodyToMono(Long.class)
                .doOnError(e -> System.err.println("Error: " + e.getMessage()));
    }
}




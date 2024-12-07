package pl.edu.agh.to.reaktywni;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.edu.agh.to.reaktywni.model.ImageDTO;
import pl.edu.agh.to.reaktywni.util.ImageDTOArrayEncoder;
import reactor.core.publisher.Flux;

import java.util.Base64;

@Component
public class ServerClient {

    private final WebClient webClient;

    public ServerClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public Flux<ImageDTO> sendImages(Flux<ImageDTO> images) {
        images.map(ImageDTOArrayEncoder::encode);

        return webClient.post()
                .uri("/images")
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(images, ImageDTO.class)
                .retrieve()
                .bodyToFlux(ImageDTO.class)
                .doOnError(e -> System.err.println("Error: " + e.getMessage()));
    }
}




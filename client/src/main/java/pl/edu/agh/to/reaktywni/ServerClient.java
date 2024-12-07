package pl.edu.agh.to.reaktywni;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class ServerClient {

    private final WebClient webClient;

    public ServerClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public Flux<String> sendStrings(Flux<String> strings) {
        return webClient.post()
                .uri("/images/process-strings")
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(strings, String.class)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(e -> System.err.println("Error: " + e.getMessage()));
    }
}




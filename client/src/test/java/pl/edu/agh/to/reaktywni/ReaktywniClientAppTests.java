package pl.edu.agh.to.reaktywni;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import pl.edu.agh.to.reaktywni.model.Image;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ReaktywniClientAppTests {

	@Mock
	private WebClient.Builder webClientBuilder;

	@Mock
	private WebClient webClient;

	@Mock
	private WebClient.RequestBodyUriSpec requestBodyUriSpec;

	@Mock
	private WebClient.ResponseSpec responseSpec;

	private ServerClient serverClient;

	private Image loadImage(String imageName, int id) {
		return Image.createFromFile(id, new File("src/test/resources/" + imageName));
	}

	@BeforeEach
	void setUp() {
		when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
		when(webClientBuilder.build()).thenReturn(webClient);
		serverClient = new ServerClient(webClientBuilder);
	}

	@Test
	public void testSendImages() {
		Flux<Image> images = Flux.just(
				loadImage("test-pepe.jpg", 0),
				loadImage("test-png.png", 1)
		);

		when(webClient.post()).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(eq(MediaType.APPLICATION_NDJSON))).thenReturn(requestBodyUriSpec);
		doAnswer(invocation -> requestBodyUriSpec).when(requestBodyUriSpec).body(any(), eq(Image.class));
		when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToFlux(eq(Image.class))).thenReturn(Flux.just(
				loadImage("test-pepe.jpg", 0),
				loadImage("test-png.png", 1)
		));

		Flux<Image> result = serverClient.sendImages(images);

		StepVerifier.create(result)
				.expectNextMatches(image -> image.getName().equals("test-pepe.jpg"))
				.expectNextMatches(image -> image.getName().equals("test-png.png"))
				.verifyComplete();
	}
}

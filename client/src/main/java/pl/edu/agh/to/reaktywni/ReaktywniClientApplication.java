package pl.edu.agh.to.reaktywni;

import javafx.application.Application;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.util.List;

@SpringBootApplication
public class ReaktywniClientApplication {

	private final ServerClient serverClient;

	public ReaktywniClientApplication(ServerClient serverClient) {
		this.serverClient = serverClient;
	}

	public static void main(String[] args) {
		Application.launch(ImageGalleryApp.class, args);
	}
	/*
	@Override
	public void run(String... args) {
		List<String> strings = List.of("hello", "world", "reactive", "spring", "boot", "webflux", "flux", "mono", "client", "server");

		serverClient.sendStrings(Flux.fromIterable(strings))
				.doOnNext(processed -> System.out.println("Received from server: " + processed))
				.blockLast();
	}
	 */
}

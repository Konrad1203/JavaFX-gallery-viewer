package pl.edu.agh.to.reaktywni;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;


@SpringBootApplication
@Profile({"dev", "prod"})
public class ReaktywniClientApp {

	public static void main(String[] args) {
		Application.launch(ImageGalleryApp.class, args);
	}
}
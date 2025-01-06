package pl.edu.agh.to.reaktywni.GUI;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import pl.edu.agh.to.reaktywni.ImageGalleryApp;
import pl.edu.agh.to.reaktywni.model.Image;


@Component
public class StageInitializer {

    private final ApplicationContext applicationContext;

    public StageInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void openBigImageView(Mono<Image> imageMono) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/big_image_view.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            BigImagePresenter presenter = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Selected Image View");
            stage.getIcons().add(ImageGalleryApp.icon);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

            imageMono.subscribe(
                    image -> {
                        javafx.scene.image.Image javaFXImage = new javafx.scene.image.Image(new ByteArrayInputStream(image.getData()));
                        Platform.runLater(() -> {
                            presenter.setName(image.getName());
                            presenter.setSize(image.getWidth(), image.getHeight());
                            presenter.setImage(javaFXImage);
                        });
                    },
                    error -> Platform.runLater(() -> presenter.setErrorImage(error.getMessage())));

        } catch (IOException e) {
            throw new RuntimeException("Failed loading FXML file: ", e);
        }
    }
}



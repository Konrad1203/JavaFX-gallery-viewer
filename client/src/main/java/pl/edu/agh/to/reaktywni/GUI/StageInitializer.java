package pl.edu.agh.to.reaktywni.GUI;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.ImageGalleryApp;
import pl.edu.agh.to.reaktywni.model.Image;
import reactor.core.publisher.Mono;

import java.io.IOException;


@Component
public class StageInitializer implements ApplicationListener<ImageGalleryApp.StageReadyEvent> {

    private final ApplicationContext applicationContext;


    public StageInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ImageGalleryApp.StageReadyEvent event) {
        Stage stage = event.getStage();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/image_gallery_view.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            stage.setTitle("Image Gallery");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException("Failed loading FXML file: ", e);
        }
    }

    public void openBigImageView(Mono<Image> imageMono) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/big_image_view.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            BigImagePresenter presenter = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Selected Image View");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

            imageMono.subscribe(image -> Platform.runLater(() -> {
                if (image == null) throw new RuntimeException("Failed loading image");
                presenter.setName(image.getName());
                presenter.setSize(image.getWidth(), image.getHeight());
                presenter.setImage(image.getData());
            }));

        } catch (IOException e) {
            throw new RuntimeException("Failed loading FXML file: ", e);
        }
    }
}



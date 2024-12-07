package pl.edu.agh.to.reaktywni.GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.ImageGalleryApp;

import java.io.IOException;
import java.net.URL;

@Component
public class StageInitializer implements ApplicationListener<ImageGalleryApp.StageReadyEvent> {

    private final ApplicationContext applicationContext;

    public StageInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ImageGalleryApp.StageReadyEvent event) {
        Stage stage = event.getStage();

        URL resource = getClass().getResource("/GUI/image_gallery_view.fxml");
        if (resource == null) {
            System.out.println("Plik FXML nie został znaleziony!");
            throw new RuntimeException("FXML file not found: /GUI/image_gallery_view.fxml");
        }

        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(applicationContext::getBean);

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            System.out.println("Failed loading FXML file");
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Image Gallery");
        stage.setScene(scene);
        stage.show();
    }
}



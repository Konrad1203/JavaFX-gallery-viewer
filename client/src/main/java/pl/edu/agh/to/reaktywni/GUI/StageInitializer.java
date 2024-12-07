package pl.edu.agh.to.reaktywni.GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.ImageGalleryApp;

import java.io.IOException;

@Component
public class StageInitializer implements ApplicationListener<ImageGalleryApp.StageReadyEvent> {

    @Override
    public void onApplicationEvent(ImageGalleryApp.StageReadyEvent event) {
        Stage stage = event.getStage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/image_gallery_view.fxml"));

        Parent root;
        try { root = loader.load(); }
        catch (IOException e) {System.out.println("Failed loading fxml file"); throw new RuntimeException(e); }

        ImageGalleryPresenter presenter = loader.getController();
        presenter.setPrimaryStage(stage);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Image Gallery");
        stage.setScene(scene);
        stage.show();
    }
}

package pl.edu.agh.to.reaktywni.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Objects;


@Component
public class BigImagePresenter {

    @FXML
    private ImageView imageView;

    @FXML
    private Label nameLabel;

    @FXML
    private Label sizeLabel;

    private static final Image loadingDots = new Image(
            Objects.requireNonNull(BigImagePresenter.class.getResourceAsStream("/GUI/loading.gif")),
            620, 349, true, true
    );

    public void initialize() {
        imageView.setImage(loadingDots);
    }

    public void setImage(byte[] imageData) {
        imageView.setImage(new Image(new ByteArrayInputStream(imageData)));
    }

    public void setName(String name) {
        nameLabel.setText(name);
    }

    public void setSize(int width, int height) {
        sizeLabel.setText(width + "x" + height);
    }
}

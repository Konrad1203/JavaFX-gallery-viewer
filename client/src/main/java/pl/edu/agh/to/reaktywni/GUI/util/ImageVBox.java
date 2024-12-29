package pl.edu.agh.to.reaktywni.GUI.util;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.Getter;
import pl.edu.agh.to.reaktywni.GUI.StageInitializer;
import pl.edu.agh.to.reaktywni.model.Image;
import pl.edu.agh.to.reaktywni.model.ImagePipeline;
import pl.edu.agh.to.reaktywni.model.ImageState;
import java.io.ByteArrayInputStream;


public class ImageVBox extends VBox {

    @Getter
    private final ImageView imageView = new ImageView();

    private final Label nameLabel = new Label();

    public ImageVBox(ThumbnailSize size, String name, int image_name_height) {
        imageView.setImage(size.getPlaceholder());
        nameLabel.setText(name);
        nameLabel.setWrapText(true);
        setMinHeight(size.getImageHeight() + image_name_height);
        setMaxHeight(size.getImageHeight() + image_name_height);
        setAlignment(Pos.TOP_CENTER);
        getChildren().addAll(imageView, nameLabel);
    }

    public void placeImage(ThumbnailSize size, Image image, StageInitializer stageInitializer, ImagePipeline imagePipeline) {
        if (image.getImageState() == ImageState.SUCCESS) {
            if (!isNameFilled()) nameLabel.setText(image.getName());
            imageView.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(image.getData()), size.getImageWidth(), size.getImageHeight(), false, false));
        } else {
            nameLabel.setText("Error: " + image.getName());
            imageView.setImage(size.getErrorImage());
        }
        setOnMouseClicked(event ->
                stageInitializer.openBigImageView(imagePipeline.getFullImage(image.getId())));
    }

    public boolean isNameFilled() {
        return !nameLabel.getText().startsWith("...");
    }

    public void changeVBoxSize(ThumbnailSize size, int image_name_height) {
        setMinHeight(size.getImageHeight() + image_name_height);
        setMaxHeight(size.getImageHeight() + image_name_height);
        imageView.setImage(size.getPlaceholder());
    }
}

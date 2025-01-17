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

    private final int startGridId;

    public ImageVBox(int startGridId, ThumbnailSize size, String name, int imageNameHeight) {
        this.startGridId = startGridId;
        imageView.setImage(size.getPlaceholder());
        nameLabel.setText(name);
        nameLabel.setWrapText(true);
        setMinHeight(size.getImageHeight() + imageNameHeight);
        setMaxHeight(size.getImageHeight() + imageNameHeight);
        setAlignment(Pos.TOP_CENTER);
        getChildren().addAll(imageView, nameLabel);
    }

    public int getGridId() {
        return startGridId;
    }

    public void placeImage(ThumbnailSize size, Image image, StageInitializer stageInitializer, ImagePipeline imagePipeline) {
        if (image.getImageState() == ImageState.SUCCESS) {
            nameLabel.setText(image.getName());
            imageView.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(image.getData()), size.getImageWidth(), size.getImageHeight(), false, false));
            setOnMouseClicked(event -> stageInitializer.openBigImageView(imagePipeline.getFullImage(image.getId())));
        } else if (image.getImageState() == ImageState.PENDING) {
            nameLabel.setText("............");
            imageView.setImage(size.getPlaceholder());
        }
        else {
            nameLabel.setText("Error: " + image.getName());
            imageView.setImage(size.getErrorImage());
        }
    }
}

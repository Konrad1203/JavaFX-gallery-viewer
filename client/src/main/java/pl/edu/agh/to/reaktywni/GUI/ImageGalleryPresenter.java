package pl.edu.agh.to.reaktywni.GUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.model.FilesToImagesConverter;
import pl.edu.agh.to.reaktywni.model.Image;
import pl.edu.agh.to.reaktywni.model.ImagePipeline;
import pl.edu.agh.to.reaktywni.model.ImageState;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;

@Component
public class ImageGalleryPresenter {

    @FXML
    private Label filesSelectedLabel;

    @FXML
    private GridPane gridPane;

    private int gridIndex = 0;

    private List<File> files;

    private final Map<Integer, ImageVBox> imageVBoxFromGridPlacementId = new HashMap<>();

    private static final javafx.scene.image.Image placeholder = new javafx.scene.image.Image(
            Objects.requireNonNull(ImageGalleryPresenter.class.getResourceAsStream("/GUI/loading.gif")),
            200, 120, false, true
    );

    private static final javafx.scene.image.Image error = new javafx.scene.image.Image(
            Objects.requireNonNull(ImageGalleryPresenter.class.getResourceAsStream("/GUI/error.png")),
            200, 120, false, true
    );

    private final ImagePipeline imagePipeline;
    private final StageInitializer stageInitializer;


    public ImageGalleryPresenter(ImagePipeline imagePipeline, StageInitializer stageInitializer) {
        this.imagePipeline = imagePipeline;
        this.stageInitializer = stageInitializer;
    }

    public void initialize() {
        imagePipeline.setPresenter(this);
        Long count = imagePipeline.getImagesCount().block();
        if (count == null) throw new RuntimeException("Nie udało się pobrać liczby obrazów");
        addPlaceholdersToGrid(count);
        imagePipeline.getThumbnails()
                .doOnNext(this::placeImageToGrid)
                .doOnComplete(() -> System.out.println("Wczytano wszystkie obrazy"))
                .subscribe();
    }

    @FXML
    private void openSelectionWindow() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz obrazy");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        files = fileChooser.showOpenMultipleDialog(null);
        if (files != null) {
            filesSelectedLabel.setText("Wybrano " + files.size() + " obrazów");
            filesSelectedLabel.setVisible(true);
        }
    }

    @FXML
    private void sendAndReceiveImages() {
        if (files == null) return;
        List<Image> imagesToSend = FilesToImagesConverter.convertWithPositionsCounting(files, gridIndex);
        addPlaceholdersToGrid(imagesToSend);
        new Thread(() -> imagePipeline.sendAndReceiveImages(imagesToSend)).start();
    }

    private void createRowsIfRequired(long count) {
        Platform.runLater(() -> {
            int requiredRows = (int) ((gridIndex + count) / gridPane.getColumnCount()) + 1;
            for (int i = gridPane.getRowCount(); i < requiredRows; i++) {
                gridPane.addRow(i);
                gridPane.getRowConstraints().add(gridPane.getRowConstraints().getFirst());
            }
        });
    }

    // To be removed if the server sends the images names
    private void addPlaceholdersToGrid(long count) {
        createRowsIfRequired(count);
        for (int i = 0; i < count; i++) {
            final int index = i;
            Platform.runLater(() ->
                    gridPane.add(new ImageView(placeholder),
                            index % gridPane.getColumnCount(),
                            index / gridPane.getColumnCount()
                    )
            );
        }
    }

    // To be removed if the server sends the images names
    private void placeImageToGrid(Image image) {
        ImageVBox imageVBox = new ImageVBox(image.getName());
        if (image.getImageState() == ImageState.FAILURE) {
            imageVBox.imageView.setImage(error);
            imageVBox.setName("Błąd: " + image.getName());
        } else {
            imageVBox.setImage(image);
        }
        Platform.runLater(() -> {
            final int index = gridIndex++;
            final int row = index / gridPane.getColumnCount();
            final int col = index % gridPane.getColumnCount();
            gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col);
            gridPane.add(imageVBox, index % gridPane.getColumnCount(), index / gridPane.getColumnCount());
        });
    }

    private void addPlaceholdersToGrid(List<Image> images) {
        createRowsIfRequired(images.size());
        for (Image image : images) {
            ImageVBox imageVBox = new ImageVBox(image.getName());
            final int index = gridIndex++;
            imageVBoxFromGridPlacementId.put(image.getGridPlacementId(), imageVBox);
            Platform.runLater(() -> gridPane.add(imageVBox, index % gridPane.getColumnCount(), index / gridPane.getColumnCount()));
        }
    }

    public void replacePlaceholderWithImage(Image image) {
        Platform.runLater(() -> {
            ImageVBox imageVBox = imageVBoxFromGridPlacementId.remove(image.getGridPlacementId());
            if (image.getImageState() == ImageState.FAILURE) {
                imageVBox.imageView.setImage(error);
                imageVBox.setName("Błąd: " + image.getName());
                return;
            }
            imageVBox.setImage(image);
        });
    }


    private class ImageVBox extends VBox {

        private final ImageView imageView = new ImageView();

        private final Label nameLabel = new Label();

        public ImageVBox(String name) {
            imageView.setImage(placeholder);
            nameLabel.setText(name);
            nameLabel.setWrapText(true);
            setMaxHeight(160);
            setAlignment(Pos.TOP_CENTER);
            getChildren().addAll(imageView, nameLabel);
        }

        public void setImage(Image image) {
            imageView.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(image.getData()), 200, 120, false, false));
            setOnMouseClicked(event ->
                    stageInitializer.openBigImageView(imagePipeline.getFullImage(image.getDatabaseId())));
        }

        public void setName(String name) {
            nameLabel.setText(name);
        }
    }
}

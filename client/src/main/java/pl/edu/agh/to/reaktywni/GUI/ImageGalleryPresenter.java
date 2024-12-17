package pl.edu.agh.to.reaktywni.GUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import pl.edu.agh.to.reaktywni.util.FilesToImagesConverter;
import pl.edu.agh.to.reaktywni.model.Image;
import pl.edu.agh.to.reaktywni.model.ImagePipeline;
import pl.edu.agh.to.reaktywni.model.ImageState;


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
        try {
            imagePipeline.setPresenter(this);
            Long count = imagePipeline.getImagesCount().block();
            if (count == null) throw new RuntimeException("Nie udało się pobrać liczby obrazów");
            if (count == 0) return;
            addStartPlaceholdersToGrid(count);
            AtomicInteger startImagesCounter = new AtomicInteger(0);
            imagePipeline.getThumbnails()
                    //.delayElements(Duration.ofMillis(300)) // simulate connection latency
                    .subscribe(image -> replacePlaceholderWithImage(image, startImagesCounter.getAndIncrement()),
                            e -> System.err.println("Error: " + e.getMessage()),
                            () -> System.out.println("Wczytano wszystkie obrazy"));
        } catch (Exception e) {
            System.err.println("Initialization failed: " + e.getMessage());
            Platform.runLater(Platform::exit);
        }
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
        List<Image> imagesToSend;
        try {
            imagesToSend = FilesToImagesConverter.convertWithPositionsCounting(files, gridIndex);
            addNamedPlaceholdersToGrid(imagesToSend);
            new Thread(() -> imagePipeline.sendAndReceiveImages(imagesToSend)).start();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Image processing error");
            alert.setHeaderText("Failed to process images from files");
            alert.setContentText("Check if the selected files are images");
            alert.showAndWait();
        }
    }

    private void createRowsIfRequired(long count) {
        final int current_gridIndex = gridIndex;
        Platform.runLater(() -> {
            int requiredRows = (int) ((current_gridIndex + count) / gridPane.getColumnCount());
            for (int i = gridPane.getRowCount(); i < requiredRows; i++) {
                gridPane.addRow(i);
                gridPane.getRowConstraints().add(gridPane.getRowConstraints().getFirst());
            }
        });
    }

    private void addStartPlaceholdersToGrid(long count) {
        createRowsIfRequired(count);
        for (int i = 0; i < count; i++) {
            ImageVBox imageVBox = new ImageVBox("............");
            final int index = gridIndex++;
            imageVBoxFromGridPlacementId.put(index, imageVBox);
            Platform.runLater(() -> gridPane.add(imageVBox, index % gridPane.getColumnCount(), index / gridPane.getColumnCount()));
        }
    }

    private void addNamedPlaceholdersToGrid(List<Image> images) {
        createRowsIfRequired(images.size());
        for (Image image : images) {
            ImageVBox imageVBox = new ImageVBox(image.getName());
            final int index = gridIndex++;
            imageVBoxFromGridPlacementId.put(image.getGridPlacementId(), imageVBox);
            Platform.runLater(() -> gridPane.add(imageVBox, index % gridPane.getColumnCount(), index / gridPane.getColumnCount()));
        }
    }

    public void replacePlaceholderWithImage(Image image, int gridPlacementId) {
        Platform.runLater(() -> {
            ImageVBox imageVBox = imageVBoxFromGridPlacementId.remove(gridPlacementId);
            if (image.getImageState() == ImageState.FAILURE) {
                imageVBox.imageView.setImage(error);
                imageVBox.setName("Błąd: " + image.getName());
                return;
            }
            imageVBox.setImage(image);
            if (!imageVBox.isNameFilled()) {
                imageVBox.setName(image.getName());
            }
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

        public boolean isNameFilled() {
            return !nameLabel.getText().startsWith("...");
        }
    }
}

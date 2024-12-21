package pl.edu.agh.to.reaktywni.GUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import lombok.Getter;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.to.reaktywni.util.FilesToImagesConverter;
import pl.edu.agh.to.reaktywni.model.Image;
import pl.edu.agh.to.reaktywni.model.ImagePipeline;
import pl.edu.agh.to.reaktywni.model.ImageState;


@Component
public class ImageGalleryPresenter {

    private static final int IMAGE_NAME_HEIGHT = 40;

    private static final Logger logger = Logger.getLogger(ImageGalleryPresenter.class.getName());

    @FXML private Label filesSelectedLabel;
    @FXML private Slider sizeSlider;
    @FXML private GridPane gridPane;

    private List<File> files;
    private int gridIndex = 0;
    private ThumbnailSize thumbnailsSize;
    private final Map<Integer, ImageVBox> imageVBoxFromGridId = new HashMap<>();
    private final Map<Integer, ImageVBox> imageVBoxFromDBId = new HashMap<>();

    private final ImagePipeline imagePipeline;
    private final StageInitializer stageInitializer;

    public ImageGalleryPresenter(ImagePipeline imagePipeline, StageInitializer stageInitializer) {
        this.imagePipeline = imagePipeline;
        this.stageInitializer = stageInitializer;
    }

    public void initialize() {
        thumbnailsSize = ThumbnailSize.getFromId((int) sizeSlider.getValue());
        initializeSizeSlider();
        imagePipeline.getThumbnailsCount()
                .subscribe(
                        count -> {
                            if (count == null) throw new IllegalStateException("Cannot get thumbnails count");
                            if (count == 0) return;
                            addStartPlaceholdersToGrid(count);
                            AtomicInteger startImagesCounter = new AtomicInteger(0);
                            imagePipeline.getThumbnails()
                                    .subscribe(image -> Platform.runLater(() -> replacePlaceholderWithImage(image, startImagesCounter.getAndIncrement())),
                                            e -> logger.log(Level.SEVERE,"Error: " + e.getMessage()),
                                            () -> logger.info("Loaded all images"));
                        },
                        error -> {
                            logger.log(Level.SEVERE,"Error: " + error.getMessage());
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Initialization error");
                                alert.setHeaderText("Cannot receive data from the server");
                                alert.setContentText("Check if the server is running and try again");
                                alert.showAndWait();
                                Platform.exit();
                            });
                        });
    }

    private void initializeSizeSlider() {
        sizeSlider.setLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Double object) {
                return switch (object.intValue()) { case 0 -> "Small"; case 1 -> "Medium"; case 2 -> "Large"; default -> "None"; }; }

            @Override
            public Double fromString(String string) {
                return switch (string) { case "Small" -> 0.; case "Medium" -> 1.; case "Large" -> 2.; default -> 3.; };
            }
        });
        sizeSlider.setOnMouseReleased(event -> updateThumbnailSizeValue());
        sizeSlider.setOnKeyPressed(event -> updateThumbnailSizeValue());
    }

    private void updateThumbnailSizeValue() {
        ThumbnailSize size = ThumbnailSize.getFromId((int) sizeSlider.getValue());
        if (!thumbnailsSize.equals(size)) {
            thumbnailsSize = size;
            refreshThumbnailsOnGrid();
        }
    }

    private void refreshThumbnailsOnGrid() {
        gridPane.getChildren().clear();
        modifyGridPane();
        updateImageVBoxesSize();
        placeVBoxesToGrid();
        downloadThumbnails();
    }

    private void modifyGridPane() {
        int columns = thumbnailsSize.columnCount;
        int current_columns = gridPane.getColumnCount();
        if (current_columns < columns) {
            for (int i = current_columns; i < columns; i++) gridPane.getColumnConstraints().add(new ColumnConstraints());
        }
        else if (current_columns > columns) {
            for (int i = current_columns; i > columns; i--) gridPane.getColumnConstraints().removeLast();
        }
        double percentWidth = 100.0 / columns;
        for (ColumnConstraints cc : gridPane.getColumnConstraints()) cc.setPercentWidth(percentWidth);
        for (RowConstraints rc : gridPane.getRowConstraints()) { rc.setMinHeight(thumbnailsSize.imageHeight + IMAGE_NAME_HEIGHT); rc.setMaxHeight(thumbnailsSize.imageHeight + IMAGE_NAME_HEIGHT); }
    }

    private void updateImageVBoxesSize() {
        for (ImageVBox imageVBox : imageVBoxFromGridId.values()) {
            imageVBox.changeVBoxSize(thumbnailsSize);
        }
    }

    private void placeVBoxesToGrid() {
        for (int index : imageVBoxFromGridId.keySet()) {
            gridPane.add(imageVBoxFromGridId.get(index), index % gridPane.getColumnCount(), index / gridPane.getColumnCount());
        }
    }

    private void downloadThumbnails() {
        // TODO: Implement downloading thumbnails with correct size

        imagePipeline.getThumbnails()   // imagePipeline.getThumbnails(thumbnailsSize)
                .subscribe(image -> {
                    ImageVBox imageVBox = imageVBoxFromDBId.get(image.getDatabaseId());
                    if (imageVBox != null) imageVBox.placeImage(thumbnailsSize, image);
                });
    }

    @FXML
    private void openSelectionWindow() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select images");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );
        files = fileChooser.showOpenMultipleDialog(null);
        if (files != null) {
            filesSelectedLabel.setText("Selected " + files.size() + " files");
            filesSelectedLabel.setVisible(true);
        }
    }

    @FXML
    private void sendAndReceiveImages() {
        if (files == null) return;
        try {
            List<Image> imagesToSend = FilesToImagesConverter.convertWithPositionsCounting(files, gridIndex);
            addNamedPlaceholdersToGrid(imagesToSend);
            new Thread(() -> imagePipeline.sendAndReceiveImages(imagesToSend)
                    .doOnNext(image -> Platform.runLater(() -> replacePlaceholderWithImage(image, image.getGridPlacementId())))
                    .blockLast()).start();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Image processing error");
            alert.setHeaderText("Failed to process images from files");
            alert.setContentText("Check if the selected files are images");
            alert.showAndWait();
        }
    }

    private void addStartPlaceholdersToGrid(long count) {
        for (int i = 0; i < count; i++) {
            ImageVBox imageVBox = new ImageVBox(thumbnailsSize, "............");
            final int index = gridIndex++;
            imageVBoxFromGridId.put(index, imageVBox);
            Platform.runLater(() -> gridPane.add(imageVBox, index % gridPane.getColumnCount(), index / gridPane.getColumnCount()));
        }
    }

    private void addNamedPlaceholdersToGrid(List<Image> images) {
        for (Image image : images) {
            ImageVBox imageVBox = new ImageVBox(thumbnailsSize, image.getName());
            final int index = gridIndex++;
            imageVBoxFromGridId.put(image.getGridPlacementId(), imageVBox);
            Platform.runLater(() -> gridPane.add(imageVBox, index % gridPane.getColumnCount(), index / gridPane.getColumnCount()));
        }
    }

    private void replacePlaceholderWithImage(Image image, int gridPlacementId) {
        ImageVBox imageVBox = imageVBoxFromGridId.get(gridPlacementId);
        imageVBoxFromDBId.put(image.getDatabaseId(), imageVBox);
        imageVBox.placeImage(thumbnailsSize, image);
    }


    private enum ThumbnailSize {

        SMALL(5, 156, 88),
        MEDIUM(4, 200, 120),
        LARGE(3, 273, 157);

        private final int columnCount;
        private final int imageWidth;
        private final int imageHeight;
        private final javafx.scene.image.Image placeholder;
        private final javafx.scene.image.Image errorImage;

        ThumbnailSize(int columnCount, int imageWidth, int imageHeight) {
            this.columnCount = columnCount;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            this.placeholder = new javafx.scene.image.Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/GUI/loading.gif")),
                    imageWidth, imageHeight, false, true
            );
            this.errorImage = new javafx.scene.image.Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/GUI/error.png")),
                    imageWidth, imageHeight, false, true
            );
        }

        private static final ThumbnailSize[] values = ThumbnailSize.values();

        public static ThumbnailSize getFromId(int id) {
            return values[id];
        }
    }


    private class ImageVBox extends VBox {

        private final ImageView imageView = new ImageView();

        private final Label nameLabel = new Label();

        @Getter
        private int imageDBId = -1;

        public ImageVBox(ThumbnailSize size, String name) {
            imageView.setImage(size.placeholder);
            nameLabel.setText(name);
            nameLabel.setWrapText(true);
            setMinHeight(size.imageHeight + IMAGE_NAME_HEIGHT);
            setMaxHeight(size.imageHeight + IMAGE_NAME_HEIGHT);
            setAlignment(Pos.TOP_CENTER);
            getChildren().addAll(imageView, nameLabel);
        }

        public void placeImage(ThumbnailSize size, Image image) {
            imageDBId = image.getDatabaseId();
            if (image.getImageState() != ImageState.FAILURE) {
                if (!isNameFilled()) nameLabel.setText(image.getName());
                imageView.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(image.getData()), size.imageWidth, size.imageHeight, false, false));
            } else {
                nameLabel.setText("Error: " + image.getName());
                imageView.setImage(size.errorImage);
            }
            setOnMouseClicked(event ->
                    stageInitializer.openBigImageView(imagePipeline.getFullImage(image.getDatabaseId())));
        }

        public boolean isNameFilled() {
            return !nameLabel.getText().startsWith("...");
        }

        public void changeVBoxSize(ThumbnailSize size) {
            setMinHeight(size.imageHeight + IMAGE_NAME_HEIGHT);
            setMaxHeight(size.imageHeight + IMAGE_NAME_HEIGHT);
            imageView.setImage(size.placeholder);
        }
    }
}

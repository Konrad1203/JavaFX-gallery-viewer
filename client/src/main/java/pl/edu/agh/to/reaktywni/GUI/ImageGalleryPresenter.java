package pl.edu.agh.to.reaktywni.GUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.edu.agh.to.reaktywni.GUI.util.ImageVBox;
import pl.edu.agh.to.reaktywni.GUI.util.ThumbnailSize;
import pl.edu.agh.to.reaktywni.util.FilesToImagesConverter;
import pl.edu.agh.to.reaktywni.model.Image;
import pl.edu.agh.to.reaktywni.model.ImagePipeline;


@Component
public class ImageGalleryPresenter {

    private static final int IMAGE_NAME_HEIGHT = 40;
    private static final int THUMBNAILS_FETCHING_DELAY = 10_000;

    private static final Logger logger = Logger.getLogger(ImageGalleryPresenter.class.getName());

    @FXML private Label filesSelectedLabel;
    @FXML private Slider sizeSlider;
    @FXML private GridPane gridPane;

    private List<File> files;
    private final AtomicInteger gridIndex = new AtomicInteger(0);
    private ThumbnailSize thumbnailsSize;
    private final List<ImageVBox> imageVBoxFromGridId = new ArrayList<>();
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
        imagePipeline.getThumbnailsCount(thumbnailsSize.toString())
                .subscribe(this::initializeThumbnailsOnStart, this::showInitializationError);
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

    private void initializeThumbnailsOnStart(Long count) {
        if (count == null) throw new IllegalStateException("Cannot get thumbnails count");
        if (count == 0) return;
        addStartPlaceholdersToGrid(count);
        AtomicInteger startImagesCounter = new AtomicInteger(0);
        imagePipeline.getThumbnails(thumbnailsSize.toString())
                .subscribe(image -> replacePlaceholderWithImage(image, startImagesCounter.getAndIncrement()),
                        e -> logger.log(Level.SEVERE,"Error: " + e.getMessage()),
                        () -> logger.info("Loaded all images"));
    }

    private void showInitializationError(Throwable error) {
        logger.log(Level.SEVERE,"Error: " + error.getMessage());
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Initialization error");
            alert.setHeaderText("Cannot receive data from the server");
            alert.setContentText("Check if the server is running and try again");
            alert.showAndWait();
            Platform.exit();
        });
    }

    @Scheduled(initialDelay = THUMBNAILS_FETCHING_DELAY, fixedDelay = THUMBNAILS_FETCHING_DELAY)
    private void fetchNewThumbnails() {
        imagePipeline.getThumbnailsCount(thumbnailsSize.toString())
                .subscribe(this::addNewThumbnails, error -> logger.log(Level.SEVERE,"Error: " + error.getMessage()));
    }

    private void addNewThumbnails(Long count) {
        if (count == null) throw new IllegalStateException("Cannot get thumbnails count");
        if (count == gridIndex.get()) return;
        logger.info("Fetching new thumbnails");
        long newImagesCount = count - gridIndex.get();
        AtomicInteger newImagesCounter = new AtomicInteger(gridIndex.get());
        addStartPlaceholdersToGrid(newImagesCount);
        imagePipeline.getThumbnailsExcludingSet(thumbnailsSize.toString(), imageVBoxFromDBId.keySet())
                .subscribe(image -> replacePlaceholderWithImage(image, newImagesCounter.getAndIncrement()),
                        e -> logger.log(Level.SEVERE,"Error: " + e.getMessage()),
                        () -> logger.info("Loaded all images"));
    }

    public void refreshClientOnServerRestart(){
        gridPane.getChildren().clear();
        modifyGridPane();
        imageVBoxFromGridId.clear();
        imageVBoxFromDBId.clear();
        gridIndex.set(0);

        imagePipeline.getThumbnailsCount(thumbnailsSize.toString())
                .subscribe(
                        this::initializeThumbnailsOnStart,
                        error -> Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Thumbnail download error");
                            alert.setHeaderText("Cannot receive data from the server");
                            alert.setContentText("Check if the server is running and try again");
                            alert.show();
                        })
//                        this::initializeThumbnailsOnStart, this::showInitializationError
        );
    }


    private void updateThumbnailSizeValue() {
        ThumbnailSize size = ThumbnailSize.getFromId((int) sizeSlider.getValue());
        if (!thumbnailsSize.equals(size)) {
            thumbnailsSize = size;
            refreshThumbnailsOnGrid();
        }

//        refreshClientOnServerRestart();
    }

    private void refreshThumbnailsOnGrid() {
        gridPane.getChildren().clear();
        modifyGridPane();
        updateImageVBoxesSize();
        placeVBoxesToGrid();
        downloadAndPlaceThumbnails();
    }

    private void modifyGridPane() {
        int columns = thumbnailsSize.getColumnCount();
        int current_columns = gridPane.getColumnCount();
        if (current_columns < columns) {
            for (int i = current_columns; i < columns; i++) gridPane.getColumnConstraints().add(new ColumnConstraints());
        }
        else if (current_columns > columns) {
            for (int i = current_columns; i > columns; i--) gridPane.getColumnConstraints().removeLast();
        }
        double percentWidth = 100.0 / columns;
        for (ColumnConstraints cc : gridPane.getColumnConstraints()) cc.setPercentWidth(percentWidth);
        for (RowConstraints rc : gridPane.getRowConstraints()) { rc.setMinHeight(thumbnailsSize.getImageHeight() + IMAGE_NAME_HEIGHT); rc.setMaxHeight(thumbnailsSize.getImageHeight() + IMAGE_NAME_HEIGHT); }
    }

    private void updateImageVBoxesSize() {
        for (ImageVBox imageVBox : imageVBoxFromGridId) {
            imageVBox.changeVBoxSize(thumbnailsSize, IMAGE_NAME_HEIGHT);
        }
    }

    private void placeVBoxesToGrid() {
        for (int index = 0; index < imageVBoxFromGridId.size(); index++) {
            gridPane.add(imageVBoxFromGridId.get(index), index % gridPane.getColumnCount(), index / gridPane.getColumnCount());
        }
    }

    private void downloadAndPlaceThumbnails() {
        imagePipeline.getThumbnails(thumbnailsSize.toString())
                .subscribe(
                        image -> {
                            ImageVBox imageVBox = imageVBoxFromDBId.get(image.getId());
                            if (imageVBox != null)
                                imageVBox.placeImage(thumbnailsSize, image, stageInitializer, imagePipeline);

                        },
                        error -> Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Thumbnail download error");
                            alert.setHeaderText("Cannot receive data from the server");
                            alert.setContentText("Check if the server is running and try again");
                            alert.show();
                        }));
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
            List<Image> imagesToSend = FilesToImagesConverter.convertWithPositionsCounting(files, gridIndex.get());
            addNamedPlaceholdersToGrid(imagesToSend);
            new Thread(() -> imagePipeline.sendAndReceiveImages(imagesToSend, thumbnailsSize.toString())
                    .doOnNext(image -> replacePlaceholderWithImage(image, image.getGridId()))
                    .doOnError(this::handleServerError)
                    .blockLast()
            ).start();
        } catch (IOException e) {
            handleServerError(e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Image processing error");
            alert.setHeaderText("Failed to process images from files");
            alert.setContentText("Check if the selected files are images");
            alert.showAndWait();
        }
    }

    private void handleServerError(Throwable e) {
        logger.log(Level.SEVERE, "Error: " + e.getMessage());
        Platform.runLater(() -> {
            for (ImageVBox imageVBox : imageVBoxFromGridId) {
                if (imageVBox.getImageView().getImage().equals(thumbnailsSize.getPlaceholder())) {
                    imageVBox.getImageView().setImage(thumbnailsSize.getErrorImage());
                }
            }
        });
    }

    private void addStartPlaceholdersToGrid(long count) {
        for (int i = 0; i < count; i++) {
            ImageVBox imageVBox = new ImageVBox(thumbnailsSize, "............", IMAGE_NAME_HEIGHT);
            final int index = gridIndex.getAndIncrement();
            imageVBoxFromGridId.add(imageVBox);
            Platform.runLater(() -> gridPane.add(imageVBox, index % gridPane.getColumnCount(), index / gridPane.getColumnCount()));
        }
    }

    private void addNamedPlaceholdersToGrid(List<Image> images) {
        for (Image image : images) {
            ImageVBox imageVBox = new ImageVBox(thumbnailsSize, image.getName(), IMAGE_NAME_HEIGHT);
            final int index = gridIndex.getAndIncrement();
            imageVBoxFromGridId.add(imageVBox);
            Platform.runLater(() -> gridPane.add(imageVBox, index % gridPane.getColumnCount(), index / gridPane.getColumnCount()));
        }
    }

    private void replacePlaceholderWithImage(Image image, int gridId) {
        ImageVBox imageVBox = imageVBoxFromGridId.get(gridId);
        imageVBoxFromDBId.put(image.getId(), imageVBox);
        Platform.runLater(() -> imageVBox.placeImage(thumbnailsSize, image, stageInitializer, imagePipeline));
    }
}

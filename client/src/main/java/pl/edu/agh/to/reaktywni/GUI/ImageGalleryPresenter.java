package pl.edu.agh.to.reaktywni.GUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
import pl.edu.agh.to.reaktywni.model.ImageState;
import pl.edu.agh.to.reaktywni.util.FilesToImagesConverter;
import pl.edu.agh.to.reaktywni.model.Image;
import pl.edu.agh.to.reaktywni.model.ImagePipeline;
import pl.edu.agh.to.reaktywni.util.ZipDataExtractor;
import reactor.core.scheduler.Schedulers;


@Component
public class ImageGalleryPresenter {

    private static final int IMAGE_NAME_HEIGHT = 40;
    private static final int THUMBNAILS_FETCHING_DELAY = 10_000;

    private static final Logger logger = Logger.getLogger(ImageGalleryPresenter.class.getName());

    @FXML private Label filesSelectedLabel;
    @FXML private Slider sizeSlider;
    @FXML private TreeView<String> dirSelectionView;
    @FXML private GridPane gridPane;
    private Stage selectionStage;

    private List<File> selectedFiles;
    private ThumbnailSize thumbnailsSize;
    private final List<ImageVBox> imageVBoxes = new ArrayList<>();
    private final List<ImageVBox> emptyImageVBoxes = new ArrayList<>();
    private final List<Integer> imageIds = new ArrayList<>();
    private final AtomicInteger processingThreads = new AtomicInteger(0);

    private final ImagePipeline imagePipeline;
    private final StageInitializer stageInitializer;

    public ImageGalleryPresenter(ImagePipeline imagePipeline, StageInitializer stageInitializer) {
        this.imagePipeline = imagePipeline;
        this.stageInitializer = stageInitializer;
    }

    public void initialize() {
        thumbnailsSize = ThumbnailSize.getFromId((int) sizeSlider.getValue());
        initializeSizeSlider();
        initializeTreeView();
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
        sizeSlider.setOnMouseReleased(event -> updateThumbnailSize());
        sizeSlider.setOnKeyPressed(event -> updateThumbnailSize());
    }

    private void initializeTreeView() {
        dirSelectionView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 1) {
                TreeItem<String> selectedItem = dirSelectionView.getSelectionModel().getSelectedItem();
                List<String> path = new ArrayList<>();
                if (!"Root".equals(selectedItem.getValue())) path.add(selectedItem.getValue());
                while (selectedItem.getParent() != null && !"Root".equals(selectedItem.getParent().getValue())) {
                    selectedItem = selectedItem.getParent();
                    path.add(selectedItem.getValue());
                }
                System.out.println("Selected item: " + String.join("/", path.reversed()) + '/');
            }
        });
    }

    private void initializeThumbnailsOnStart(Long count) {
        if (count == 0) return;
        addStartPlaceholdersToGrid(count);
        imagePipeline.getThumbnails(thumbnailsSize.toString())
                .index()
                .subscribe(image -> replacePlaceholderWithImage(image.getT2(), image.getT1().intValue()),
                        e -> logger.log(Level.SEVERE,"initializeThumbnailsOnStartError: " + e.getMessage()),
                        () -> logger.info("Loaded all images"));
    }

    private void showInitializationError(Throwable error) {
        logger.log(Level.SEVERE,"showInitializationError: " + error.getMessage());
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
        if (processingThreads.get() != 0) return;
        imagePipeline.getThumbnailsCount(thumbnailsSize.toString())
                .subscribe(
                        count -> {
                            if (!emptyImageVBoxes.isEmpty() || count != imageVBoxes.size()) downloadAndUpdateThumbnails(count);
                        },
                        error -> logger.log(Level.SEVERE,"fetchNewThumbnailsError: " + error.getMessage()));
    }

    private void downloadAndUpdateThumbnails(Long count) {
        logger.info("Fetching new thumbnails");
        if (count < imageVBoxes.size()) {
            long toBeRemovedCount = imageVBoxes.size() - count;
            for (int i = 0; i < toBeRemovedCount; i++) {
                ImageVBox vbox = imageVBoxes.removeLast();
                emptyImageVBoxes.remove(vbox);
                Platform.runLater(() -> gridPane.getChildren().remove(vbox));
            }
            if (emptyImageVBoxes.isEmpty()) return;
        } else if (count > imageVBoxes.size()) {
            addStartPlaceholdersToGrid(count - imageVBoxes.size());
        }
        imagePipeline.getThumbnailsExcludingList(thumbnailsSize.toString(), imageIds)
                .index()
                .subscribe(
                        image -> {
                            ImageVBox imageVBox = emptyImageVBoxes.get(image.getT1().intValue());
                            replacePlaceholderWithImage(image.getT2(), imageVBox.getGridId());
                        },
                        e -> logger.log(Level.SEVERE,"downloadAndUpdateThumbnailsError: " + e.getMessage()),
                        () -> logger.info("Loaded all images"));
    }

    private void updateThumbnailSize() {
        ThumbnailSize size = ThumbnailSize.getFromId((int) sizeSlider.getValue());
        if (!thumbnailsSize.equals(size)) {
            thumbnailsSize = size;
            refreshThumbnailsOnGrid();
        }
    }

    private void refreshThumbnailsOnGrid() {
        gridPane.getChildren().clear();
        modifyGridPaneRowAndColCount();
        updateImageVBoxesSize();
        placeVBoxesToGrid();
        downloadAndPlaceThumbnails();
    }

    private void modifyGridPaneRowAndColCount() {
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
        for (ImageVBox imageVBox : imageVBoxes) {
            imageVBox.changeVBoxSize(thumbnailsSize, IMAGE_NAME_HEIGHT);
        }
    }

    private void placeVBoxesToGrid() {
        for (int index = 0; index < imageVBoxes.size(); index++) {
            gridPane.add(imageVBoxes.get(index), index % gridPane.getColumnCount(), index / gridPane.getColumnCount());
        }
    }

    private void downloadAndPlaceThumbnails() {
        imagePipeline.getThumbnails(thumbnailsSize.toString())
                .index()
                .subscribe(
                        image -> {
                            if (image.getT1() > imageVBoxes.size()) logger.log(Level.SEVERE, "Error: Received image index is out of bounds");
                            else if (image.getT1() == imageVBoxes.size()) addPlaceholderToGrid("............");
                            replacePlaceholderWithImage(image.getT2(), image.getT1().intValue());
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
        if (selectionStage == null) {
            selectionStage = stageInitializer.initializeFilesSelectionStage(this::openImagesSelectionWindow, this::openZipSelectionWindow);
        }
        selectionStage.show();
    }

    private void openImagesSelectionWindow() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select images");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );
        selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null) {
            filesSelectedLabel.setText("Selected " + selectedFiles.size() + " files");
            filesSelectedLabel.setVisible(true);
        }
        selectionStage.hide();
    }

    private void openZipSelectionWindow() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select zip file");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Zip Files", "*.zip")
        );
        File file = fileChooser.showOpenDialog(null);
        selectedFiles = file != null ? List.of(file) : null;
        if (file != null) {
            filesSelectedLabel.setText("Selected zip file");
            filesSelectedLabel.setVisible(true);
            try {
                ZipDataExtractor.ZipData zipData = ZipDataExtractor.extractZipData(file);
                addDirectoryToTreeView(zipData.directory());
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Zip file processing error");
                alert.setHeaderText("Failed to process zip file");
                alert.setContentText("Check if the selected file is a zip archive");
                alert.showAndWait();
            }
        }
        selectionStage.hide();
    }

    private void addDirectoryToTreeView(ZipDataExtractor.Directory directory) {
        for (ZipDataExtractor.Directory subDir : directory.subdirectories())
            addDirectoryToTreeView(dirSelectionView.getRoot(), subDir);
    }

    private void addDirectoryToTreeView(TreeItem<String> root, ZipDataExtractor.Directory directory) {
        TreeItem<String> foundItem = null;
        for (TreeItem<String> treeItem : root.getChildren()) {
            if (treeItem.getValue().equals(directory.name())) { foundItem = treeItem; break; }
        }
        if (foundItem == null) {
            TreeItem<String> item = new TreeItem<>(directory.name());
            item.setExpanded(true);
            root.getChildren().add(item);
        } else {
            for (ZipDataExtractor.Directory subDir : directory.subdirectories()) {
                addDirectoryToTreeView(foundItem, subDir);
            }
        }
    }

    @FXML
    private void sendAndReceiveImages() {
        if (selectedFiles == null) return;
        processingThreads.incrementAndGet();
        int startCount = imageVBoxes.size();
        try {
            List<Image> imagesToSend = FilesToImagesConverter.convert(selectedFiles);
            for (int i = 0; i < imagesToSend.size(); i++) imagesToSend.get(i).setGridId(startCount + i);
            addNamedPlaceholdersToGrid(imagesToSend.stream().map(Image::getName).toList());
            imagePipeline.sendAndReceiveImages(imagesToSend, thumbnailsSize.toString())
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                            image -> replacePlaceholderWithImage(image, image.getGridId()),
                            this::handleServerError,
                            processingThreads::decrementAndGet
                    );
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Image processing error");
            alert.setHeaderText("Failed to process images from files");
            alert.setContentText("Check if the selected files are images");
            alert.showAndWait();
        }
        /*finally {
            selectedFiles = null;
            filesSelectedLabel.setVisible(false);
        }*/
    }

    @FXML
    private void refreshGridOnButton() {
        gridPane.getChildren().clear();
        imageVBoxes.clear();
        emptyImageVBoxes.clear();
        imageIds.clear();

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
                );
    }

    private void handleServerError(Throwable e) {
        processingThreads.set(0);
        logger.log(Level.SEVERE, "sendAndReceiveImagesError: " + e.getMessage());
        Platform.runLater(() -> {
            for (ImageVBox imageVBox : imageVBoxes) {
                if (imageVBox.getImageView().getImage().equals(thumbnailsSize.getPlaceholder())) {
                    imageVBox.getImageView().setImage(thumbnailsSize.getErrorImage());
                }
            }
        });
    }

    private void addStartPlaceholdersToGrid(long count) {
        for (int i = 0; i < count; i++) addPlaceholderToGrid("............");
    }

    private void addNamedPlaceholdersToGrid(List<String> imageNames) {
        imageNames.forEach(this::addPlaceholderToGrid);
    }

    private void addPlaceholderToGrid(String imageName) {
        ImageVBox imageVBox = new ImageVBox(imageVBoxes.size(), thumbnailsSize, imageName, IMAGE_NAME_HEIGHT);
        imageVBoxes.add(imageVBox);
        emptyImageVBoxes.add(imageVBox);
        Platform.runLater(() -> gridPane.add(imageVBox, imageVBox.getGridId() % gridPane.getColumnCount(), imageVBox.getGridId() / gridPane.getColumnCount()));
    }

    private void replacePlaceholderWithImage(Image image, int gridId) {
        ImageVBox imageVBox = imageVBoxes.get(gridId);
        if (image.getImageState() == ImageState.SUCCESS) {
            logger.info("Placed image on position: " + gridId + " | Image: " + image);
            emptyImageVBoxes.remove(imageVBox);
            imageIds.add(image.getId());
        }
        Platform.runLater(() -> imageVBox.placeImage(thumbnailsSize, image, stageInitializer, imagePipeline));
    }
}

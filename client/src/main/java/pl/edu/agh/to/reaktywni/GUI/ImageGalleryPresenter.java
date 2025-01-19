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
    @FXML private ScrollPane scrollPane;
    @FXML private GridPane gridPane;
    private Stage selectionStage;

    private List<Image> selectedImages;
    private ThumbnailSize thumbnailsSize;
    private final List<ImageVBox> imageVBoxes = new ArrayList<>();
    private final List<ImageVBox> emptyImageVBoxes = new LinkedList<>();
    private final List<Integer> imageIds = new ArrayList<>();
    private final AtomicInteger processingThreads = new AtomicInteger(0);
    private int pagesDownloaded = 0;
    private boolean scrolledToEnd = false;

    private final ImagePipeline imagePipeline;
    private final StageInitializer stageInitializer;

    public ImageGalleryPresenter(ImagePipeline imagePipeline, StageInitializer stageInitializer) {
        this.imagePipeline = imagePipeline;
        this.stageInitializer = stageInitializer;
    }

    // ==================================== STARTUP INITIALIZATION ====================================
    public void initialize() {
        thumbnailsSize = ThumbnailSize.getFromId((int) sizeSlider.getValue());
        initializeSizeSlider();
        initializeTreeView();
        initializeOnScrollAction();
        imagePipeline.getThumbnailsCount(thumbnailsSize.toString(), getSelectedDirectoryPath())
                .subscribe(this::fetchThumbnailsOnStart, this::showInitializationError);
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
        sizeSlider.setOnMouseReleased(event -> updateThumbnailSizeSliderHandling());
        sizeSlider.setOnKeyPressed(event -> updateThumbnailSizeSliderHandling());
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

    private void initializeOnScrollAction() {
        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (!scrolledToEnd && newValue.doubleValue() == 1.0) fetchNextPageOfThumbnails();
        });
    }
    // ================================================================================================

    // =========================== FETCHING NEXT THUMBNAILS ON SCROLL =================================
    private void fetchNextPageOfThumbnails() {
        List<Image> imageList = imagePipeline.getThumbnails(thumbnailsSize.name(), getSelectedDirectoryPath(), pagesDownloaded, thumbnailsSize.getPageSize())
                .collectList()
                .block();
        if (imageList == null) { logger.warning("Failed to load new thumbnails page!"); return; }
        pagesDownloaded++;
        if (imageList.size() != thumbnailsSize.getPageSize()) scrolledToEnd = true;
        imageList.forEach(image -> replacePlaceholderWithImage(image, addPlaceholderToGrid("Placing...")));
    }

    private void scrollToEnd() {
        while (!scrolledToEnd) fetchNextPageOfThumbnails();
        scrollPane.setVvalue(scrollPane.getVmax());
    }
    // ================================================================================================

    // ============================ FETCHING THUMBNAILS ON STARTUP ====================================
    private void fetchThumbnailsOnStart(Long count) {
        if (count == 0) return;
        addStartPlaceholdersToGrid(Math.min(count, thumbnailsSize.getPageSize()));
        pagesDownloaded = 1;
        scrolledToEnd = false;
        imagePipeline.getThumbnails(thumbnailsSize.toString(), getSelectedDirectoryPath(),0, thumbnailsSize.getPageSize())
                .index()
                .subscribe(image -> replacePlaceholderWithImage(image.getT2(), image.getT1().intValue()),
                        e -> logger.log(Level.SEVERE,"initializeThumbnailsOnStartError: " + e.getMessage()),
                        () -> logger.info("Loaded all images")
                );
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
    // ================================================================================================

    // =========================== SCHEDULED FETCHING OF NEW THUMBNAILS ===============================
    @Scheduled(initialDelay = THUMBNAILS_FETCHING_DELAY, fixedDelay = THUMBNAILS_FETCHING_DELAY)
    private void handleScheduledOperations() {
        if (processingThreads.get() == 0) {
            if (!emptyImageVBoxes.isEmpty()) removeUnnecessaryVBoxes();
            if (!emptyImageVBoxes.isEmpty()) fetchMissingThumbnails();
        }
    }

    private void removeUnnecessaryVBoxes() {
        imagePipeline.getThumbnailsCount(thumbnailsSize.toString(), getSelectedDirectoryPath())
                .subscribe(
                        count -> {
                            if (count < imageVBoxes.size()) {
                                long toBeRemovedCount = imageVBoxes.size() - count;
                                for (int i = 0; i < toBeRemovedCount; i++) {
                                    ImageVBox vbox = imageVBoxes.removeLast();
                                    emptyImageVBoxes.remove(vbox);
                                    Platform.runLater(() -> gridPane.getChildren().remove(vbox));
                                }
                            } else if (count > imageVBoxes.size()) {
                                logger.info("There are new images in the database");
                            }
                        },
                        error -> logger.log(Level.SEVERE,"removeUnnecessaryVBoxesError: " + error.getMessage())
                );
    }

    private void fetchMissingThumbnails() {
        logger.info("Fetching missing thumbnails");
        imagePipeline.getThumbnailsExcludingList(thumbnailsSize.toString(), getSelectedDirectoryPath(), imageIds, imageVBoxes.size())
                .subscribe(
                        image -> replacePlaceholderWithImage(image, emptyImageVBoxes.getFirst().getGridId()),
                        e -> logger.log(Level.SEVERE,"downloadAndUpdateThumbnailsError: " + e.getMessage()),
                        () -> logger.info("Loaded all images")
                );
    }
    // ================================================================================================

    // ================================== THUMBNAIL SIZE CHANGE =======================================
    private void updateThumbnailSizeSliderHandling() {
        ThumbnailSize size = ThumbnailSize.getFromId((int) sizeSlider.getValue());
        if (!thumbnailsSize.equals(size)) {
            if (processingThreads.get() != 0) sizeSlider.setValue(thumbnailsSize.getId());
            else { thumbnailsSize = size; refreshThumbnailsOnGrid(); }
        }
    }

    private void refreshThumbnailsOnGrid() {
        removeImageVBoxes();
        modifyGridPaneRowAndColCount();
        fetchThumbnailsAfterSizeChange();
    }

    private void removeImageVBoxes() {
        gridPane.getChildren().clear();
        imageVBoxes.clear();
        emptyImageVBoxes.clear();
        imageIds.clear();
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

    private void fetchThumbnailsAfterSizeChange() {
        pagesDownloaded = 1;
        scrolledToEnd = false;
        imagePipeline.getThumbnails(thumbnailsSize.toString(), getSelectedDirectoryPath(), 0, thumbnailsSize.getPageSize())
                .subscribe(
                        image -> replacePlaceholderWithImage(image, addPlaceholderToGrid("Placing...")),
                        error -> Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Thumbnail download error");
                            alert.setHeaderText("Cannot receive data from the server");
                            alert.setContentText("Check if the server is running and try again");
                            alert.show();
                        })
                );
    }
    // ================================================================================================

    // ============================== SENDING THUMBNAILS FROM CLIENT ==================================
    @FXML
    private void openSelectionWindow() {
        if (selectionStage == null) {
            selectionStage = stageInitializer.initializeFilesSelectionStage(
                    this::openImagesSelectionWindow, this::openZipSelectionWindow);
        }
        selectionStage.show();
    }

    private void openImagesSelectionWindow() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select images");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(null);
        if (files != null) {
            try {
                selectedImages = FilesToImagesConverter.convert(files);
                filesSelectedLabel.setText("Selected " + selectedImages.size() + " images");
                filesSelectedLabel.setVisible(true);
            } catch (IOException e) {
                selectedImages = null;
                logger.warning("Image processing error: " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Image processing error");
                alert.setHeaderText("Failed to process images from files");
                alert.setContentText("Check if the selected files are images");
                alert.showAndWait();
            }
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
        if (file != null) {
            try {
                ZipDataExtractor.ZipData zipData = ZipDataExtractor.extractZipData(file);
                addDirectoryToTreeView(zipData.directory());
                selectedImages = zipData.images();
                filesSelectedLabel.setText("Selected zip file with " + selectedImages.size() + " images");
                filesSelectedLabel.setVisible(true);
            } catch (IOException e) {
                selectedImages = null;
                logger.warning("Zip file processing error: " + e.getMessage());
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
            foundItem = item;
        }
        for (ZipDataExtractor.Directory subDir : directory.subdirectories()) {
            addDirectoryToTreeView(foundItem, subDir);
        }
    }

    @FXML
    private void sendAndReceiveImages() {
        if (selectedImages == null) return;
        processingThreads.incrementAndGet();
        if (!scrolledToEnd && processingThreads.get() == 1) scrollToEnd();
        int startCount = imageVBoxes.size();
        List<Image> imagesCopy = selectedImages.stream().map(Image::copyOf).toList();
        for (int i = 0; i < imagesCopy.size(); i++) imagesCopy.get(i).setGridId(startCount + i);
        addNamedPlaceholdersToGrid(
                imagesCopy.stream()
                .filter(image -> image.getDirectoryPath().equals(getSelectedDirectoryPath()))
                .map(Image::getName)
                .toList()
        );
        imagePipeline.sendAndReceiveImages(imagesCopy, thumbnailsSize.toString(), getSelectedDirectoryPath())
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        image -> replacePlaceholderWithImage(image, image.getGridId()),
                        this::handleServerError,
                        processingThreads::decrementAndGet
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
    // ================================================================================================

    // ============================== REFRESH BUTTON HANDLING =========================================
    @FXML
    private void refreshGridOnButton() {
        gridPane.getChildren().clear();
        imageVBoxes.clear();
        emptyImageVBoxes.clear();
        imageIds.clear();

        imagePipeline.getThumbnailsCount(thumbnailsSize.toString(), getSelectedDirectoryPath())
                .subscribe(
                        this::fetchThumbnailsOnStart,
                        error -> Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Thumbnail download error");
                            alert.setHeaderText("Cannot receive data from the server");
                            alert.setContentText("Check if the server is running and try again");
                            alert.show();
                        })
                );
    }
    // ================================================================================================

    // ====================================== HELP METHODS ============================================
    private void addStartPlaceholdersToGrid(long count) {
        for (int i = 0; i < count; i++) addPlaceholderToGrid("............");
    }

    private void addNamedPlaceholdersToGrid(List<String> imageNames) {
        imageNames.forEach(this::addPlaceholderToGrid);
    }

    private int addPlaceholderToGrid(String imageName) {
        ImageVBox imageVBox = new ImageVBox(imageVBoxes.size(), thumbnailsSize, imageName, IMAGE_NAME_HEIGHT);
        imageVBoxes.add(imageVBox);
        emptyImageVBoxes.add(imageVBox);
        Platform.runLater(() -> gridPane.add(imageVBox, imageVBox.getGridId() % gridPane.getColumnCount(), imageVBox.getGridId() / gridPane.getColumnCount()));
        return imageVBox.getGridId();
    }

    private void replacePlaceholderWithImage(Image image, int gridId) {
        ImageVBox imageVBox = imageVBoxes.get(gridId);
        emptyImageVBoxes.remove(imageVBox);
        if (image.getImageState() == ImageState.SUCCESS) {
            logger.info("Placed image on position: " + gridId + " | Image: " + image);
            emptyImageVBoxes.remove(imageVBox);
            imageIds.add(image.getId());
        } else {
            emptyImageVBoxes.addLast(imageVBox);
        }
        Platform.runLater(() -> imageVBox.placeImage(thumbnailsSize, image, stageInitializer, imagePipeline));
    }

    private String getSelectedDirectoryPath() {
        TreeItem<String> selectedItem = dirSelectionView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || "Root".equals(selectedItem.getValue())) {
            return "/";
        }
        List<String> path = new ArrayList<>();
        path.add(selectedItem.getValue());
        while (selectedItem.getParent() != null && !"Root".equals(selectedItem.getParent().getValue())) {
            selectedItem = selectedItem.getParent();
            path.add(selectedItem.getValue());
        }
        Collections.reverse(path);
        return "/" + String.join("/", path) + "/";
    }
    // ================================================================================================
}

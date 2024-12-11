package pl.edu.agh.to.reaktywni.GUI;

import javafx.application.Platform;
import javafx.event.ActionEvent;
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

    private final Map<Integer, Integer> imageIdToGridIndex = new HashMap<>();

    private final ImagePipeline imagePipeline;
    private final StageInitializer stageInitializer;


    public ImageGalleryPresenter(ImagePipeline imagePipeline, StageInitializer stageInitializer) {
        this.imagePipeline = imagePipeline;
        this.stageInitializer = stageInitializer;
    }

    public void initialize() {
        imagePipeline.setPresenter(this);
        /*
        Long count = imagePipeline.getImagesCount().block();
        if (count == null) throw new RuntimeException("Nie udało się pobrać liczby obrazów");
        addPlaceholdersToGrid(count);
        imagePipeline.getThumbnails()
                .doOnNext(this::placeImageToGrid)
                .doOnComplete(() -> System.out.println("Wczytano wszystkie obrazy"))
                .subscribe();*/
    }

    @FXML
    private void openSelectionWindow(ActionEvent actionEvent) {
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
    private void sendAndReceiveImages(ActionEvent actionEvent) {
        if (files == null) return;
        final int startGridPosition = gridIndex;
        List<Image> imagesToSend = FilesToImagesConverter.convertWithPositionsCounting(files, startGridPosition);
        gridIndex += files.size();
        addPlaceholdersToGrid(imagesToSend, startGridPosition);

        new Thread(() -> imagePipeline.sendAndReceiveImages(imagesToSend)).start();
    }

    public void addPlaceholdersToGrid(long count) {
        createRowsIfRequired(count, 0);
        javafx.scene.image.Image image = new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/GUI/loading.gif")), 100, 100, true, true);
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(image);
            final int index = i;
            Platform.runLater(() -> gridPane.add(imageView, index % gridPane.getColumnCount(), index / gridPane.getColumnCount()));
        }
    }

    public void addPlaceholdersToGrid(List<Image> images, int startGridPosition) {
        createRowsIfRequired(images.size(), startGridPosition);

        javafx.scene.image.Image placeholder = new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/GUI/loading.gif")), 100, 100, true, true);
        for (Image image : images) {
            ImageView imageView = new ImageView(placeholder);
            Label nameLabel = new Label(image.getName());
            nameLabel.setWrapText(true);
            VBox placeholderBox = new VBox(imageView, nameLabel);
            final int index = startGridPosition++;
            imageIdToGridIndex.put(image.getGridPlacementId(), index);
            Platform.runLater(() -> gridPane.add(placeholderBox, index % gridPane.getColumnCount(), index / gridPane.getColumnCount()));
        }
    }

    private void createRowsIfRequired(long count, int startGridPosition) {
        Platform.runLater(() -> {
            int requiredRows = (int) ((startGridPosition + count) / gridPane.getColumnCount()) + 1;
            for (int i = gridPane.getRowCount(); i < requiredRows; i++) {
                gridPane.addRow(i);
                gridPane.getRowConstraints().add(gridPane.getRowConstraints().getFirst());
            }
        });
    }

    private VBox getVBox(Image image) {
        ImageView imageView = new ImageView(new javafx.scene.image.Image(new ByteArrayInputStream(image.getData()), 200, 120, false, false));
        Label nameLabel = new Label(image.getName());
        nameLabel.setWrapText(true);
        VBox photoBox = new VBox(imageView, nameLabel);
        photoBox.setMaxHeight(160);
        photoBox.setAlignment(Pos.TOP_CENTER);
        photoBox.setOnMouseClicked(event ->
                stageInitializer.openBigImageView(imagePipeline.getFullImage(image.getDatabaseId())));
        return photoBox;
    }

    public void replacePlaceholderWithImage(Image image) {
        VBox photoBox = getVBox(image);

        Platform.runLater(() -> {
            final int index = imageIdToGridIndex.remove(image.getGridPlacementId());
            final int row = index / gridPane.getColumnCount();
            final int col = index % gridPane.getColumnCount();
            gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col);
            gridPane.add(photoBox, col, row);
        });
    }

    public void placeImageToGrid(Image image) {
        VBox photoBox = getVBox(image);
        Platform.runLater(() -> {
            final int index = gridIndex++;
            final int row = index / gridPane.getColumnCount();
            final int col = index % gridPane.getColumnCount();
            gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col);
            gridPane.add(photoBox, index % gridPane.getColumnCount(), index / gridPane.getColumnCount());
        });
    }
}

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
    private void sendImages(ActionEvent actionEvent) {
        if (files == null) return;
        new Thread(() -> imagePipeline.sendImagesFromFiles(files, 0)).start();
    }

    public void addPlaceholdersToGrid(long count) {
        createRowsIfRequired(count);
        javafx.scene.image.Image image = new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/GUI/loading.gif")), 100, 100, true, true);
        for (int i = gridIndex; i < gridIndex + count; i++) {
            ImageView imageView = new ImageView(image);
            final int index = i;
            Platform.runLater(() -> gridPane.add(imageView, index % gridPane.getColumnCount(), index / gridPane.getColumnCount()));
        }
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

    public void placeImageToGrid(Image image) {
        ImageView imageView = new ImageView(new javafx.scene.image.Image(new ByteArrayInputStream(image.getData()), 200, 120, false, false));
        Label nameLabel = new Label(image.getName());
        nameLabel.setWrapText(true);
        VBox photoBox = new VBox(imageView, nameLabel);
        photoBox.setMaxHeight(160);
        photoBox.setAlignment(Pos.TOP_CENTER);
        photoBox.setOnMouseClicked(event ->
                stageInitializer.openBigImageView(imagePipeline.getFullImage(image.getDatabaseId())));

        Platform.runLater(() -> {
            final int row = gridIndex / gridPane.getColumnCount();
            final int col = gridIndex % gridPane.getColumnCount();
            gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col);
            gridPane.add(photoBox, col, row);
            gridIndex++;
        });
    }
}

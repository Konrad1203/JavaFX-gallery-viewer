package pl.edu.agh.to.reaktywni.GUI;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class ImageGalleryPresenter {

    @FXML
    private Label filesSelectedLabel;

    @FXML
    private GridPane gridPane;

    private Stage primaryStage;

    private int row = 0;
    private int col = 0;

    private List<File> files;


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void openSelectionWindow(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz obrazy");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        files = fileChooser.showOpenMultipleDialog(primaryStage);
        if (files != null) {
            filesSelectedLabel.setText("Wybrano " + files.size() + " obrazów");
            filesSelectedLabel.setVisible(true);
        }
    }

    public void sendImages(ActionEvent actionEvent) {
        if (files != null) {

            // TYMCZASOWY WĄTEK DO WSTAWIANIA OBRAZÓW
            new Thread(() -> {
                addPlaceholdersToGrid(files.size());

                try { Thread.sleep(1000); }
                catch (InterruptedException e) { throw new RuntimeException(e); }

                addImagesToGrid(files);
                files = null;
            }).start();
        }
        filesSelectedLabel.setVisible(false);
    }

    private void addImagesToGrid(List<File> files) {
        for (File file : files) {
            System.out.println(file.getName());
            try {
                Image image = new Image(file.toURI().toString(), 200, 180, true, false);
                placeInGrid(image, file.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to load image: " + file.getName());
                alert.showAndWait();
            }

            try { Thread.sleep(500); }
            catch (InterruptedException e) { throw new RuntimeException(e); }
        }
    }

    private void addPlaceholdersToGrid(int count) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/GUI/loading.gif")), 100, 100, true, true);
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(image);
            final int index = i;
            Platform.runLater(() -> {
                if (gridPane.getRowCount() <= index / gridPane.getColumnCount()) {
                    gridPane.addRow(row);
                    gridPane.getRowConstraints().add(gridPane.getRowConstraints().getFirst());
                }
                gridPane.add(imageView, index % gridPane.getColumnCount(), index / gridPane.getColumnCount());
            });
        }
    }

    private void placeInGrid(Image image, String fileName) {
        ImageView imageView = new ImageView(image);
        Label nameLabel = new Label(fileName);
        nameLabel.setWrapText(true);
        VBox photoBox = new VBox(imageView, nameLabel);
        photoBox.setMaxHeight(160);
        photoBox.setAlignment(Pos.TOP_CENTER);

        Platform.runLater(() -> {
            if (gridPane.getRowCount() <= row) {
                gridPane.addRow(row);
                gridPane.getRowConstraints().add(gridPane.getRowConstraints().getFirst());
            }
            int current_col = col;
            int current_row = row;
            gridPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == current_row && GridPane.getColumnIndex(node) == current_col);
            gridPane.add(photoBox, current_col, current_row);
            col++;
            if (col >= gridPane.getColumnCount()) {
                col = 0;
                row++;
            }
        });
    }
}

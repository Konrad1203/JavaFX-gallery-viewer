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
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.ServerClient;
import pl.edu.agh.to.reaktywni.model.ImageDTO;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ImageGalleryPresenter {
    @FXML
    private Label filesSelectedLabel;

    @FXML
    private GridPane gridPane;
    private Stage primaryStage;
    private int row = 0;
    private int col = 0;
    private List<File> files;
    private final ServerClient serverClient;

    public ImageGalleryPresenter(ServerClient serverClient) {
        this.serverClient = serverClient;
    }

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
        //testOfInsertingImages();
        //sendingPipelineTest();
        sendImagesPipeline();
    }

    public void sendImagesPipeline(){
        if(files == null || files.isEmpty()) return;

        int positionCounter = 0;
        List<ImageDTO> images = new ArrayList<>();

        for (File file : files) {
            try {
                images.add(new ImageDTO(positionCounter++, file));
            } catch (IOException e) {
                System.out.println("Blad przy przetwarzaniu pliku: " + file.getAbsolutePath());
                e.printStackTrace();
            }
        }


        System.out.println("Wyslam obrazy: " + images.size());
        serverClient.sendImages(Flux.fromIterable(images))
                .doOnNext(processed -> System.out.println("Otrzymano: " + processed.getName()))
                .blockLast();

        //todo
        //files.clear();
    }

    public void sendingPipelineTest(){
        List<String> paths = List.of(
                "C:\\Users\\Mateusz\\Desktop\\hotdogi\\1001.png",
                "C:\\Users\\Mateusz\\Desktop\\hotdogi\\1002.png"
        );

        List<ImageDTO> images = new ArrayList<>();

        for(String path : paths){
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Plik nie istnieje: " + file.getAbsolutePath());
                return;
            } else {
                try {
                    images.add(new ImageDTO(1, file));
                } catch (IOException e) {
                    System.out.println("Blad przy przetwarzaniu pliku: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Wyslam obrazy: " + images.size());
        serverClient.sendImages(Flux.fromIterable(images))
				.doOnNext(processed -> System.out.println("Otrzymano: " + processed.getName()))
				.blockLast();
    }

    private void testOfInsertingImages() {
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
                Image image = new Image(file.toURI().toString(), 200, 120, true, false);
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

package pl.edu.agh.to.reaktywni.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.GUI.ImageGalleryPresenter;
import pl.edu.agh.to.reaktywni.ServerClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImagePipeline {

    private final ServerClient serverClient;
    private ImageGalleryPresenter presenter;

    public ImagePipeline(ServerClient serverClient) {
        this.serverClient = serverClient;
    }

    public void setPresenter(ImageGalleryPresenter presenter) {
        this.presenter = presenter;
    }

    public void sendImagesFromFiles(List<File> files, int positionCounter) {

        List<ImageDTO> images = convertFilesToImages(files, positionCounter);
        presenter.addPlaceholdersToGrid(images.size());

        System.out.println("Wysylam obrazy: " + images.size());
        serverClient.sendImages(Flux.fromIterable(images))
                .doOnNext(processed -> {
                    System.out.println("Otrzymano: " + processed.getName());
                    System.out.println("Width: " + processed.getWidth() + "\tHeight: " + processed.getHeight());
                })
                .doOnNext(presenter::placeImageToGrid)
                .blockLast();
    }

    private List<ImageDTO> convertFilesToImages(List<File> files, int positionCounter) {
        List<ImageDTO> images = new ArrayList<>();
        for (File file : files) {
            images.add(ImageDTO.createFromFile(file));
            positionCounter++;
        }
        return images;
    }

    public Flux<ImageDTO> getThumbnails() {
        return serverClient.getThumbnails();
    }

    public Mono<Long> getImagesCount() {
        return serverClient.getImagesCount();
    }

    public void sendTestImages(List<File> files) {
        List<String> paths = List.of(
                "C:\\Users\\Mateusz\\Desktop\\hotdogi\\1001.png",
                "C:\\Users\\Mateusz\\Desktop\\hotdogi\\1002.png"
        );

        List<ImageDTO> images = new ArrayList<>();

        for (String path : paths) {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Plik nie istnieje: " + file.getAbsolutePath());
                return;
            } else {
                images.add(ImageDTO.createFromFile(file));
            }
        }

        System.out.println("Wyslam obrazy: " + images.size());
        serverClient.sendImages(Flux.fromIterable(images))
                .doOnNext(processed -> System.out.println("Otrzymano: " + processed.getName()))
                .blockLast();
    }

    public ImageDTO getFullImage(int id) {
        return serverClient.getFullImage(id);
    }
}

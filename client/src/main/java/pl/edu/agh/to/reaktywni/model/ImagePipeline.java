package pl.edu.agh.to.reaktywni.model;

import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.GUI.ImageGalleryPresenter;
import pl.edu.agh.to.reaktywni.ServerClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

        List<Image> images = convertFilesToImages(files, positionCounter);
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

    private List<Image> convertFilesToImages(List<File> files, int positionCounter) {
        List<Image> images = new ArrayList<>();
        for (File file : files) {
            images.add(Image.createFromFile(positionCounter, file));
            positionCounter++;
        }
        return images;
    }

    public Flux<Image> getThumbnails() {
        return serverClient.getThumbnails();
    }

    public Mono<Long> getImagesCount() {
        return serverClient.getImagesCount();
    }

    public Mono<Image> getFullImage(int id) {
        return serverClient.getFullImage(id);
    }
}

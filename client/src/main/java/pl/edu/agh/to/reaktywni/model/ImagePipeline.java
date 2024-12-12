package pl.edu.agh.to.reaktywni.model;

import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.GUI.ImageGalleryPresenter;
import pl.edu.agh.to.reaktywni.ServerClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public void sendAndReceiveImages(List<Image> images) {
        System.out.println("Wysylam obrazy: " + images.size());
        serverClient.sendImages(Flux.fromIterable(images))
                //.doOnNext(image -> System.out.println("Send: " + image.getName() + " | GridID: " + image.getGridPlacementId() + " | DB_ID: " + image.getDatabaseId()))
                .doOnNext(image -> presenter.replacePlaceholderWithImage(image, image.getGridPlacementId()))
                .blockLast();
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

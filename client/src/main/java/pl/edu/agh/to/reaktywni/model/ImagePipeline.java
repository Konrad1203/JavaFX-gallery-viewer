package pl.edu.agh.to.reaktywni.model;

import lombok.Setter;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.GUI.ImageGalleryPresenter;
import pl.edu.agh.to.reaktywni.ServerClient;
import pl.edu.agh.to.reaktywni.util.Base64ImageDataCodec;
import pl.edu.agh.to.reaktywni.util.WrongImage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class ImagePipeline {
    private static final Logger logger = Logger.getLogger(ImagePipeline.class.getName());

    private final ServerClient serverClient;

    @Setter
    private ImageGalleryPresenter presenter;

    public ImagePipeline(ServerClient serverClient) {
        this.serverClient = serverClient;
    }

    public void sendAndReceiveImages(List<Image> images) {
        logger.info("Sending images: " + images.size());

        Flux<Image> receivedImages = serverClient.sendImages(Flux.fromIterable(images).doOnNext(Base64ImageDataCodec::encode));

        receivedImages
                .doOnNext(Base64ImageDataCodec::decode)
                .map(WrongImage::convertIfStateEqualsFailure)
                .doOnNext(image -> presenter.replacePlaceholderWithImage(image, image.getGridPlacementId()))
                .blockLast();
    }

    public Flux<Image> getThumbnails() {
        return serverClient.getThumbnails()
                .doOnNext(Base64ImageDataCodec::decode)
                .doOnError(e -> logger.log(Level.SEVERE, "getThumbnailsError: " + e.getMessage()));

    }

    public Mono<Long> getThumbnailsCount() {
        return serverClient.getThumbnailsCount();
    }

    public Mono<Image> getFullImage(int id) {
        return serverClient.getFullImage(id)
                .doOnNext(Base64ImageDataCodec::decode)
                .onErrorResume(e -> {
                    logger.log(Level.SEVERE, "getFullImageError: " + e.getMessage());
                    return Mono.just(WrongImage.getWrongImage());
                });
    }
}

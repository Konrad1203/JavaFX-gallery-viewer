package pl.edu.agh.to.reaktywni.model;

import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.ServerClient;
import pl.edu.agh.to.reaktywni.util.Base64ImageDataCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class ImagePipeline {
    private static final Logger logger = Logger.getLogger(ImagePipeline.class.getName());

    private final ServerClient serverClient;

    public ImagePipeline(ServerClient serverClient) {
        this.serverClient = serverClient;
    }

    public Flux<Image> sendAndReceiveImages(List<Image> images, String thumbnailSize) {
        logger.info("Sending images: " + images.size());

        Flux<Image> receivedImages = serverClient.sendImages(
                Flux.fromIterable(images).doOnNext(Base64ImageDataCodec::encode),
                thumbnailSize
        );

        return receivedImages.doOnNext(Base64ImageDataCodec::decode)
                .doOnNext(this::logImageInfo);
    }

    private void logImageInfo(Image image) {
        logger.info("Received image: " + image.getName() +
                " | Size: " + image.getWidth() + "x" + image.getHeight() + " | State: " + image.getImageState());
    }

    public Flux<Image> getThumbnails(String thumbnailSize) {
        return serverClient.getThumbnails(thumbnailSize)
                .doOnNext(Base64ImageDataCodec::decode)
                .doOnError(e -> logger.log(Level.SEVERE, "getThumbnailsError: " + e.getMessage()));

    }

    public Mono<Long> getThumbnailsCount(String thumbnailSize) {
        return serverClient.getThumbnailsCount(thumbnailSize);
    }

    public Mono<Image> getFullImage(int id) {
        return serverClient.getFullImage(id)
                .doOnNext(Base64ImageDataCodec::decode)
                .doOnError(e -> logger.log(Level.SEVERE, "getFullImageError: " + e.getMessage()));
    }
}

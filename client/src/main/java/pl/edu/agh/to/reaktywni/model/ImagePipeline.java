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
        logger.info("Received: " + image);
    }

    public Flux<Image> getThumbnails(String thumbnailSize, int page, int pageSize) {
        return serverClient.getThumbnails(thumbnailSize, page, pageSize)
                .doOnNext(Base64ImageDataCodec::decode);
    }

    public Flux<Image> getThumbnailsExcludingList(String thumbnailSize, List<Integer> ids, int elemCount) {
        return serverClient.getThumbnailsExcludingSet(thumbnailSize, ids, elemCount)
                .doOnNext(Base64ImageDataCodec::decode);
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

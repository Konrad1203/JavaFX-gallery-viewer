package pl.edu.agh.to.reaktywni.image;


import org.springframework.stereotype.Service;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailRepository;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailSize;
import pl.edu.agh.to.reaktywni.util.Base64ImageDataCodec;
import pl.edu.agh.to.reaktywni.util.Resizer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;


@Service
public class ImageService {

    private final static Logger logger = Logger.getLogger(ImageService.class.getName());

    private final ImageRepository imageRepository;
    private final ThumbnailRepository thumbnailRepository;
    private final Resizer imageResizer;

    public ImageService(ImageRepository imageRepository, ThumbnailRepository thumbnailRepository, Resizer imageResizer) {
        this.imageRepository = imageRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.imageResizer = imageResizer;
    }

    public Mono<Image> getImage(int id) {
        return Mono.fromCallable(() -> imageRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty)
                .doOnNext(Base64ImageDataCodec::encode);
    }

    public Flux<Image> getThumbnails(String size) {
        return Mono.fromCallable(() -> thumbnailRepository.getThumbnailsBySize(ThumbnailSize.valueOf(size)))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(this::createImageFromThumbnail)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .doOnNext(Base64ImageDataCodec::encode);
    }

    public Mono<Long> getThumbnailsCount(String size) {
        return Mono.fromCallable(() -> thumbnailRepository.countBySize(ThumbnailSize.valueOf(size)))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty);
    }

    public Flux<Image> processImages(Flux<Image> images, String size) {
        return images
                .doOnNext(Base64ImageDataCodec::decode)
                .doOnNext(this::logImageData)
                .map(imageRepository::save)
                .doOnNext(image -> Mono.fromRunnable(() -> generateAndSaveOtherThumbnails(image, ThumbnailSize.valueOf(size)))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe())
                .flatMap(image -> processThumbnailAndReturnImage(image, ThumbnailSize.valueOf(size)))
                .doOnNext(this::logThumbnailData)
                .doOnNext(Base64ImageDataCodec::encode);
    }

    private void logImageData(Image image) {
        logger.log(Level.INFO, "Received image: " + image.getName() + " Size: " + image.getWidth() + "x" + image.getHeight());
    }

    private void logThumbnailData(Image image) {
        logger.log(Level.INFO, "To be sended: ImageId: " + image.getDatabaseId() + ", " + image.getGridPlacementId() +
                " | Size: " + image.getWidth() + "x" + image.getHeight() + " | Status: " + image.getImageState());
    }

    private void logThumbnailData(Thumbnail thumbnail) {
        logger.log(Level.INFO, "Thumbnail: ImageId: " + thumbnail.getImageId() + " | Size: " + thumbnail.getSize() + " | Status: " + thumbnail.getState());
    }

    private Mono<Image> processThumbnailAndReturnImage(Image image, ThumbnailSize thumbnailSize) {
        return generateAndSaveThumbnail(image, thumbnailSize)
                .map(thumbnail -> createImageFromThumbnail(thumbnail, image.getName(), image.getExtensionType(), image.getGridPlacementId()));
    }

    private void generateAndSaveOtherThumbnails(Image image, ThumbnailSize thumbnailSize) {
        Flux.fromStream(Arrays.stream(ThumbnailSize.values())
                        .filter(size -> !size.equals(thumbnailSize)))
                .flatMap(size -> generateAndSaveThumbnail(image, size))
                .subscribe();
    }

    private Mono<Thumbnail> generateAndSaveThumbnail(Image image, ThumbnailSize thumbnailSize) {
        return Mono.fromCallable(() -> imageResizer.createThumbnail(image, thumbnailSize))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(this::logThumbnailData)
                .doOnNext(thumbnailRepository::save);
    }


    private Optional<Image> createImageFromThumbnail(Thumbnail thumbnail) {
        return imageRepository.findByDatabaseId(thumbnail.getImageId())
                .map(imageMetaData ->
                        createImageFromThumbnail(thumbnail, imageMetaData.getName(), imageMetaData.getExtensionType(), -1));
    }

    private Image createImageFromThumbnail(Thumbnail thumbnail, String imageName, String extensionType, int gridId) {
        return Image.builder()
                .databaseId(thumbnail.getImageId())
                .gridPlacementId(gridId)
                .imageState(thumbnail.getState())
                .name(imageName)
                .extensionType(extensionType)
                .width(thumbnail.getSize().getWidth())
                .height(thumbnail.getSize().getHeight())
                .data(thumbnail.getData())
                .build();
    }
}

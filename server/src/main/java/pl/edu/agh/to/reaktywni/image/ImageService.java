package pl.edu.agh.to.reaktywni.image;


import org.slf4j.LoggerFactory;
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
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ImageService.class);

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
                .doOnNext(this::savePlaceholderThumbnails)
                .doOnNext(image -> Mono.fromRunnable(() -> generateAndSaveOtherThumbnails(image, ThumbnailSize.valueOf(size)))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe())
                .flatMap(image -> processThumbnailAndReturnImage(image, ThumbnailSize.valueOf(size)))
                .doOnNext(this::logThumbnailData)
                .doOnNext(Base64ImageDataCodec::encode);
    }

    private void savePlaceholderThumbnails(Image image) {
        Arrays.stream(ThumbnailSize.values())
                .forEach(size -> {
                    Thumbnail placeholder = new Thumbnail(image.getDatabaseId(), size);
                    thumbnailRepository.save(placeholder);
                });
    }

    private void logImageData(Image image) {
        logger.log(Level.INFO, "Received image: " + image.getName() + " Size: " + image.getWidth() + "x" + image.getHeight());
    }

    private void logThumbnailData(Image image) {
        logger.log(Level.INFO, "To be sent: ImageId: " + image.getDatabaseId() + ", " + image.getGridPlacementId() +
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
                .flatMap(thumbnail -> Mono.fromCallable(() -> {
                    Thumbnail placeholder = thumbnailRepository.findByImageIdAndSize(image.getDatabaseId(), thumbnailSize);
                    if (thumbnail.getData().length == 0) {
                        placeholder.setState(ImageState.FAILURE);
                        logger.log(Level.WARNING, "Error while resizing image: " + image.getName());
                    }else {
                        placeholder.setData(thumbnail.getData());
                        placeholder.setState(ImageState.SUCCESS);
                    }
                    thumbnailRepository.save(placeholder);
                    return placeholder;
                }).subscribeOn(Schedulers.boundedElastic()))
                .doOnNext(this::logThumbnailData)
                .doOnError(error -> {
                    Mono.fromCallable(() -> {
                        Thumbnail placeholder = thumbnailRepository.findByImageIdAndSize(image.getDatabaseId(), thumbnailSize);
                        placeholder.setFailure();
                        thumbnailRepository.save(placeholder);
                        return placeholder;
                    }).subscribeOn(Schedulers.boundedElastic()).subscribe();
                });
    }


    private Optional<Image> createImageFromThumbnail(Thumbnail thumbnail) {
        return imageRepository.findByDatabaseId(thumbnail.getImageId())
                .map(image -> createImageFromThumbnail(thumbnail, image.getName(), image.getExtensionType(), -1));
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

    public void reprocessPendingThumbnails() {
        Flux.fromIterable(thumbnailRepository.findByState(ImageState.PENDING))
                .doOnNext(this::logReprocessing)
                .flatMap(thumbnail -> Mono.fromCallable(() -> imageRepository.findByDatabaseId(thumbnail.getImageId()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(Mono::justOrEmpty)
                        .flatMap(image -> generateAndSaveThumbnail(image, thumbnail.getSize()))
                        .doOnNext(this::logSuccessfulReprocessing)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private void logReprocessing(Thumbnail thumbnail) {
        logger.log(Level.INFO, "Reprocessing: " + thumbnail + " | Size: " + thumbnail.getSize());
    }

    private void logSuccessfulReprocessing(Thumbnail thumbnail) {
        logger.info("Processed Thumbnail: " + thumbnail);
    }
}

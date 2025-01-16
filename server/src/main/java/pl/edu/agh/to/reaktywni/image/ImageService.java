package pl.edu.agh.to.reaktywni.image;


import org.springframework.data.domain.Pageable;
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
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;


@Service
public class ImageService {

    private final static Logger logger = Logger.getLogger(ImageService.class.getName());
    //private static final org.slf4j.Logger log = LoggerFactory.getLogger(ImageService.class);

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

    public Flux<Image> getThumbnails(String size, Pageable pageable) {
        return Mono.fromCallable(() -> thumbnailRepository.getThumbnailsBySize(ThumbnailSize.valueOf(size), pageable))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(this::createImageFromThumbnail)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .doOnNext(Base64ImageDataCodec::encode);
    }

    public Flux<Image> getThumbnailsExcludingList(String size, List<Integer> ids, int elemCount) {
        return Mono.fromCallable(() -> thumbnailRepository.getThumbnailsBySizeExcludingList(ThumbnailSize.valueOf(size), ids, elemCount))
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
                .doOnNext(this::saveEmptyThumbnails)
                .doOnNext(image -> Mono.fromRunnable(() -> generateAndSaveOtherThumbnails(image, ThumbnailSize.valueOf(size)))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe())
                .flatMap(image -> processThumbnailAndReturnImage(image, ThumbnailSize.valueOf(size)))
                .doOnNext(this::logProcessedData)
                .doOnNext(Base64ImageDataCodec::encode);
    }

    private void saveEmptyThumbnails(Image image) {
        thumbnailRepository.saveAll(
                Arrays.stream(ThumbnailSize.values())
                        .map(size -> new Thumbnail(image, size))
                        .toList()
        );
    }

    private Mono<Image> processThumbnailAndReturnImage(Image image, ThumbnailSize thumbnailSize) {
        return generateAndUpdateThumbnail(image, thumbnailSize)
                .map(thumbnail -> createImageFromThumbnail(thumbnail, image.getName(), image.getExtensionType(), image.getGridId()));
    }

    private void generateAndSaveOtherThumbnails(Image image, ThumbnailSize thumbnailSize) {
        Flux.fromStream(Arrays.stream(ThumbnailSize.values())
                        .filter(size -> !size.equals(thumbnailSize)))
                .flatMap(size -> generateAndUpdateThumbnail(image, size))
                .subscribe();
    }

    private Mono<Thumbnail> generateAndUpdateThumbnail(Image image, ThumbnailSize thumbnailSize) {
        return Mono.fromCallable(() -> imageResizer.createThumbnail(image, thumbnailSize))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(thumbnail -> updateEmptyThumbnail(thumbnail, image, thumbnailSize))
                .doOnError(error -> updateEmptyThumbnailOnError(image, thumbnailSize)
                        .subscribe());
    }

    private Mono<Thumbnail> updateEmptyThumbnail(Thumbnail readyThumbnail, Image image, ThumbnailSize thumbnailSize) {
        return Mono.fromCallable(() -> {
            Thumbnail emptyThumbnail = thumbnailRepository.findByImageIdAndSize(image.getId(), thumbnailSize);
            if (readyThumbnail.getState().equals(ImageState.SUCCESS)) {
                emptyThumbnail.setData(readyThumbnail.getData());
            } else {
                emptyThumbnail.setFailure();
                logger.log(Level.WARNING, "Resizing image failure: " + image.getName());
            }
            thumbnailRepository.save(emptyThumbnail);
            return emptyThumbnail;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Thumbnail> updateEmptyThumbnailOnError(Image image, ThumbnailSize thumbnailSize) {
        return Mono.fromCallable(() -> {
            Thumbnail emptyThumbnail = thumbnailRepository.findByImageIdAndSize(image.getId(), thumbnailSize);
            emptyThumbnail.setFailure();
            thumbnailRepository.save(emptyThumbnail);
            return emptyThumbnail;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    private Optional<Image> createImageFromThumbnail(Thumbnail thumbnail) {
        return imageRepository.findImageMetaDataById(thumbnail.getImageId())
                .map(image -> createImageFromThumbnail(thumbnail, image.getName(), image.getExtensionType(), -1));
    }

    public void createEmptyThumbnailsIfMissing() {
        long imagesCount = imageRepository.count();
        long thumbnailsCount = thumbnailRepository.count();
        if (imagesCount * ThumbnailSize.SIZES_COUNT == thumbnailsCount) {
            return;
        }
        logger.info("Creating missing thumbnails");
        Flux.fromIterable(imageRepository.findAllIdsWithMissingThumbnails(ThumbnailSize.SIZES_COUNT))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(this::saveMissingThumbnails)
                .blockLast();
    }

    private Image createEmptyImageWithId(int id) {
        Image image = new Image();
        image.setId(id);
        return image;
    }

    private void saveMissingThumbnails(int imageId) {
        List<ThumbnailSize> existingSizes = thumbnailRepository.findByImageId(imageId).stream()
                .map(Thumbnail::getSize)
                .toList();
        List<Thumbnail> missingThumbnails = Arrays.stream(ThumbnailSize.values())
                .filter(size -> !existingSizes.contains(size))
                .map(size -> new Thumbnail(createEmptyImageWithId(imageId), size))
                .toList();
        thumbnailRepository.saveAll(missingThumbnails);
    }

    public void reprocessPendingThumbnails() {
        for (ImageState state : new ImageState[] {ImageState.PENDING, ImageState.FAILURE}) {
            reprocessPendingThumbnails(state);
        }
    }

    private void reprocessPendingThumbnails(ImageState state) {
        Flux.fromIterable(thumbnailRepository.findByStateWithImages(state))
                .doOnNext(this::logReprocessing)
                .flatMap(thumbnail -> Mono.fromCallable(thumbnail::getImage)
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(Mono::justOrEmpty)
                        .flatMap(image -> generateAndUpdateThumbnail(image, thumbnail.getSize()))
                        .doOnNext(this::logSuccessfulReprocessing)
                )
                .subscribe();
    }

    private Image createImageFromThumbnail(Thumbnail thumbnail, String imageName, String extensionType, int gridId) {
        return Image.builder()
                .id(thumbnail.getImageId())
                .gridId(gridId)
                .imageState(thumbnail.getState())
                .name(imageName)
                .extensionType(extensionType)
                .width(thumbnail.getSize().getWidth())
                .height(thumbnail.getSize().getHeight())
                .data(thumbnail.getData())
                .build();
    }

    private void logReprocessing(Thumbnail thumbnail) {
        logger.log(Level.INFO, "Reprocessing: " + thumbnail + " | Size: " + thumbnail.getSize());
    }

    private void logSuccessfulReprocessing(Thumbnail thumbnail) {
        logger.info("Processed Thumbnail: " + thumbnail);
    }

    private void logImageData(Image image) {
        logger.log(Level.INFO, "Received: " + image);
    }

    private void logProcessedData(Image image) {
        logger.log(Level.INFO, "To be sent: " + image);
    }
}

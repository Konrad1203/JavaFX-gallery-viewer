package pl.edu.agh.to.reaktywni.image;


import org.springframework.stereotype.Service;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailRepository;
import pl.edu.agh.to.reaktywni.util.Base64ImageDataCodec;
import pl.edu.agh.to.reaktywni.util.Resizer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;


@Service
public class ImageService {

    private final static Logger logger = Logger.getLogger(ImageService.class.getName());

    private final int THUMBNAIL_WIDTH = 320;
    private final int THUMBNAIL_HEIGHT = 180;

    private final ImageRepository imageRepository;
    private final ThumbnailRepository thumbnailRepository;
    private final Resizer imageResizer;

    public ImageService(ImageRepository imageRepository, ThumbnailRepository thumbnailRepository, Resizer imageResizer) {
        this.imageRepository = imageRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.imageResizer = imageResizer;
    }

    public Mono<Image> getImage(int id) {
        return Mono.justOrEmpty(imageRepository.findById(id))
                .doOnNext(Base64ImageDataCodec::encode);
    }

    public Flux<Image> getThumbnails() {
        return Flux.fromIterable(thumbnailRepository.findAll())
                .map(this::createImageFromThumbnail)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .doOnNext(Base64ImageDataCodec::encode);
    }

    public Mono<Long> getThumbnailsCount() {
        return Mono.justOrEmpty(thumbnailRepository.count());

    }

    public Flux<Image> processImages(Flux<Image> images) {
        return images  //.log()
                .doOnNext(this::printImageData)
                .doOnNext(Base64ImageDataCodec::decode)
                .doOnNext(this::saveImage)
                .flatMap(image -> Mono.fromCallable(() -> imageResizer.resize(image, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT))
                        .subscribeOn(Schedulers.boundedElastic())
                        .doOnNext(this::printProcessedImageData)
                        .doOnNext(this::createAndSaveThumbnail)
                        .doOnNext(Base64ImageDataCodec::encode)
                );
    }

    private void printImageData(Image image) {
        logger.log(Level.FINE, "Received image: " + image.getName() + " Size: " + image.getWidth() + "x" + image.getHeight());
    }

    private void printProcessedImageData(Image image) {
        if (image.getImageState().equals(ImageState.FAILURE)) {
            logger.log(Level.FINE, "Failed to process image: " + image.getName());
        } else {
            logger.log(Level.FINE, "Processed image: " + image.getName() + " Size: " + image.getWidth() + "x" + image.getHeight());
        }
    }

    public void saveImage(Image image) {
        imageRepository.save(image);
    }

    public void createAndSaveThumbnail(Image image) {
        if (image.getImageState().equals(ImageState.SUCCESS)) {
            Thumbnail thumbnail = new Thumbnail(image.getDatabaseId(), 1, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
            thumbnail.setData(image.getData());
            thumbnailRepository.save(thumbnail);
        }
    }

    public Optional<Image> createImageFromThumbnail(Thumbnail thumbnail) {
        return imageRepository.findByDatabaseId(thumbnail.getImageId())
                .map(imageMetaData -> Image.builder()
                                    .name(imageMetaData.getName())
                                    .extensionType(imageMetaData.getExtensionType())
                                    .width(thumbnail.getWidth())
                                    .height(thumbnail.getHeight())
                                    .data(thumbnail.getData())
                                    .databaseId(thumbnail.getImageId())
                                    .build()
                );
    }
}

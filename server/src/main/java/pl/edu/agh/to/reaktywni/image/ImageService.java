package pl.edu.agh.to.reaktywni.image;

import org.springframework.stereotype.Service;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailRepository;
import pl.edu.agh.to.reaktywni.util.Resizer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;


@Service
public class ImageService {

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

    public Optional<Image> getImage(int id) {
        return imageRepository.findById(id);
    }

    public Flux<Image> getThumbnails() {
        return Flux.fromIterable(thumbnailRepository.findAll())
                .map(this::createImageFromThumbnail);
    }

    public long getImagesCount() {
        return imageRepository.count();
    }

    public Flux<Image> processImages(Flux<Image> images) {

        return images
                .doOnNext(this::printImageData)
                .doOnNext(this::saveImage)
                .flatMap(image -> Mono.fromCallable(() -> imageResizer.resize(image, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT))
                        .subscribeOn(Schedulers.boundedElastic())
                        .doOnNext(this::printProcessedImageData)
                        .doOnNext(this::createAndSaveThumbnail)
                );
    }

    private void printImageData(Image image) {
        System.out.println("Received image: " + image.getName() + " Size: " + image.getWidth() + "x" + image.getHeight());
    }

    private void printProcessedImageData(Image image) {
        if(image.getImageState().equals(ImageState.FAILURE)) {
            System.out.println("Failed to process image: " + image.getName());
        }else {
            System.out.println("Processed image: " + image.getName() + " Size: " + image.getWidth() + "x" + image.getHeight());
        }
    }

    public void saveImage(Image image) {
        imageRepository.save(image);
    }

    public void createAndSaveThumbnail(Image image) {
        if(image.getImageState().equals(ImageState.SUCCESS)) {
            Thumbnail thumbnail = new Thumbnail(image.getDatabaseId(), THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, image.getData());
            thumbnailRepository.save(thumbnail);
        }
    }

    public Image createImageFromThumbnail(Thumbnail thumbnail) {
        Optional<ImageMetaData> optionalImage = imageRepository.findByDatabaseId(thumbnail.getImageId());
        if (optionalImage.isPresent()) {
            ImageMetaData imageMetaData = optionalImage.get();
            Image image = new Image(imageMetaData.getName(), imageMetaData.getExtensionType(),
                    thumbnail.getWidth(), thumbnail.getHeight(), thumbnail.getData());
            image.setDatabaseId(thumbnail.getImageId());
            return image;
        } else {
            throw new RuntimeException("Image not found");
        }
    }
}

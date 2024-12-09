package pl.edu.agh.to.reaktywni.image;

import org.springframework.stereotype.Service;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailRepository;
import pl.edu.agh.to.reaktywni.util.Resizer;
import reactor.core.publisher.Flux;
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

    public Optional<ImageDTO> getImage(int id) {
        return imageRepository.findById(id).map(ImageDTO::createFromImage);
    }

    public Flux<ImageDTO> getThumbnails() {
        return Flux.fromIterable(thumbnailRepository.findAll()).map(imageDTO -> ImageDTO.createFromThumbnail(imageDTO, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));
    }

    public long getImagesCount() {
        return imageRepository.count();
    }

    public Flux<ImageDTO> processImages(Flux<ImageDTO> images) {

        return images.map(Image::createFromImageDTO)
                .doOnNext(this::printImageData)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(this::saveImage)
                .map(image -> imageResizer.resize(image, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(this::saveThumbnail)
                .map(thumbnail -> ImageDTO.createFromThumbnail(thumbnail, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));
    }

    private void printImageData(Image image) {
        System.out.println("Received image: " + image.getName() + " Size: " + image.getWidth() + "x" + image.getHeight());
    }

    public void saveImage(Image image) {
        imageRepository.save(image);
    }

    public void saveThumbnail(Thumbnail thumbnail) {
        thumbnailRepository.save(thumbnail);
    }
}

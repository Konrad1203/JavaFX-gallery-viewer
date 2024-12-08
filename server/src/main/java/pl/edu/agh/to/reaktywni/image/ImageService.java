package pl.edu.agh.to.reaktywni.image;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailRepository;
import pl.edu.agh.to.reaktywni.util.Resizable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final ThumbnailRepository thumbnailRepository;
    private final Resizable imageResizer;

    public ImageService(ImageRepository imageRepository, ThumbnailRepository thumbnailRepository, Resizable imageResizer) {
        this.imageRepository = imageRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.imageResizer = imageResizer;

    }

    public Flux<ImageDTO> processImages(Flux<ImageDTO> images){
        return images.doOnNext(imageDTO -> System.out.println("Received image: " + imageDTO.getName() +
                               "\tWidth: " + imageDTO.getWidth() + "\tHeight: " + imageDTO.getHeight())
                ).flatMap(imageDTO -> Mono.fromCallable(() -> imageResizer.resize(imageDTO, 100, 100))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(optionalImageDTO -> Mono.justOrEmpty(optionalImageDTO)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Failed to resize image: " + imageDTO.getName())))
                        )
                ).doOnNext(imageDTO -> {
                    System.out.println("Processed image: " + imageDTO.getName());
                    System.out.println("Width: " + imageDTO.getWidth() + "\tHeight: " + imageDTO.getHeight());
                });
    }

    public int saveImage(Image image) {
        imageRepository.save(image);
        return image.getId();
    }

    public byte[] getImage(int id) {
        return imageRepository.findById(id).map(Image::getImageData).orElse(null);
    }

    public void saveThumbnail(Thumbnail thumbnail) {
        thumbnailRepository.save(thumbnail);
    }


    public ResponseEntity<String> saveImages(MultipartFile[] files) {
        // TO DO
        System.out.println("Files uploaded: " + files.length);
        return ResponseEntity.ok("Files uploaded successfully!");
    }

    public Flux<Image> getImages() {
        return Flux.fromIterable(imageRepository.findAll());
    }

    public long getImagesCount() {
        return imageRepository.count();
    }
}

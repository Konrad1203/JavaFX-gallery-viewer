package pl.edu.agh.to.reaktywni.image;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailRepository;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final ThumbnailRepository thumbnailRepository;

    public ImageService(ImageRepository imageRepository, ThumbnailRepository thumbnailRepository) {
        this.imageRepository = imageRepository;
        this.thumbnailRepository = thumbnailRepository;
    }

    public int saveImage(Image image) {
        imageRepository.save(image);
        return image.getId();
    }

    public void saveThumbnail(Thumbnail thumbnail) {
        thumbnailRepository.save(thumbnail);
    }

    private static final String UPLOAD_FOLDER = "uploads";

    static {
        File directory = new File(UPLOAD_FOLDER);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public ResponseEntity<String> saveImages(MultipartFile[] files) {

        System.out.println("Files uploaded: " + files.length);

        return ResponseEntity.ok("Files uploaded successfully!");
    }

    public byte[] getImageData(int id) {
        try {
            Path path = Paths.get(UPLOAD_FOLDER + File.separator + id);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Flux<Image> getImages() {
        return Flux.fromIterable(imageRepository.findAll());
    }
}

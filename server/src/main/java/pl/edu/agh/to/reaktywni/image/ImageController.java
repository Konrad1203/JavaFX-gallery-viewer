package pl.edu.agh.to.reaktywni.image;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;


@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    public Mono<ImageDTO> getImage(@PathVariable int id) {
        Optional<ImageDTO> image = imageService.getImage(id);
        if (image.isPresent()) {
            System.out.println("Zdjęcie znalezione: " + image.get().getName());
            return Mono.just(image.get());
        } else {
            System.out.println("Zdjęcie nie znalezione");
            return Mono.error(new RuntimeException("Image not found"));
        }
    }

    @GetMapping
    public Flux<ImageDTO> getThumbnails() {
        return imageService.getThumbnails();
    }

    @GetMapping("/count")
    public long getImagesCount() {
        return imageService.getImagesCount();
    }

    @PostMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ImageDTO> postImages(@RequestBody Flux<ImageDTO> images) {
        return imageService.processImages(images);
    }
}


package pl.edu.agh.to.reaktywni.image;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


@RestController
@RequestMapping("/images")
public class ImageController {

    private final static Logger logger = Logger.getLogger(ImageController.class.getName());

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    public Mono<Image> getImage(@PathVariable int id) {
        Optional<Image> image = imageService.getImage(id);
        if (image.isPresent()) {
            logger.info("Image found: " + image.get().getName());
            return Mono.just(image.get());
        } else {
            logger.log(Level.WARNING, "Image not found");
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));
        }
    }

    @GetMapping("/thumbnails")
    public Flux<Image> getThumbnails() {
        return imageService.getThumbnails();
    }

    @GetMapping("/count")
    public long getImagesCount() {
        return imageService.getImagesCount();
    }

    @GetMapping("/thumbnails/count")
    public long getThumbnailsCount() {
        return imageService.getThumbnailsCount();
    }

    @PostMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Image> postImages(@RequestBody Flux<Image> images) {
        return imageService.processImages(images);
    }
}


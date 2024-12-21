package pl.edu.agh.to.reaktywni.image;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.stream.Stream;


@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{id}")
    public Mono<Image> getImage(@PathVariable int id) {
        return imageService.getImage(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found")));
    }

    @PostMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Image> postImages(@RequestBody Flux<Image> images) {
        return imageService.processImages(images);
    }

    @GetMapping("/thumbnails")
    public Flux<Image> getThumbnails(@RequestParam String size) {
        if (Stream.of("SMALL", "MEDIUM", "LARGE").noneMatch(size::equalsIgnoreCase)) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid thumbnail size"));
        }
        return imageService.getThumbnails(size);
    }

    @GetMapping("/thumbnails/count")
    public Mono<Long> getThumbnailsCount() {
        return imageService.getThumbnailsCount()
                .switchIfEmpty(Mono.error(new IllegalStateException("Cannot get thumbnails count")));
    }


}


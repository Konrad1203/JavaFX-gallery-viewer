package pl.edu.agh.to.reaktywni.image;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Stream;


@RestController
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/images/{id}")
    public Mono<Image> getImage(@PathVariable int id) {
        return imageService.getImage(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found")));
    }

    @PostMapping(path="/images", consumes = MediaType.APPLICATION_NDJSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Image> postImages(@RequestBody Flux<Image> images, @RequestParam String size) {
        if (Stream.of(ThumbnailSize.values()).map(Enum::toString).noneMatch(size::equalsIgnoreCase)) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid thumbnail size"));
        }
        return imageService.processImages(images, size);
    }

    @GetMapping("/thumbnails")
    public Flux<Image> getThumbnails(@RequestParam String size) {
        if (Stream.of(ThumbnailSize.values()).map(Enum::toString).noneMatch(size::equalsIgnoreCase)) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid thumbnail size"));
        }
        return imageService.getThumbnails(size);
    }

    @GetMapping("/thumbnails/excluding")
    public Flux<Image> getThumbnailsExcludingSet(@RequestParam String size, @RequestParam Set<Integer> ids) {
        if (Stream.of(ThumbnailSize.values()).map(Enum::toString).noneMatch(size::equalsIgnoreCase)) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid thumbnail size"));
        }
        return imageService.getThumbnailsExcludingSet(size, ids);
    }

    @GetMapping("/thumbnails/count")
    public Mono<Long> getThumbnailsCount(@RequestParam String size) {
        if (Stream.of(ThumbnailSize.values()).map(Enum::toString).noneMatch(size::equalsIgnoreCase)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid thumbnail size"));
        }
        return imageService.getThumbnailsCount(size)
                .switchIfEmpty(Mono.error(new IllegalStateException("Cannot get thumbnails count")));
    }
}


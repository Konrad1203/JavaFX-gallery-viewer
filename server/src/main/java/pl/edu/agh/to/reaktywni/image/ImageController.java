package pl.edu.agh.to.reaktywni.image;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


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
    public Flux<Image> postImages(@RequestBody Flux<Image> images, @RequestParam String size) {
        if (!ThumbnailSize.isValidSize(size)) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid thumbnail size"));
        }
        return imageService.processImages(images, size);
    }
}


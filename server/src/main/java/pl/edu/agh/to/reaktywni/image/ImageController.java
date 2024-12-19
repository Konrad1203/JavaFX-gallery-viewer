package pl.edu.agh.to.reaktywni.image;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
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
        return imageService.getImage(id);
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
    public Mono<Long> getThumbnailsCount() {
        return imageService.getThumbnailsCount();
    }

    @PostMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Image> postImages(@RequestBody Flux<Image> images) {
        return imageService.processImages(images);
    }
}


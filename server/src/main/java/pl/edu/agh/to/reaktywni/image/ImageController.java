package pl.edu.agh.to.reaktywni.image;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailSize;
import pl.edu.agh.to.reaktywni.util.Directory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


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
    public Flux<Image> postImages(@RequestBody Flux<Image> images, @RequestParam String size, @RequestParam String directoryPath) {
        System.out.println(directoryPath);
        if (!ThumbnailSize.isValidSize(size)) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid thumbnail size"));
        }
        return imageService.processImages(images, size, directoryPath);
    }

    @PostMapping("/deleteDirectory")
    public void deleteImages(@RequestBody String directoryPath) {
        imageService.deleteImagesFromDirectory(directoryPath);
    }

    @PostMapping("/deleteImages")
    public void deleteImages(@RequestBody List<Integer> imageIds) {
        imageService.deleteImagesWithId(imageIds);
    }

    @GetMapping("/directoryTree")
    public Mono<Directory> getDirectoryTree() {
        return imageService.getDirectoryTree()
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No directory tree found")));
    }

    @PostMapping("/directoryTree")
    public void postDirectoryTree(@RequestBody Directory directory) {
        imageService.mergeToDirectoryTree(directory);
    }

    @PostMapping("/moveImages")
    public void moveImagesToDirectory(@RequestBody List<Integer> imageIds, @RequestParam String directoryPath) {
        imageService.moveImagesToDirectory(imageIds, directoryPath);
    }
}


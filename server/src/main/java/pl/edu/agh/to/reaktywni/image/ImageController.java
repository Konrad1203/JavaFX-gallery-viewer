package pl.edu.agh.to.reaktywni.image;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/images")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    public Flux<Image> getImages() {
        return imageService.getImages();
    }

    @PostMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ImageDTO> processImages(@RequestBody Flux<ImageDTO> images) {
        return imageService.processImages(images);
    }

    @GetMapping("/{id}")
    public byte[] getImage(@PathVariable int id) {
        return imageService.getImageData(id);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> handleFileUploads(@RequestPart(value = "files") MultipartFile[] files) {
        return imageService.saveImages(files);
    }
}


package pl.edu.agh.to.reaktywni.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.to.reaktywni.util.ImageResizer;
import pl.edu.agh.to.reaktywni.util.Resizable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Arrays;


@RestController
@RequestMapping("/images")
public class ImageController {
    private final Resizable imageResizer;
    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService, Resizable imageResizer) {
        this.imageService = imageService;
        this.imageResizer = imageResizer;
    }

    @GetMapping
    public Flux<Image> getImages() {
        return imageService.getImages();
    }

    @PostMapping(consumes = MediaType.APPLICATION_NDJSON_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ImageDTO> processImages(@RequestBody Flux<ImageDTO> images) {
        return images
                .doOnNext(imageDTO -> {
                    System.out.println("Received image: " + imageDTO.getName());
                    System.out.println("Width: " + imageDTO.getWidth() + "\tHeight: " + imageDTO.getHeight());
                    //System.out.println("Data: " + Arrays.toString(imageDTO.getData()));
                    imageDTO.setName(imageDTO.getName().toUpperCase());
                })
                .map(imageDTO -> imageResizer.resize(imageDTO, 100, 100))
                .flatMap(optionalImageDTO -> {
                    if (optionalImageDTO.isPresent()) {
                        return Mono.just(optionalImageDTO.get());
                    } else {
                        return Mono.error(new IllegalArgumentException("Failed to resize image: " + optionalImageDTO));
                    }
                })
                .doOnNext(imageDTO -> {
                    System.out.println("Processed image: " + imageDTO.getName());
                    System.out.println("Width: " + imageDTO.getWidth() + "\tHeight: " + imageDTO.getHeight());
                });
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


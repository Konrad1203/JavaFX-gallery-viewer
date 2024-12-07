package pl.edu.agh.to.reaktywni.image;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.to.reaktywni.util.ImageResizer;
import pl.edu.agh.to.reaktywni.util.Resizable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Arrays;


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
        Resizable imageResizer = new ImageResizer();
        return images
                .doOnNext(imageDTO -> {
                    System.out.println("Received image: " + imageDTO.getName());
                    System.out.println("Width: " + imageDTO.getWidth() + "\tHeight: " + imageDTO.getHeight());
                    //System.out.println("Data: " + Arrays.toString(imageDTO.getData()));
                    imageDTO.setName(imageDTO.getName().toUpperCase());
                })
                .map(imageDTO -> {
                    try {
                        return imageResizer.resize(imageDTO, 100, 100);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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


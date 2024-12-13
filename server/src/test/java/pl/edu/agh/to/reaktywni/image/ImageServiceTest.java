package pl.edu.agh.to.reaktywni.image;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailRepository;
import pl.edu.agh.to.reaktywni.util.Resizer;
import reactor.core.publisher.Flux;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ThumbnailRepository thumbnailRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private Resizer imageResizer;

    private static Image getTestImage(int width, int height, int size) {
        return Image.builder()
                .gridPlacementId(1)
                .name("test-image")
                .extensionType("jpg")
                .width(width)
                .height(height)
                .data(new byte[size])
                .build();
    }

    private static Image getExistingImage(ByteArrayOutputStream byteArrayOutputStream, BufferedImage originalImage) {
        byte[] imageData = byteArrayOutputStream.toByteArray();

        return Image.builder()
                .name("test-image")
                .extensionType("jpg")
                .data(imageData)
                .width(originalImage.getWidth())
                .height(originalImage.getHeight())
                .build();
    }

    @Test
    public void testProcessImages_Success() throws IOException {
        File imageFile = new File("src/test/resources/test-tiger.jpg");
        if (!imageFile.exists()) {
            throw new IOException("Image doesn't exist at path: " + imageFile.getAbsolutePath());
        }

        BufferedImage originalImage = ImageIO.read(imageFile);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", byteArrayOutputStream);
        Image image = getExistingImage(byteArrayOutputStream, originalImage);
        imageRepository.save(image);

        Flux<Image> processedImages = imageService.processImages(Flux.just(image));

        processedImages
                .doOnNext(i -> {
                    assertNotNull(i);
                    assertEquals(320, i.getWidth());
                    assertEquals(180, i.getHeight());
                    assertEquals("test-image", i.getName());
                })
                .blockLast();
    }

    @Test
    public void testGetThumbnails() {
        Thumbnail thumbnail = new Thumbnail(1, 320, 180, new byte[50]);
        thumbnailRepository.save(thumbnail);

        Image image = getTestImage(500, 300, 100);
        imageRepository.save(image);

        Flux<Image> thumbnails = imageService.getThumbnails();

        thumbnails.doOnNext(t -> {
                    assertEquals(320, t.getWidth());
                    assertEquals(180, t.getHeight());
                    assertArrayEquals(new byte[50], t.getData());
                }).blockLast();
    }

    @Test
    public void getImagesCountTest() {
        Image image1 = getTestImage(500, 300, 100);
        Image image2 = getTestImage(500, 300, 100);
        Image image3 = getTestImage(500, 300, 100);

        imageRepository.save(image1);
        imageRepository.save(image2);
        imageRepository.save(image3);

        long count = imageService.getImagesCount();

        assertEquals(3L, count);
    }
}

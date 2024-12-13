package pl.edu.agh.to.reaktywni.util;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pl.edu.agh.to.reaktywni.image.Image;
import pl.edu.agh.to.reaktywni.image.ImageState;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ImageResizerTest {

    private static Image getResizedImage(ByteArrayOutputStream byteArrayOutputStream, BufferedImage originalImage) {
        byte[] imageData = byteArrayOutputStream.toByteArray();

        Image realImage = Image.builder()
                .name("test-image")
                .extensionType("jpg")
                .data(imageData)
                .width(originalImage.getWidth())
                .height(originalImage.getHeight())
                .build();

        ImageResizer resizer = new ImageResizer();

        return resizer.resize(realImage, 100, 100);
    }

    @Test
    void testResizeWithRealImage() throws IOException {
        File imageFile = new File("src/test/resources/test-tiger.jpg");
        if (!imageFile.exists()) {
            throw new IOException("Image doesn't exist at path: " + imageFile.getAbsolutePath());
        }

        BufferedImage originalImage = ImageIO.read(imageFile);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", byteArrayOutputStream);
        Image resizedImage = getResizedImage(byteArrayOutputStream, originalImage);

        assertNotNull(resizedImage);
        assertEquals(100, resizedImage.getWidth());
        assertEquals(100, resizedImage.getHeight());
        assertEquals(ImageState.SUCCESS, resizedImage.getImageState());
        assertNotNull(resizedImage.getData());
    }
}

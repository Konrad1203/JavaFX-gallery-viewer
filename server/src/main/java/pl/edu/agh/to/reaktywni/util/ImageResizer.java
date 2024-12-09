package pl.edu.agh.to.reaktywni.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.image.Image;
import pl.edu.agh.to.reaktywni.image.ImageDTO;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;


@Component
public class ImageResizer implements Resizer {

    @Override
    public Thumbnail resize(Image image, int targetWidth, int targetHeight) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(image.getImageData());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            BufferedImage originalImage = ImageIO.read(inputStream);
            BufferedImage thumbnail = Thumbnails.of(originalImage)
                    .size(targetWidth, targetHeight)
                    .keepAspectRatio(true)
                    .asBufferedImage();

            ImageIO.write(thumbnail, image.getExtensionType(), outputStream);
        } catch (IOException e) {
            return null;
        }

        return new Thumbnail(image, outputStream.toByteArray());
    }
}

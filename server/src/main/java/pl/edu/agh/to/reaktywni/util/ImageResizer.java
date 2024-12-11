package pl.edu.agh.to.reaktywni.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;


@Component
public class ImageResizer implements Resizer {

    @Override
    public Optional<Image> resize(Image image, int targetWidth, int targetHeight) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(image.getData());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            BufferedImage originalImage = ImageIO.read(inputStream);
            BufferedImage thumbnail = Thumbnails.of(originalImage)
                    .size(targetWidth, targetHeight)
                    .keepAspectRatio(true)
                    .asBufferedImage();

            ImageIO.write(thumbnail, image.getExtensionType(), outputStream);
        } catch (IOException e) {
            return Optional.empty();
        }
        image.setData(outputStream.toByteArray());
        image.setWidth(targetWidth);
        image.setHeight(targetHeight);
        return Optional.of(image);
    }
}

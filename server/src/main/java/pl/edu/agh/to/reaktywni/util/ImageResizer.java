package pl.edu.agh.to.reaktywni.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.image.Image;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import pl.edu.agh.to.reaktywni.image.ImageState;


@Component
public class ImageResizer implements Resizer {

    private static final Logger logger = Logger.getLogger(ImageResizer.class.getName());

    @Override
    public Image resize(Image image, int targetWidth, int targetHeight) {
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
            logger.warning("Error while resizing image: " + e.getMessage());
            setFailureImage(image);
            return image;
        }
        setSuccessImage(image, outputStream, targetWidth, targetHeight);
        return image;
    }

    private void setFailureImage(Image image) {
        image.setImageState(ImageState.FAILURE);
        image.setData(new byte[0]);
        image.setWidth(0);
        image.setHeight(0);
    }

    private void setSuccessImage(Image image, ByteArrayOutputStream outputStream, int targetWidth, int targetHeight) {
        image.setImageState(ImageState.SUCCESS);
        image.setData(outputStream.toByteArray());
        image.setWidth(targetWidth);
        image.setHeight(targetHeight);
    }
}

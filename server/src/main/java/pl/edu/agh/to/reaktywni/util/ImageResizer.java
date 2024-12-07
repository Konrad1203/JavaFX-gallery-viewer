package pl.edu.agh.to.reaktywni.util;

import net.coobird.thumbnailator.Thumbnails;
import pl.edu.agh.to.reaktywni.image.ImageDTO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageResizer implements Resizable {

        public ImageDTO resize(ImageDTO imageDTO, int targetWidth, int targetHeight) throws IOException {
            if (imageDTO.getData() == null || imageDTO.getData().length == 0) {
                throw new IOException("Image data is null or empty");
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageDTO.getData());
            BufferedImage originalImage = ImageIO.read(inputStream);

            BufferedImage thumbnail = Thumbnails.of(originalImage)
                    .size(targetWidth, targetHeight)
                    .keepAspectRatio(true)
                    .asBufferedImage();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, imageDTO.getExtensionType(), outputStream);

            return ImageDTO.builder()
                            .idOfPutting(imageDTO.getIdOfPutting())
                            .extensionType(imageDTO.getExtensionType())
                            .name(imageDTO.getName())
                            .width(targetWidth)
                            .height(targetHeight)
                            .data(outputStream.toByteArray())
                            .build();
        }
}

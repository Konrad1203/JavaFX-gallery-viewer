package pl.edu.agh.to.reaktywni.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.image.ImageDTO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Component
public class ImageResizer implements Resizable {
    @Override
        public Optional<ImageDTO> resize(ImageDTO imageDTO, int targetWidth, int targetHeight){
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageDTO.getData());
            BufferedImage originalImage = null;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {
                originalImage = ImageIO.read(inputStream);
                BufferedImage thumbnail = Thumbnails.of(originalImage)
                        .size(targetWidth, targetHeight)
                        .keepAspectRatio(true)
                        .asBufferedImage();

                ImageIO.write(thumbnail, imageDTO.getExtensionType(), outputStream);
            } catch (IOException e) {
                return Optional.empty();
            }
            return Optional.of(
                            ImageDTO.builder()
                            .idOfPutting(imageDTO.getIdOfPutting())
                            .extensionType(imageDTO.getExtensionType())
                            .name(imageDTO.getName())
                            .width(targetWidth)
                            .height(targetHeight)
                            .data(outputStream.toByteArray())
                            .build()
                                );
        }
}

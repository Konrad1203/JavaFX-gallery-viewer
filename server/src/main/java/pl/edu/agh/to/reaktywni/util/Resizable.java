package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.image.ImageDTO;

import java.io.IOException;
import java.util.Optional;

@FunctionalInterface
public interface Resizable {
    Optional<ImageDTO> resize(ImageDTO imageDTO, int targetWidth, int targetHeight);
}

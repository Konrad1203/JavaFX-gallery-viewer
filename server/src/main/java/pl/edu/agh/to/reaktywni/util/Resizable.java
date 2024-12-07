package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.image.ImageDTO;

import java.io.IOException;

@FunctionalInterface
public interface Resizable {
    ImageDTO resize(ImageDTO imageDTO, int targetWidth, int targetHeight) throws IOException;
}

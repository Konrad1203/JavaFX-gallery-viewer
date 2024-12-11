package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.image.Image;

import java.util.Optional;


@FunctionalInterface
public interface Resizer {

    Optional<Image> resize(Image image, int targetWidth, int targetHeight);
}

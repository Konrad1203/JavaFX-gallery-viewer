package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.image.Image;

import java.util.Optional;


@FunctionalInterface
public interface Resizer {

    Image resize(Image image, int targetWidth, int targetHeight);
}

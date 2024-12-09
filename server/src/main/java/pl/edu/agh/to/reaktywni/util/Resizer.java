package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.image.Image;
import pl.edu.agh.to.reaktywni.image.ImageDTO;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;

import java.util.Optional;

@FunctionalInterface
public interface Resizer {

    Thumbnail resize(Image image, int targetWidth, int targetHeight);
}

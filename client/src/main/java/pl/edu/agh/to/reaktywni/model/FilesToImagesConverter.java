package pl.edu.agh.to.reaktywni.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface FilesToImagesConverter {
    static List<Image> convertWithPositionsCounting(List<File> files, int positionCounter) {
        List<Image> images = new ArrayList<>();
        for (File file : files) {
            images.add(Image.createFromFile(positionCounter, file));
            positionCounter++;
        }
        return images;
    }
}

package pl.edu.agh.to.reaktywni.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface FilesToImagesConverter {

    static List<Image> convertWithPositionsCounting(List<File> files, int gridPlacementCounter) {
        List<Image> images = new ArrayList<>();
        for (File file : files) {
            images.add(Image.createFromFile(gridPlacementCounter, file));
            gridPlacementCounter++;
        }
        return images;
    }
}

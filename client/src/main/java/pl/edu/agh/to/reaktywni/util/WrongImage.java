package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.model.Image;
import pl.edu.agh.to.reaktywni.model.ImageState;

import java.io.File;
import java.io.IOException;

import static pl.edu.agh.to.reaktywni.util.FilesToImagesConverter.createFromFile;

public class WrongImage {
    private static final String path = "src/main/resources/GUI/error.png";
    private static Image image;

    static{
        try {
            image = createFromFile(0, new File(path));
        }catch (IOException e) {
            //todo log error
            System.out.println("Error while creating image from file: " + path);
            image = Image.getBuilder().build();
        }
    }

    public static Image getWrongImage() {
        return image;
    }

    public static Image convert(Image image){
        image.setData(image.getData());
        image.setWidth(image.getWidth());
        image.setHeight(image.getHeight());
        return image;
    }
}

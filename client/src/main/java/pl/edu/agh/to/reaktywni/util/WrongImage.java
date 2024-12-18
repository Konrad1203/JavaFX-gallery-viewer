package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.model.Image;
import pl.edu.agh.to.reaktywni.model.ImageState;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.edu.agh.to.reaktywni.util.FilesToImagesConverter.createFromFile;

public class WrongImage {
    private static final Logger logger = Logger.getLogger(WrongImage.class.getName());

    private static final String path = "src/main/resources/GUI/error.png";
    private static Image image;

    static{
        try {
            image = createFromFile(0, new File(path));
        }catch (IOException e) {
            //todo log error
            logger.log(Level.WARNING, "Error while creating image from file: " + path);
            image = Image.getBuilder().build();
        }
    }

    public static Image getWrongImage() {
        return image;
    }

    public static Image convert(Image image){
        image.setData(WrongImage.image.getData());
        image.setWidth(WrongImage.image.getWidth());
        image.setHeight(WrongImage.image.getHeight());
        return image;
    }

    public static Image convertIfStateEqualsFailure(Image image){
        return image.getImageState().equals(ImageState.FAILURE) ? convert(image) : image;
    }
}

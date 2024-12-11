package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.model.Image;

import java.util.Base64;


public interface Base64ImageDataEncoder {

    static Image encode(Image image){
        String encodedData = Base64.getEncoder().encodeToString(image.getData());
        image.setData(encodedData.getBytes());
        return image;
    }
}

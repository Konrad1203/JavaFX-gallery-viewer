package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.model.ImageDTO;

import java.util.Base64;

public interface ImageDTOArrayEncoder {
    static ImageDTO encode(ImageDTO imageDTO){
        String encodedData = Base64.getEncoder().encodeToString(imageDTO.getData());
        imageDTO.setData(encodedData.getBytes());
        return imageDTO;
    }
}

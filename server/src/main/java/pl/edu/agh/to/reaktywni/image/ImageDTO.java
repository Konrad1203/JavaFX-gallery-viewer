package pl.edu.agh.to.reaktywni.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Getter
public class ImageDTO {
    private int idOfPutting;
    @Setter
    private String name;
    private String extensionType;
    private int width;
    private int height;
    private byte[] data;

    public ImageDTO() {}

}

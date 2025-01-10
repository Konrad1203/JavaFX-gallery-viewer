package pl.edu.agh.to.reaktywni.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Builder
public class Image {

    private int id;

    @Setter private int gridId;

    private ImageState imageState;

    private String name;

    private final String extensionType;

    @Setter private int width;

    @Setter private int height;

    @Setter private byte[] data;


    public static ImageBuilder getBuilder() {
        return new ImageBuilder();
    }

    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", gridId=" + gridId +
                ", imageState=" + imageState +
                ", name='" + name + '\'' +
                ", extensionType='" + extensionType + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", data_len=" + data.length +
                '}';
    }
}


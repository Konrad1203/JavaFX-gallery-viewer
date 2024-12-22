package pl.edu.agh.to.reaktywni.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Builder
public class Image {

    private int id;

    private final int gridId;

    private ImageState imageState;

    private String name;

    private final String extensionType;

    @Setter private int width;

    @Setter private int height;

    @Setter private byte[] data;


    public static ImageBuilder getBuilder() {
        return new ImageBuilder();
    }
}


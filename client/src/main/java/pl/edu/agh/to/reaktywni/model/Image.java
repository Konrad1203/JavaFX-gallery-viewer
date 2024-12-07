package pl.edu.agh.to.reaktywni.model;

import java.util.Set;

public class Image {

    private int id;
    private String name;
    private String extensionType;
    private int width;
    private int height;
    private byte[] data;
    private Set<Thumbnail> thumbnails;


    public Image() {}

    public Image(String name, String extensionType, int width, int height, byte[] data) {
        this.name = name;
        this.extensionType = extensionType;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExtensionType() {
        return extensionType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getImageData() {
        return data;
    }
}

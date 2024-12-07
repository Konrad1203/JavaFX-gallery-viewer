package pl.edu.agh.to.reaktywni.image;

public class ImageDTO {
    private int idOfPutting;
    private String name;
    private String extensionType;
    private int width;
    private int height;
    private byte[] data;

    public ImageDTO() {}

    public void setName(String name) {
        this.name = name;
    }

    public int getIdOfPutting() {
        return idOfPutting;
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

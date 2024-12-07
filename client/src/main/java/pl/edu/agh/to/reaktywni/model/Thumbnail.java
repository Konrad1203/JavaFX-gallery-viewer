package pl.edu.agh.to.reaktywni.model;

public class Thumbnail {

    private int id;
    private ImageDTO image;
    private byte[] data;
    private int state;

    public Thumbnail() {}

    public Thumbnail(ImageDTO image, byte[] data) {
        this.image = image;
        this.data = data;
        this.state = 0;
    }

    public int getId() {
        return id;
    }
}

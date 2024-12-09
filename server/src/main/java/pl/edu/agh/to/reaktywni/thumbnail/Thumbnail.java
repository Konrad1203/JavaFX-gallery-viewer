package pl.edu.agh.to.reaktywni.thumbnail;

import jakarta.persistence.*;
import pl.edu.agh.to.reaktywni.image.Image;

@Entity
public class Thumbnail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Lob
    private byte[] data;

    private int state;

    public Thumbnail() {}

    public Thumbnail(Image image, byte[] data) {
        this.image = image;
        this.data = data;
    }

    public Image getImage() {
        return image;
    }

    public byte[] getData() {
        return data;
    }
}

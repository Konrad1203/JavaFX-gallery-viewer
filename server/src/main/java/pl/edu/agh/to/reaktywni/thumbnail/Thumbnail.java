package pl.edu.agh.to.reaktywni.thumbnail;

import jakarta.persistence.*;
import lombok.Getter;
import pl.edu.agh.to.reaktywni.image.Image;


@Getter
@Entity
public class Thumbnail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int imageId;

    private int width;

    private int height;

    @Lob
    private byte[] data;

    private int state;


    public Thumbnail() {}

    public Thumbnail(int imageId, int width, int height, byte[] data) {
        this.imageId = imageId;
        this.width = width;
        this.height = height;
        this.data = data;
    }
}

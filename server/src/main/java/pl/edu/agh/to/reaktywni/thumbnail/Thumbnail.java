package pl.edu.agh.to.reaktywni.thumbnail;

import jakarta.persistence.*;
import lombok.Getter;
import pl.edu.agh.to.reaktywni.image.ImageState;


@Getter
@Entity
public class Thumbnail {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int imageId;

    private int type; // size {small, medium, large}

    private int width;

    private int height;

    @Lob private byte[] data;

    private ImageState state = ImageState.PENDING;

    public Thumbnail() {}

    public Thumbnail(int imageId, int type, int width, int height) {
        this.imageId = imageId;
        this.type = type;
        this.width = width;
        this.height = height;
    }

    public void setData(byte[] data) {
        this.data = data;
        this.state = ImageState.SUCCESS;
    }

    public void setFailure() {
        this.state = ImageState.FAILURE;
    }
}

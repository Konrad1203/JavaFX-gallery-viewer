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

    private ThumbnailSize size;

    @Lob private byte[] data;

    private ImageState state = ImageState.PENDING;

    public Thumbnail() {}

    public Thumbnail(int imageId, ThumbnailSize size) {
        this.imageId = imageId;
        this.size = size;
    }

    public void setData(byte[] data) {
        this.data = data;
        this.state = ImageState.SUCCESS;
    }

    public void setFailure() {
        this.state = ImageState.FAILURE;
    }
}

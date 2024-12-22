package pl.edu.agh.to.reaktywni.thumbnail;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.to.reaktywni.image.ImageState;


@Getter
@Entity
public class Thumbnail {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int imageId;

    private ThumbnailSize size;

    @Lob private byte[] data;

    @Setter
    @Enumerated(EnumType.STRING)
    private ImageState state = ImageState.PENDING;

    public Thumbnail() {}

    public Thumbnail(int imageId, ThumbnailSize size) {
        this.imageId = imageId;
        this.size = size;
        setData(new byte[0]);
    }

    public void setData(byte[] data) {
        this.data = data;
        this.state = ImageState.SUCCESS;
    }

    public void setFailure() {
        this.state = ImageState.FAILURE;
    }
}

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
    private ImageState state;

    public Thumbnail() {}

    public Thumbnail(int imageId, ThumbnailSize size) {
        this.imageId = imageId;
        this.size = size;
        this.data = new byte[0];
        this.state = ImageState.PENDING;
    }

    public void setData(byte[] data) {
        this.data = data;
        this.state = ImageState.SUCCESS;
    }

    @Override
    public String toString() {
        return "Thumbnail{" +
                "id=" + id +
                ", imageId=" + imageId +
                ", size=" + size +
                ", data=" + getPrintingData() +
                ", state=" + state +
                '}';
    }

    private String getPrintingData() {
        if (data == null || data.length == 0) {
            return "[]";
        }
        return "[length=" + data.length + "]";
    }

    public void setFailure() {
        this.state = ImageState.FAILURE;
    }
}

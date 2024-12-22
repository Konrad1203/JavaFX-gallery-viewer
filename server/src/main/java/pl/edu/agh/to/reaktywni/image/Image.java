package pl.edu.agh.to.reaktywni.image;

import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;


@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Transient
    private int gridId;

    @Setter
    @Transient
    @Builder.Default
    private ImageState imageState = ImageState.PENDING;

    @Setter
    private String name;

    @Setter
    private String extensionType;

    @Setter
    private int width;

    @Setter
    private int height;

    @Setter
    @Lob
    private byte[] data;

    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", gridId=" + gridId +
                ", imageState=" + imageState +
                ", name='" + name + '\'' +
                ", extensionType='" + extensionType + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}

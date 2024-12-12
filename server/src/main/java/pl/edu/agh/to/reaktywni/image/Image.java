package pl.edu.agh.to.reaktywni.image;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int databaseId;

    @Transient
    private int gridPlacementId;

    @Setter
    @Transient
    private ImageState imageState;

    private String name;

    private String extensionType;

    @Setter
    private int width;

    @Setter
    private int height;

    @Setter
    @Lob
    private byte[] data;
}

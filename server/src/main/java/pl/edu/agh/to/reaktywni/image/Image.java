package pl.edu.agh.to.reaktywni.image;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int databaseId;

    @Transient
    private int gridPlacementID;

    private String name;

    private String extensionType;

    @Setter
    private int width;

    @Setter
    private int height;

    @Setter
    @Lob
    private byte[] data;


    public Image() {}

    public Image(String name, String extensionType, int width, int height, byte[] data) {
        this.name = name;
        this.extensionType = extensionType;
        this.width = width;
        this.height = height;
        this.data = data;
    }
}

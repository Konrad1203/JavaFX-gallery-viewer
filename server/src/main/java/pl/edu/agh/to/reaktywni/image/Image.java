package pl.edu.agh.to.reaktywni.image;

import jakarta.persistence.*;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;

import java.util.Set;


@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String extensionType;

    private int width;

    private int height;

    @Lob
    private byte[] data;

    @OneToMany(mappedBy = "image", fetch = FetchType.LAZY)
    private Set<Thumbnail> thumbnails;


    public Image() {}

    public Image(String name, String extensionType, int width, int height, byte[] data) {
        this.name = name;
        this.extensionType = extensionType;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public static Image createFromImageDTO(ImageDTO imageDTO) {
        return new Image(imageDTO.getName(), imageDTO.getExtensionType(),
                imageDTO.getWidth(), imageDTO.getHeight(), imageDTO.getData());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExtensionType() {
        return extensionType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getImageData() {
        return data;
    }
}

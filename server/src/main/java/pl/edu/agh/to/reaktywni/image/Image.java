package pl.edu.agh.to.reaktywni.image;

import jakarta.persistence.*;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;

import java.nio.file.Path;
import java.util.Set;


@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String extensionType;

    @OneToMany(mappedBy = "image", fetch = FetchType.LAZY)
    private Set<Thumbnail> thumbnail;


    public Image() {}

    public Image(String name, String extensionType) {
        this.name = name;
        this.extensionType = extensionType;
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
}

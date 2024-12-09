package pl.edu.agh.to.reaktywni.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;

@Builder
@AllArgsConstructor
@Getter
public class ImageDTO {

    private int gridPlacementId;
    @Setter
    private String name;
    private final String extensionType;
    private final int width;
    private final int height;
    private byte[] data;

    public static ImageDTO createFromImage(Image image) {
        return ImageDTO.builder()
                .name(image.getName())
                .extensionType(image.getExtensionType())
                .width(image.getWidth())
                .height(image.getHeight())
                .data(image.getImageData())
                .build();
    }

    public static ImageDTO createFromThumbnail(Thumbnail thumbnail, int width, int height) {
        Image image = thumbnail.getImage();
        return ImageDTO.builder()
                .name(image.getName())
                .extensionType(image.getExtensionType())
                .width(width)
                .height(height)
                .data(thumbnail.getData())
                .build();
    }
}

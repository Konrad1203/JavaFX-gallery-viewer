package pl.edu.agh.to.reaktywni.GUI.util;

import lombok.Getter;

import java.util.Objects;


@Getter
public enum ThumbnailSize {

    SMALL(5, 156, 88),
    MEDIUM(4, 200, 120),
    LARGE(3, 273, 157);

    private final int id = ordinal();
    private final int columnCount;
    private final int imageWidth;
    private final int imageHeight;
    private final javafx.scene.image.Image placeholder;
    private final javafx.scene.image.Image errorImage;

    ThumbnailSize(int columnCount, int imageWidth, int imageHeight) {
        this.columnCount = columnCount;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.placeholder = new javafx.scene.image.Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/GUI/icons/loading.gif")),
                imageWidth, imageHeight, false, true
        );
        this.errorImage = new javafx.scene.image.Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/GUI/icons/error.png")),
                imageWidth, imageHeight, false, true
        );
    }

    private static final ThumbnailSize[] values = ThumbnailSize.values();

    public static ThumbnailSize getFromId(int id) {
        return values[id];
    }
}

package pl.edu.agh.to.reaktywni.thumbnail;

import lombok.Getter;


@Getter
public enum ThumbnailSize {

    SMALL(160, 90),
    MEDIUM(320, 180),
    LARGE(480, 270);

    private final int width;
    private final int height;

    ThumbnailSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}

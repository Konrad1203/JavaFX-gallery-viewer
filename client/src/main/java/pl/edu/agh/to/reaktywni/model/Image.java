package pl.edu.agh.to.reaktywni.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

@Getter
@Builder
public class Image {
    private int databaseId;
    private final int gridPlacementId;
    private ImageState imageState;
    private String name;
    private final String extensionType;
    private final int width;
    private final int height;
    @Setter
    private byte[] data;

    public static Image createFromFile(int gridPlacementId, File file) {
        try {
            String extensionType = getFileExtension(file.getName());
            int[] size = getImageDimensions(file, extensionType);

            return new ImageBuilder()
                    .gridPlacementId(gridPlacementId)
                    .name(file.getName())
                    .extensionType(extensionType)
                    .width(size[0])
                    .height(size[1])
                    .data(Files.readAllBytes(file.toPath()))
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Error reading image file: " + file.getAbsolutePath(), e);
        }
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex <= 0) throw new IllegalArgumentException("No extension for file: " + fileName);
        return fileName.substring(dotIndex + 1);
    }

    private static int[] getImageDimensions(File imgFile, String extensionType) throws IOException {
        Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(extensionType);
        if (!it.hasNext()) throw new IOException("File format not supported: " + imgFile.getAbsolutePath());
        ImageReader reader = it.next();

        try (ImageInputStream stream = new FileImageInputStream(imgFile)) {
            reader.setInput(stream);
            return new int[] {reader.getWidth(reader.getMinIndex()), reader.getHeight(reader.getMinIndex())};
        } catch (IOException e) {
            throw new IOException("Error reading image dimensions for file: " + imgFile.getAbsolutePath(), e);
        } finally {
            reader.dispose();
        }
    }
}


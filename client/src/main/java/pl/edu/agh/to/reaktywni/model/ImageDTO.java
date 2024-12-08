package pl.edu.agh.to.reaktywni.model;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

public class ImageDTO {

    private int gridPlacementId = 0;
    private String name;
    private final String extensionType;
    private final int width;
    private final int height;
    private byte[] data;


    private ImageDTO(String name, String extensionType, int width, int height, byte[] data) {
        this.name = name;
        this.extensionType = extensionType;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public static ImageDTO createFromFile(File file) {
        try {
            String extensionType = getFileExtension(file.getName());
            int[] size = getImageDimensions(file, extensionType);
            return new ImageDTO(file.getName(), extensionType, size[0], size[1], Files.readAllBytes(file.toPath()));
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

    public int getGridPlacementId() {
        return gridPlacementId;
    }

    public void setGridPlacementId(int id) {
        this.gridPlacementId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}


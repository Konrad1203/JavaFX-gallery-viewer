package pl.edu.agh.to.reaktywni.model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

public class ImageDTO {

    private int idOfPutting;
    private String name;
    private String extensionType;
    private int width;
    private int height;
    private byte[] data;

    public ImageDTO() {}

    public ImageDTO(int idOfPutting, File file) throws IOException {
        this.idOfPutting = idOfPutting;
        readImageDetails(file);
    }

    private void readImageDetails(File file) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file);

        if (bufferedImage == null) {
            throw new IOException("Nie udało się wczytać obrazu z pliku: " + file.getPath());
        }

        this.name = file.getName();
        this.extensionType = getFileExtension(file.getName());
        this.width = bufferedImage.getWidth();
        this.height = bufferedImage.getHeight();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, extensionType, baos);
            this.data = baos.toByteArray();
        }
    }

    private String getFileExtension(String fileName) {
        String extension = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex + 1);
        }
        return extension;
    }


    public int getIdOfPutting() {
        return idOfPutting;
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

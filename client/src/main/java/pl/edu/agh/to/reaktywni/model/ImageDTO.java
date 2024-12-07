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

    private int idOfPutting;
    private String name;
    private String extensionType;
    private int width;
    private int height;
    private byte[] data;

    public ImageDTO() {}

    public ImageDTO(int idOfPutting, File file) throws IOException {
        this.idOfPutting = idOfPutting;
        readFileDetails(file);
        setImageDimension(file);
    }

    private void readFileDetails(File file) throws IOException {
        this.name = file.getName();
        this.extensionType = getFileExtension(file.getName());
        this.data = Files.readAllBytes(file.toPath());
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex > 0) ? fileName.substring(dotIndex + 1) : "";
    }

    private void setImageDimension(File imgFile) throws IOException {
        int pos = imgFile.getName().lastIndexOf(".");
        if (pos == -1) {
            throw new IOException("No extension for file: " + imgFile.getAbsolutePath());
        }

        String suffix = imgFile.getName().substring(pos + 1);
        Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);

        if (!iter.hasNext()) {
            throw new IOException("No suitable ImageReader for file: " + imgFile.getAbsolutePath());
        }

        while (iter.hasNext()) {
            ImageReader reader = iter.next();
            try (ImageInputStream stream = new FileImageInputStream(imgFile)) {
                reader.setInput(stream);
                this.width = reader.getWidth(reader.getMinIndex());
                this.height = reader.getHeight(reader.getMinIndex());
                return;
            } catch (IOException e) {
                throw new IOException("Error reading image dimensions for file: " + imgFile.getAbsolutePath(), e);
            } finally {
                reader.dispose();
            }
        }

        throw new IOException("Not a valid image file: " + imgFile.getAbsolutePath());
    }


    public int getIdOfPutting() { return idOfPutting; }
    public String getName() { return name; }
    public String getExtensionType() { return extensionType; }
    public int getWidth(){ return width; }
    public int getHeight(){ return height; }
    public byte[] getImageData() { return data; }
}


package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.model.Image;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static pl.edu.agh.to.reaktywni.util.FilesToImagesConverter.getFileExtension;


public interface ZipDataExtractor {

    List<String> acceptedExtensions = List.of("jpg", "jpeg", "png", "gif");


    record Directory(String name, List<Directory> subdirectories) {

        public void addSubdirectory(String path) {
            for (String directoryName : path.split("/")) {
                if (directoryName.isBlank()) continue;
                Directory newDir = new Directory(directoryName, new ArrayList<>());
                if (!subdirectories.contains(newDir)) {
                    subdirectories.add(newDir);
                }
            }
        }

        @Override
        public String toString() {
            return "{name=" + name + " " + subdirectories +"}";
        }
    }


    record ZipData(Directory directory, List<Image> images) {}


    static ZipData extractZipData(File file) throws IOException {

        Directory root = new Directory("root", new ArrayList<>());
        List<Image> images = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory()) {
                    root.addSubdirectory(name);
                } else {
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        Optional<Image> image = convertToImage(name, inputStream);
                        image.ifPresent(images::add);
                    }
                }
            }
        }
        return new ZipData(root, images);
    }

    private static Optional<Image> convertToImage(String filename, InputStream inputStream) throws IOException {

        String fileExtension = getFileExtension(filename);
        if (!acceptedExtensions.contains(fileExtension))
            return Optional.empty();

        byte[] data = inputStream.readAllBytes();
        int[] dimensions = getImageDimensions(new ByteArrayInputStream(data), fileExtension);

        return Optional.of(Image.builder()
                        .name(getFileName(filename))
                        .extensionType(getFileExtension(filename))
                        //.directory(getDirectoryPath(filename))   coś takiego?
                        .width(dimensions[0])
                        .height(dimensions[1])
                        .data(data)
                        .build());
    }

    private static String getFileName(String path) {
        String[] pathSplit = path.split("/");
        return pathSplit[pathSplit.length - 1];
    }
    
    private static String getDirectoryPath(String path) {
        int lastSlashIndex = path.lastIndexOf('/');
        return path.substring(0, lastSlashIndex + 1);
    }

    private static int[] getImageDimensions(InputStream stream, String extensionType) throws IOException {
        Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(extensionType);
        if (!it.hasNext()) throw new IOException("File format not supported: " + extensionType);
        ImageReader reader = it.next();
        reader.setInput(ImageIO.createImageInputStream(stream));
        int[] dimensions = new int[] {reader.getWidth(reader.getMinIndex()), reader.getHeight(reader.getMinIndex())};
        reader.dispose();
        return dimensions;
    }
}

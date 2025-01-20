package pl.edu.agh.to.reaktywni.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public interface JsonFileReaderWriter {

    Path JSON_FILE_PATH = Paths.get("server/src/main/resources/directory_tree_data.json");

    static String read() throws IOException {
        return new String(Files.readAllBytes(JSON_FILE_PATH));
    }

    static void write(String string) throws IOException {
        Path parentDir = JSON_FILE_PATH.getParent();
        if (parentDir != null && !Files.exists(parentDir)) Files.createDirectories(parentDir);
        Files.write(JSON_FILE_PATH, string.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}

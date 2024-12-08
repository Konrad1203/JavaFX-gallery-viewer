package pl.edu.agh.to.reaktywni.model;

import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.ServerClient;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImagePipeline {
    private final ServerClient serverClient;

    public ImagePipeline(ServerClient serverClient) {
        this.serverClient = serverClient;
    }

    public void sendImagesFromFiles(List<File> files, int positionCounter) {
        if(files == null || files.isEmpty()) return;

        List<ImageDTO> images = convertFilesToImages(files, positionCounter);

        System.out.println("Wyslam obrazy: " + images.size());
        serverClient.sendImages(Flux.fromIterable(images))
                .doOnNext(processed -> {
                    System.out.println("Otrzymano: " + processed.getName());
                    System.out.println("Width: " + processed.getWidth() + "\tHeight: " + processed.getHeight());
                    //System.out.println("Data" + Arrays.toString(processed.getData()));
                })
                .blockLast();
    }

    private List<ImageDTO> convertFilesToImages(List<File> files, int positionCounter) {
        List<ImageDTO> images = new ArrayList<>();
        for (File file : files) {
            try {
                images.add(new ImageDTO(positionCounter++, file));
            } catch (IOException e) {
                System.out.println("Blad przy przetwarzaniu pliku: " + file.getAbsolutePath());
                e.printStackTrace();
            }
        }
        return images;
    }

    public void sendTestImages(List<File> files) {
        List<String> paths = List.of(
                "C:\\Users\\Mateusz\\Desktop\\hotdogi\\1001.png",
                "C:\\Users\\Mateusz\\Desktop\\hotdogi\\1002.png"
        );

        List<ImageDTO> images = new ArrayList<>();

        for (String path : paths) {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Plik nie istnieje: " + file.getAbsolutePath());
                return;
            } else {
                try {
                    images.add(new ImageDTO(1, file));
                } catch (IOException e) {
                    System.out.println("Blad przy przetwarzaniu pliku: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Wyslam obrazy: " + images.size());
        serverClient.sendImages(Flux.fromIterable(images))
                .doOnNext(processed -> System.out.println("Otrzymano: " + processed.getName()))
                .blockLast();
    }
}

package pl.edu.agh.to.reaktywni.model;

import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.ServerClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
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

        System.out.println("Wysylam obrazy: " + images.size());
        serverClient.sendImages(Flux.fromIterable(images))
                .doOnNext(processed -> {
                    System.out.println("Otrzymano: " + processed.getName());
                    System.out.println("Width: " + processed.getWidth() + "\tHeight: " + processed.getHeight());
                })
                .blockLast();
    }

    private List<ImageDTO> convertFilesToImages(List<File> files, int positionCounter) {
        List<ImageDTO> images = new ArrayList<>();
        for (File file : files) {
            images.add(ImageDTO.createFromFile(file));
            positionCounter++;
        }
        return images;
    }

    public Flux<ImageDTO> getThumbnails() {
        return serverClient.getThumbnails();
    }

    public Mono<Long> getImagesCount() {
        return serverClient.getImagesCount();
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
                images.add(ImageDTO.createFromFile(file));
            }
        }

        System.out.println("Wyslam obrazy: " + images.size());
        serverClient.sendImages(Flux.fromIterable(images))
                .doOnNext(processed -> System.out.println("Otrzymano: " + processed.getName()))
                .blockLast();
    }
}

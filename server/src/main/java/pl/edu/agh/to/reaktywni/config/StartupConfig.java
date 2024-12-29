package pl.edu.agh.to.reaktywni.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.edu.agh.to.reaktywni.image.ImageService;

import java.util.logging.Level;
import java.util.logging.Logger;


@Configuration
public class StartupConfig {

    private final static Logger logger = Logger.getLogger(StartupConfig.class.getName());

    @Bean
    public CommandLineRunner run(ImageService imageService) {
        logger.log(Level.INFO, "Looking for thumbnails to reprocess...");

        return args -> {
            imageService.createEmptyThumbnailsIfMissing();
            imageService.reprocessPendingThumbnails();
        };
    }
}
package pl.edu.agh.to.reaktywni;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    private final int maxMemorySizeInMB = 1000;

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().maxInMemorySize(maxMemorySizeInMB * 1024 * 1024);
    }
}
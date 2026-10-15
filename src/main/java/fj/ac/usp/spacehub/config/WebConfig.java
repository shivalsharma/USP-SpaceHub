package fj.ac.usp.spacehub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {

        Path profilePictureDirectory =
                Path.of(
                        "uploads",
                        "profile-pictures"
                )
                .toAbsolutePath()
                .normalize();

        String location =
                profilePictureDirectory
                        .toUri()
                        .toString();

        if (!location.endsWith("/")) {
            location += "/";
        }

        registry
                .addResourceHandler(
                        "/uploads/profile-pictures/**"
                )
                .addResourceLocations(
                        location
                );
    }
}
package fj.ac.usp.spacehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpaceHubApplication {
    public static void main(String[] args) { SpringApplication.run(SpaceHubApplication.class, args); }
}

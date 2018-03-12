package com.iggroup.universityworkshopmw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableScheduling
public class UniversityWorkshopMwApplication {
   public static void main(String[] args) {
      SpringApplication.run(UniversityWorkshopMwApplication.class, args);
   }
}

package org.cg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class GbaasApplication {

  public static void main(String[] args) {
    SpringApplication.run(GbaasApplication.class, args);
  }

}

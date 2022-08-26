package it.gov.pagopa.group;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class GroupApplication {

  public static void main(String[] args) {
    SpringApplication.run(GroupApplication.class, args);
  }

}


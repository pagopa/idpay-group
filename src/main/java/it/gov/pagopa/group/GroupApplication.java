package it.gov.pagopa.group;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = "it.gov.pagopa")
@EnableScheduling
@EnableAsync
public class GroupApplication {

  public static void main(String[] args) {
    SpringApplication.run(GroupApplication.class, args);
  }

}


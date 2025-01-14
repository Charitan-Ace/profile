package com.charitan.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sun.misc.Signal;

@SpringBootApplication
public class ProfileApplication {

  public static void main(String[] args) {

    SpringApplication.run(ProfileApplication.class, args);

    Signal.handle(
        new Signal("TERM"),
        sig -> {
          System.out.println("Intercepted SIGTERM. Ignoring shutdown...");
          System.out.flush();
        });
  }
}

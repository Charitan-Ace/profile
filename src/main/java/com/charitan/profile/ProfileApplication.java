package com.charitan.profile;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProfileApplication {

	public static void main(String[] args) {

		SpringApplication.run(ProfileApplication.class, args);

		Signal.handle(new Signal("TERM"), sig -> {
			System.out.println("Intercepted SIGTERM. Ignoring shutdown...");
			System.out.flush();
		});

	}

}

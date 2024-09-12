package org.example;

import org.example.network.Peer;
import org.example.storage.TransactionStorage;
import org.example.utils.PortUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException {
        int webPort = PortUtils.findAvailablePort();
        System.setProperty("server.port", String.valueOf(webPort));

        SpringApplication.run(Application.class, args);
    }

}

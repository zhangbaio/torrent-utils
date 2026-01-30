package com.example.torrentutils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TorrentUtilsApplication {

    public static void main(String[] args) {

        SpringApplication.run(TorrentUtilsApplication.class, args);
        String baseDir = System.getProperty("user.dir");
        System.out.println("baseDir " + baseDir);
    }

}

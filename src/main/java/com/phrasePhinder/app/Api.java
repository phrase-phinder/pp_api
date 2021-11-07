package com.phrasePhinder.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class Api {
    public static Set<String> trieShows = new HashSet<>(Arrays.asList("The_Office"));
    public static void main(String[] args) {
        for(String show: trieShows)
            Trie.addShow(show);
        SpringApplication.run(Api.class, args);
    }
}

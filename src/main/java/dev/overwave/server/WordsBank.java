package dev.overwave.server;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WordsBank {
    private final Random random;
    private final List<String> words;

    public WordsBank() {
        random = new Random();
        words = readWords("words_list.txt");
    }

    private static List<String> readWords(String path) {
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(path))) {
            String vocabulary = new String(reader.readAllBytes());
            return Arrays.asList(vocabulary.split("\r\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public String getWord() {
        return words.get(random.nextInt(words.size()));
    }
}

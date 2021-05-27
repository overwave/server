package dev.overwave.server;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class WordsBank {
    private final List<String> words;

    public WordsBank() {
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
        return Util.getRandom(words);
    }
}

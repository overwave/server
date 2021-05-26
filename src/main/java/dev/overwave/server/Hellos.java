package dev.overwave.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Hellos {
    private static final String PATH = "hello/hellos.txt";

    private final Set<Integer> hellos;

    public Hellos() {
        hellos = new HashSet<>();

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(PATH))) {
            String leaderboard = new String(inputStream.readAllBytes());
            for (String row : leaderboard.split("\r\n")) {
                hellos.add(Integer.valueOf(row));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hello(int chatId) {
        if (hellos.contains(chatId)) {
            return true;
        }

        hellos.add(chatId);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(PATH))) {
            for (Integer id : hellos) {
                outputStream.write((id + "\r\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

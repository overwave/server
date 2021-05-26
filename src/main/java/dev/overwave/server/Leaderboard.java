package dev.overwave.server;

import org.springframework.beans.factory.support.ManagedMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class Leaderboard {
    private static final String PATH = "-2000000002-leaderboard.txt";

    public Map<Integer, Integer> getBoard(int chatId) {
        Map<Integer, Integer> result = new ManagedMap<>();

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(chatId + PATH))) {
            String leaderboard = new String(inputStream.readAllBytes());
            for (String row : leaderboard.split("\r\n")) {
                String[] tokens = row.split(" ");
                result.put(Integer.valueOf(tokens[0]), Integer.valueOf(tokens[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void incrementUser(int chatId, int userId) {
        Map<Integer, Integer> board = getBoard(chatId);
        int newValue = board.getOrDefault(userId, 0) + 1;
        board.put(userId, newValue);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(chatId + PATH))) {
            for (Map.Entry<Integer, Integer> entry : board.entrySet()) {
                String line = entry.getKey() + " " + entry.getValue() + "\r\n";
                outputStream.write(line.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
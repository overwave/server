package dev.overwave.server;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class Chat {
    private static final String PATH = "state/state-";

    private final int id;
    private final Leaderboard leaderboard;
    private final Structure<String> recentWords;

    private int leaderId;
    private CrokoGame.State state;
    private String word;

    public Chat(int id) {
        this.id = id;

        this.leaderboard = new Leaderboard(id);
        this.recentWords = new Structure<>();

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(PATH + id + ".txt"))) {
            String content = new String(inputStream.readAllBytes());
            String[] lines = content.split("\r\n");

            this.leaderId = Integer.parseInt(lines[0]);
            this.word = lines[1].equals("") ? null : lines[1];
            this.state = CrokoGame.State.valueOf(lines[2]);

            for (int i = 3; i < lines.length; i++) {
                recentWords.add(lines[i]);
            }
            return;
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.leaderId = 0;
        this.word = null;
        this.state = CrokoGame.State.IDLE;
    }

    private void saveState() {
        StringBuilder builder = new StringBuilder();
        builder.append(leaderId).append("\r\n");
        builder.append(word == null ? "" : word).append("\r\n");
        builder.append(state).append("\r\n");
        builder.append(recentWords);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(PATH + id + ".txt"))) {
            outputStream.write(builder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Integer> getLeaderboard() {
        return leaderboard.getBoard();
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
        saveState();
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
        saveState();
    }

    public CrokoGame.State getState() {
        return state;
    }

    public void setState(CrokoGame.State state) {
        this.state = state;
        saveState();
    }

    public void incrementLeader() {
        leaderboard.incrementUser(leaderId);
    }

    public void rememberWord() {
        recentWords.add(word);
        saveState();
    }

    public boolean wasRecently(String word) {
        return !recentWords.contains(word);
    }
}

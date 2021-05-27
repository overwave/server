package dev.overwave.server;

import java.util.Map;

public class Chat {
    private final int id;
    private final Leaderboard leaderboard;
    private final Structure<String> recentWords;

    private int leaderId;
    private CrokoGame.State state;
    private String word;

    public Chat(int id) {
        this.id = id;

        this.leaderId = 0;
        this.word = null;
        this.leaderboard = new Leaderboard(id);
        this.state = CrokoGame.State.IDLE;
        this.recentWords = new Structure<>();
    }

    public Map<Integer, Integer> getLeaderboard() {
        return leaderboard.getBoard();
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public CrokoGame.State getState() {
        return state;
    }

    public void setState(CrokoGame.State state) {
        this.state = state;
    }

    public void incrementLeader() {
        leaderboard.incrementUser(leaderId);
    }

    public void rememberWord() {
        recentWords.add(word);
    }

    public boolean wasRecently(String word) {
        return !recentWords.contains(word);
    }
}

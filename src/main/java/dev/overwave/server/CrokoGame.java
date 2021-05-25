package dev.overwave.server;


public class CrokoGame {

    public void getHelp() {

    }

    public void becomeLeader(int userId) {

    }

    private enum State {
        IDLE,
        IN_GAME
    }

    private State state;

    public void begin() {
        if (state != State.IDLE) {
//            return "Игра уже идёт!";
        }

        state = State.IN_GAME;
//        return "S"
    }
}

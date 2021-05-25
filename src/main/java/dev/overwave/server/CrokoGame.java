package dev.overwave.server;


public class CrokoGame {

    public void getHelp() {

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

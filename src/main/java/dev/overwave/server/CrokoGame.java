package dev.overwave.server;


import api.longpoll.bots.model.objects.additional.Button;
import api.longpoll.bots.model.objects.additional.Keyboard;
import com.vk.api.sdk.objects.users.User;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.overwave.server.ChatBot.BEGIN_ACTION;
import static dev.overwave.server.ChatBot.NEXT_ACTION;

public class CrokoGame {

    private final WordsBank wordsBank;
    private final Leaderboard leaderboard;

    private int leaderId;
    private State state;
    private String word;

    Keyboard beginKeyboard;
    Keyboard nextKeyboard;

    public CrokoGame() {
        this.leaderId = 0;
        this.word = null;
        this.wordsBank = new WordsBank();
        this.leaderboard = new Leaderboard();

        Button.Action action = new Button.CallbackAction("Стать ведущим").setPayload(BEGIN_ACTION);
        Button button = new Button(Button.ButtonColor.SECONDARY, action);
        beginKeyboard = new Keyboard()
                .setButtons(List.of(List.of(button)))
                .setInline(true);

        Button.Action action2 = new Button.CallbackAction("Получить слово").setPayload(NEXT_ACTION);
        Button button2 = new Button(Button.ButtonColor.SECONDARY, action2);
        nextKeyboard = new Keyboard()
                .setButtons(List.of(List.of(button2)))
                .setInline(true);
    }

//    "failed":2 — истекло время действия ключа, нужно заново получить key методом groups.getLongPollServer.

    private String idToFormattedUser(int id, MessagingFacade facade) {
        return userToFormattedUser(facade.userById(id));
    }

    private String userToFormattedUser(User user) {
        return "[id%d|%s %s]".formatted(user.getId(), user.getFirstName(), user.getLastName());
    }

    public void getHelp(MessagingFacade facade) {
    }

    public void becomeLeader(MessagingFacade facade) {
        if (state == State.IDLE || state == null) {
            leaderId = facade.getFrom();
            state = State.STARTING;
            facade.confirmEvent();
            facade.sendMessage(idToFormattedUser(leaderId, facade) + " загадывает слово.", nextKeyboard);
        } else {
            facade.showNotification("Ведущий уже назначен!");
        }
    }

    public void getWord(MessagingFacade facade) {
        if (facade.getFrom() != leaderId) {
            facade.showNotification("Вы не являетесь ведущим!");
            return;
        }

        if (state == State.STARTING || state == State.IN_GAME) {
            state = State.IN_GAME;

            word = wordsBank.getWord();
            facade.showNotification("Объясни слово: " + word);
        } else {
            facade.showNotification("Игра ещё не начата!");
        }
    }

    public void checkAnswer(MessagingFacade facade) {
        if (state == State.IN_GAME && compareWords(facade.getMessage(), word)) {
            int fromId = facade.getFrom();

            if (fromId != leaderId) {
                leaderId = fromId;
                state = State.STARTING;
                leaderboard.incrementUser(facade.getPeerId(), leaderId);

                User user = facade.userById(leaderId);
                String ending = switch (user.getSex()) {
                    case MALE -> "угадал";
                    case FEMALE -> "угадала";
                    default -> "угадало";
                };

                facade.sendMessage("%s %s: %s.".formatted(userToFormattedUser(user), ending, word), nextKeyboard);
                word = null;
            }
        }
    }

    private boolean compareWords(String guess, String word) {
        if (guess == null || word == null) {
            return false;
        }

        guess = guess.toLowerCase(Locale.ROOT).replace('ё', 'е');
        word = word.toLowerCase(Locale.ROOT).replace('ё', 'е');

        return guess.equals(word);
    }

    public void showLeaderboard(MessagingFacade facade) {
        Map<Integer, Integer> board = leaderboard.getBoard(facade.getPeerId());
        if (board.isEmpty()) {
            facade.sendMessage("Топ пока пуст.");
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Топ игроков:\n");

        List<Map.Entry<Integer, Integer>> sortedLeaderboard = board.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByKey().reversed())
                .limit(9)
                .collect(Collectors.toList());

        for (int i = 0; i < sortedLeaderboard.size(); i++) {
            Map.Entry<Integer, Integer> entry = sortedLeaderboard.get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(idToFormattedUser(entry.getKey(), facade))
                    .append(" - ")
                    .append(entry.getValue())
                    .append("\n");
        }

        facade.sendMessage(builder.toString());
    }

    private enum State {
        IDLE,
        STARTING,
        IN_GAME
    }

    public void begin(MessagingFacade facade) {
        state = State.IDLE;
        facade.sendMessage("Игра запущена", beginKeyboard);
    }
}

package dev.overwave.server;


import api.longpoll.bots.model.objects.additional.Button;
import api.longpoll.bots.model.objects.additional.Keyboard;
import com.vk.api.sdk.objects.users.User;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.overwave.server.ChatBot.BEGIN_ACTION;
import static dev.overwave.server.ChatBot.NEXT_ACTION;
import static dev.overwave.server.ChatBot.PEEK_ACTION;
import static dev.overwave.server.ChatBot.SKIP_ACTION;

public class CrokoGame {

    private final WordsBank wordsBank;
    private final Map<Integer, Chat> chats;

    Keyboard beginKeyboard;
    Keyboard nextKeyboard;
    Keyboard leaderKeyboard;

    public CrokoGame() {
        this.wordsBank = new WordsBank();

        Button.Action beginAction = new Button.CallbackAction("Стать ведущим").setPayload(BEGIN_ACTION);
        Button beginButton = new Button(Button.ButtonColor.SECONDARY, beginAction);
        beginKeyboard = new Keyboard()
                .setButtons(List.of(List.of(beginButton)))
                .setInline(true);

        Button.Action getWordAction = new Button.CallbackAction("Получить слово").setPayload(NEXT_ACTION);
        Button getWordButton = new Button(Button.ButtonColor.SECONDARY, getWordAction);
        Button.Action skipAction = new Button.CallbackAction("Пропустить ход").setPayload(SKIP_ACTION);
        Button skipButton = new Button(Button.ButtonColor.SECONDARY, skipAction);
        nextKeyboard = new Keyboard()
                .setButtons(List.of(List.of(getWordButton), List.of(skipButton)))
                .setInline(true);

        Button.Action getNewWordAction = new Button.CallbackAction("Получить новое слово").setPayload(NEXT_ACTION);
        Button getNewWordButton = new Button(Button.ButtonColor.SECONDARY, getNewWordAction);
        Button.Action peekWordAction = new Button.CallbackAction("Показать слово").setPayload(PEEK_ACTION);
        Button peekWordButton = new Button(Button.ButtonColor.SECONDARY, peekWordAction);

        leaderKeyboard = new Keyboard()
                .setButtons(List.of(List.of(getNewWordButton), List.of(peekWordButton), List.of(skipButton)))
                .setInline(true);

        chats = new HashMap<>();
    }

//    "failed":2 — истекло время действия ключа, нужно заново получить key методом groups.getLongPollServer.

    private String idToFormattedUser(int id, MessagingFacade facade) {
        return userToFormattedUser(facade.userById(id));
    }

    private String userToFormattedUser(User user) {
        return "[id%d|%s %s]".formatted(user.getId(), user.getFirstName(), user.getLastName());
    }

    public void getHelp(MessagingFacade facade) {
        facade.sendMessage("""
                Список команд:
                "крок начать" или "крок игра" - начать игру
                "крок топ" - таблица лидеров
                "крок команды" - список команд
                "крок ведущий" - меню ведущего
                """);
    }

    public void becomeLeader(MessagingFacade facade) {
        Chat chat = getChat(facade.getPeerId());

        if (chat.getState() == State.IDLE) {
            chat.setLeaderId(facade.getFrom());
            chat.setState(State.STARTING);
            facade.confirmEvent();
            facade.sendMessage(idToFormattedUser(chat.getLeaderId(), facade) + " объясняет слово.", nextKeyboard);
        } else {
            String name = facade.userById(chat.getLeaderId()).getFirstName();
            facade.showNotification(name + " сейчас ведущий");
        }
    }

    private Chat getChat(int chatId) {
        return chats.computeIfAbsent(chatId, Chat::new);
    }

    public void getWord(MessagingFacade facade) {
        Chat chat = getChat(facade.getPeerId());

        if (facade.getFrom() != chat.getLeaderId()) {
            facade.showNotification("Вы не являетесь ведущим!");
            return;
        }

        if (chat.getState() == State.STARTING || chat.getState() == State.IN_GAME) {
            chat.setState(State.IN_GAME);

            chat.setWord(wordsBank.getWord());
            facade.showNotification("Объясни слово: " + chat.getWord());
        } else {
            facade.showNotification("Ведущий ещё не выбран!");
        }
    }

    public void skipTurn(MessagingFacade facade) {
        Chat chat = getChat(facade.getPeerId());

        if (facade.getFrom() != chat.getLeaderId()) {
            facade.showNotification("Вы не являетесь ведущим!");
            return;
        }

        User user = facade.userById(chat.getLeaderId());
        chat.setState(State.IDLE);
        chat.setWord(null);
        chat.setLeaderId(0);

        facade.confirmEvent();
        facade.sendMessage("%s %s место ведущего.".formatted(
                idToFormattedUser(facade.getFrom(), facade),
                getVerb(user, Verb.YIELDED)
        ), beginKeyboard);
    }

    public void checkAnswer(MessagingFacade facade) {
        Chat chat = getChat(facade.getPeerId());

        if (chat.getState() == State.IN_GAME && compareWords(facade.getMessage(), chat.getWord())) {
            int fromId = facade.getFrom();

            if (fromId != chat.getLeaderId()) {
                chat.setLeaderId(fromId);
                chat.setState(State.STARTING);
                chat.incrementLeader();

                User user = facade.userById(chat.getLeaderId());

                facade.sendMessage("%s %s: %s.".formatted(
                        userToFormattedUser(user),
                        getVerb(user, Verb.DECIDED),
                        chat.getWord()
                ), nextKeyboard);
                chat.setWord(null);
            }
        }
    }

    private String getVerb(User user, Verb verb) {
        if (verb == Verb.DECIDED) {
            return switch (user.getSex()) {
                case MALE -> "угадал";
                case FEMALE -> "угадала";
                default -> "угадало";
            };
        } else if (verb == Verb.YIELDED) {
            return switch (user.getSex()) {
                case MALE -> "уступил";
                case FEMALE -> "уступила";
                default -> "уступило";
            };
        } else {
            return null;
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
        Chat chat = getChat(facade.getPeerId());

        Map<Integer, Integer> board = chat.getLeaderboard();
        if (board.isEmpty()) {
            facade.sendMessage("Топ пока пуст.");
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Топ игроков:\n");

        List<Map.Entry<Integer, Integer>> sortedLeaderboard = board.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
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

    public void showLeaderMenu(MessagingFacade facade) {
        Chat chat = getChat(facade.getPeerId());

        if (chat.getState() == State.IDLE) {
            facade.sendMessage("Ведущий ещё не назначен", beginKeyboard);
        } else if (chat.getState() == State.STARTING) {
            facade.sendMessage(idToFormattedUser(chat.getLeaderId(), facade) + " - ведущий, слово ещё не выбрано.", beginKeyboard);
        } else if (chat.getState() == State.IN_GAME) {
            facade.sendMessage(idToFormattedUser(chat.getLeaderId(), facade) + " - ведущий.", leaderKeyboard);
        }

    }

    private enum Verb {
        DECIDED,
        YIELDED,
    }

    public enum State {
        IDLE,
        STARTING,
        IN_GAME
    }

    public void begin(MessagingFacade facade) {
        getChat(facade.getPeerId()).setState(State.IDLE);
        facade.sendMessage("Игра запущена", beginKeyboard);
    }
}

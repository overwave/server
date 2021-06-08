package dev.overwave.server;

import api.longpoll.bots.exceptions.BotsLongPollAPIException;
import api.longpoll.bots.exceptions.BotsLongPollException;
import api.longpoll.bots.methods.messages.MessagesSend;
import api.longpoll.bots.methods.messages.MessagesSendEventAnswer;
import api.longpoll.bots.model.events.messages.MessageEvent;
import api.longpoll.bots.model.events.messages.MessageNewEvent;
import api.longpoll.bots.model.objects.additional.Button;
import api.longpoll.bots.model.objects.additional.Keyboard;
import com.vk.api.sdk.objects.users.User;

import java.util.function.Function;

public class MessagingFacade {
    private final String accessToken;
    private final MessageEvent messageEvent;
    private final MessageNewEvent messageNewEvent;
    private final Function<Integer, User> userApi;

    private MessagingFacade(String accessToken, Function<Integer, User> userApi, MessageEvent messageEvent) {
        this.accessToken = accessToken;
        this.userApi = userApi;
        this.messageEvent = messageEvent;
        this.messageNewEvent = null;
    }

    private MessagingFacade(String accessToken, Function<Integer, User> userApi, MessageNewEvent messageNewEvent) {
        this.accessToken = accessToken;
        this.userApi = userApi;
        this.messageEvent = null;
        this.messageNewEvent = messageNewEvent;
    }

    public void showNotification(String text) {
        if (messageEvent == null) {
            throw new IllegalStateException("Not message event");
        }
        if (text.length() > 90) {
            throw new IllegalArgumentException("Max text length is 90");
        }

        try {
            new MessagesSendEventAnswer(accessToken)
                    .setEventId(messageEvent.getEventId())
                    .setPeerId(messageEvent.getPeerId())
                    .setUserId(messageEvent.getUserId())
                    .setEventData(new Button.ShowSnackbar(text))
                    .execute();
        } catch (BotsLongPollException e) {
            e.printStackTrace();
        }
    }

    public int getFrom() {
        if (messageNewEvent != null) {
            return messageNewEvent.getMessage().getFromId();
        }
        if (messageEvent != null) {
            return messageEvent.getUserId();
        }
        throw new IllegalStateException("Not new message/message event");
    }

    public void sendMessage(String message) {
        try {
            new MessagesSend(accessToken)
                    .setPeerId(getPeerId())
                    .setMessage(message)
                    .setDisableMentions(true)
                    .execute();
        } catch (BotsLongPollException e) {
            e.printStackTrace();
        }
    }

    public void sendSticker( int stickerId) {
        try {
            new MessagesSend(accessToken)
                    .setPeerId(getPeerId())
                    .setDisableMentions(true)
                    .setStickerId(stickerId)
                    .execute();
        } catch (BotsLongPollException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message, Keyboard keyboard) {
        try {
            new MessagesSend(accessToken)
                    .setKeyboard(keyboard)
                    .setPeerId(getPeerId())
                    .setMessage(message)
                    .setDisableMentions(true)
                    .execute();
        } catch (BotsLongPollException e) {
            e.printStackTrace();
        }
    }

    public void confirmEvent() {
        if (messageEvent == null) {
            throw new IllegalStateException("Not message event");
        }
        try {
            new MessagesSendEventAnswer(accessToken)
                    .setEventId(messageEvent.getEventId())
                    .setPeerId(messageEvent.getPeerId())
                    .setUserId(messageEvent.getUserId())
                    .execute();
        } catch (BotsLongPollException e) {
            e.printStackTrace();
        }
    }

    public int getPeerId() {
        if (messageNewEvent != null) {
            return messageNewEvent.getMessage().getPeerId();
        }
        if (messageEvent != null) {
            return messageEvent.getPeerId();
        }
        throw new IllegalStateException("Not new message/message event");
    }

    public String getMessage() {
        if (messageNewEvent != null) {
            return messageNewEvent.getMessage().getText();
        }
        throw new IllegalStateException("Not new message event");
    }

    public User userById(int id) {
        return userApi.apply(id);
    }

    public static class MessagingFacadeBuilder {
        private final String accessToken;
        private final Function<Integer, User> userApi;

        public MessagingFacadeBuilder(String accessToken, Function<Integer, User> userApi) {
            this.accessToken = accessToken;
            this.userApi = userApi;
        }

        public MessagingFacade of(MessageEvent messageEvent) {
            return new MessagingFacade(accessToken, userApi, messageEvent);
        }

        public MessagingFacade of(MessageNewEvent messageNewEvent) {
            return new MessagingFacade(accessToken, userApi, messageNewEvent);
        }
    }
}

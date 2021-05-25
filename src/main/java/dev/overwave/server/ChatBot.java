package dev.overwave.server;

import api.longpoll.bots.BotsLongPoll;
import api.longpoll.bots.LongPollBot;
import api.longpoll.bots.exceptions.BotsLongPollAPIException;
import api.longpoll.bots.exceptions.BotsLongPollException;
import api.longpoll.bots.methods.messages.MessagesSend;
import api.longpoll.bots.methods.messages.MessagesSendEventAnswer;
import api.longpoll.bots.model.events.messages.MessageEvent;
import api.longpoll.bots.model.events.messages.MessageNewEvent;
import api.longpoll.bots.model.objects.additional.Button;
import api.longpoll.bots.model.objects.additional.Keyboard;
import api.longpoll.bots.model.objects.basic.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;


@Component
public class ChatBot extends LongPollBot implements ApplicationRunner {

    private static final String HELP_COMMAND = "крок команды";
    private static final String BEGIN_COMMAND = "крок начать";

    private static final String BEGIN_ACTION = "{\"action\":\"begin\"}";

    @Value("${groupId}")
    int groupId;
    @Value("${accessToken}")
    String accessToken;

    private final CrokoGame crokoGame;
    Keyboard keyboard;

    public ChatBot() {
        crokoGame = new CrokoGame();

        Button.Action action = new Button.CallbackAction("Стать ведущим").setPayload(BEGIN_ACTION);
        Button button = new Button(Button.ButtonColor.SECONDARY, action);

        keyboard = new Keyboard()
                .setButtons(List.of(List.of(button)))
                .setInline(true);
    }

    @Override
    public void onMessageEvent(MessageEvent messageEvent) {
        String action = (String) messageEvent.getPayload().get("action");
        if ("begin".equals(action)) {
            crokoGame.becomeLeader(messageEvent.getUserId());
        }
        try {
            new MessagesSendEventAnswer(accessToken)
                    .setEventId(messageEvent.getEventId())
                    .setPeerId(messageEvent.getPeerId())
                    .setUserId(messageEvent.getUserId())
                    .setEventData(new Button.ShowSnackbar("{\"button\":\"ведущий2\"}"))
                    .execute();
        } catch (BotsLongPollAPIException | BotsLongPollException e) {
            e.printStackTrace();
        }
        System.out.println(messageEvent.getPayload());
    }

    @Override
    public void onMessageNew(MessageNewEvent messageNewEvent) {
        Message message = messageNewEvent.getMessage();
        try {
//            message.getAttachments().get(0).getType()
            if (message.hasText()) {
                String lowerCaseMessage = message.getText().toLowerCase(Locale.ROOT);
                if (BEGIN_COMMAND.equals(lowerCaseMessage)) {
                    crokoGame.begin();
                } else if (HELP_COMMAND.equals(lowerCaseMessage)) {
                    crokoGame.getHelp();
                }


                new MessagesSend(getAccessToken())
                        .setKeyboard(keyboard)
                        .setPeerId(message.getPeerId())
//                        .setReplyTo(message.getFromId())
//                        .setUserId(messageNewEvent.getMessage().getFromId())
                        .setMessage("response")
                        .execute();
//            messageNewEvent.s

//                String response = "Hello! Received your message: " + message.getText();
//                new MessagesSend(getAccessToken())
//                        .setPeerId(message.getPeerId())
//                        .setMessage(response)
//                        .execute();
            }
        } catch (BotsLongPollAPIException | BotsLongPollException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public int getGroupId() {
        return groupId;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        new BotsLongPoll(this).run();
    }
}
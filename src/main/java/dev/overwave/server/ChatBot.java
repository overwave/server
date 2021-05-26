package dev.overwave.server;

import api.longpoll.bots.BotsLongPoll;
import api.longpoll.bots.LongPollBot;
import api.longpoll.bots.model.events.messages.MessageEvent;
import api.longpoll.bots.model.events.messages.MessageNewEvent;
import api.longpoll.bots.model.objects.basic.Message;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.User;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import dev.overwave.server.MessagingFacade.MessagingFacadeBuilder;
import javax.annotation.PostConstruct;
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

    public static final String BEGIN_ACTION = "{\"action\":\"begin\"}";
    public static final String NEXT_ACTION = "{\"action\":\"next\"}";

    @Value("${groupId}")
    int groupId;
    @Value("${accessToken}")
    String accessToken;

    private final VkApiClient vk;
    private GroupActor groupActor;

    private final CrokoGame crokoGame;
    private MessagingFacadeBuilder facadeFactory;

    public ChatBot() {
        crokoGame = new CrokoGame();

        TransportClient transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);
    }

    private User getUserById(int id) {
        try {
            List<GetResponse> responses = vk.users()
                    .get(groupActor)
                    .userIds(String.valueOf(id))
                    .fields(Fields.SEX)
                    .execute();

            return responses.get(0);
        } catch (ApiException | ClientException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void postConstruct() {
         groupActor = new GroupActor(groupId, accessToken);

        facadeFactory = new MessagingFacade.MessagingFacadeBuilder(accessToken, this::getUserById);
    }

    @Override
    public void onMessageEvent(MessageEvent messageEvent) {
        String action = (String) messageEvent.getPayload().get("action");
        if ("begin".equals(action)) {
            crokoGame.becomeLeader(facadeFactory.of(messageEvent));
        } else if ("next".equals(action)) {
            crokoGame.getWord(facadeFactory.of(messageEvent));
        }
    }

    @Override
    public void onMessageNew(MessageNewEvent messageNewEvent) {
        crokoGame.checkAnswer(facadeFactory.of(messageNewEvent));

        Message message = messageNewEvent.getMessage();
        if (message.hasText()) {
            String lowerCaseMessage = message.getText().toLowerCase(Locale.ROOT);
            if (BEGIN_COMMAND.equals(lowerCaseMessage)) {
                crokoGame.begin(facadeFactory.of(messageNewEvent));
            } else if (HELP_COMMAND.equals(lowerCaseMessage)) {
                crokoGame.getHelp(facadeFactory.of(messageNewEvent));
            }
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
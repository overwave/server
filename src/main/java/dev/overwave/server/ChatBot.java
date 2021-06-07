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
    private static final String HELP_COMMAND_2 = "крок ком";
    private static final String BEGIN_COMMAND = "крок начать";
    private static final String PLAY_COMMAND = "крок игра";
    private static final String PLAY_COMMAND_2 = "крок играть";
    private static final String LEADERBOARD_COMMAND = "крок топ";
    private static final String LEADER_COMMAND = "крок ведущий";
    private static final String LEADER_COMMAND_2 = "крок вед";
    private static final String TEST_COMMAND = "крок тест";

    public static final String BEGIN_ACTION = "{\"action\":\"begin\"}";
    public static final String NEXT_ACTION = "{\"action\":\"next\"}";
    public static final String PREVIOUS_ACTION = "{\"action\":\"previous\"}";
    public static final String SKIP_ACTION = "{\"action\":\"skip\"}";
    public static final String PEEK_ACTION = "{\"action\":\"peek\"}";

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
        } else if ("previous".equals(action)) {
            crokoGame.getPreviousWord(facadeFactory.of(messageEvent));
        } else if ("skip".equals(action)) {
            crokoGame.skipTurn(facadeFactory.of(messageEvent));
        } else if ("peek".equals(action)) {
            crokoGame.peekWord(facadeFactory.of(messageEvent));
        }
    }

    @Override
    public void onMessageNew(MessageNewEvent messageNewEvent) {
        crokoGame.checkAnswer(facadeFactory.of(messageNewEvent));

        Message message = messageNewEvent.getMessage();
        if (message.hasText()) {
            String lowerCaseMessage = message.getText().toLowerCase(Locale.ROOT);
            switch (lowerCaseMessage) {
                case BEGIN_COMMAND, PLAY_COMMAND, PLAY_COMMAND_2 -> crokoGame.begin(facadeFactory.of(messageNewEvent));
                case HELP_COMMAND, HELP_COMMAND_2 -> crokoGame.getHelp(facadeFactory.of(messageNewEvent));
                case LEADERBOARD_COMMAND -> crokoGame.showLeaderboard(facadeFactory.of(messageNewEvent));
                case LEADER_COMMAND, LEADER_COMMAND_2 -> crokoGame.showLeaderMenu(facadeFactory.of(messageNewEvent));
                case TEST_COMMAND -> crokoGame.test(facadeFactory.of(messageNewEvent));
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
    public void run(ApplicationArguments args) {
        while (true) {
            try {
                new BotsLongPoll(this).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //java.lang.IllegalStateException: Failed to execute ApplicationRunner
    //	at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:789) ~[spring-boot-2.5.1-20210524.103327-4.jar:2.5.1-SNAPSHOT]
    //	at org.springframework.boot.SpringApplication.callRunners(SpringApplication.java:776) ~[spring-boot-2.5.1-20210524.103327-4.jar:2.5.1-SNAPSHOT]
    //	at org.springframework.boot.SpringApplication.run(SpringApplication.java:344) ~[spring-boot-2.5.1-20210524.103327-4.jar:2.5.1-SNAPSHOT]
    //	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1336) ~[spring-boot-2.5.1-20210524.103327-4.jar:2.5.1-SNAPSHOT]
    //	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1325) ~[spring-boot-2.5.1-20210524.103327-4.jar:2.5.1-SNAPSHOT]
    //	at dev.overwave.server.ServerApplication.main(ServerApplication.java:10) ~[classes/:na]
    //Caused by: java.lang.NullPointerException: Cannot invoke "api.longpoll.bots.model.objects.media.AttachmentType.ordinal()" because "attachmentType" is null
    //	at api.longpoll.bots.adapters.deserializers.AttachmentDeserializer.getType(AttachmentDeserializer.java:50) ~[java-vk-bots-longpoll-api-1.5.2.jar:na]
    //	at api.longpoll.bots.adapters.deserializers.AttachmentDeserializer.deserialize(AttachmentDeserializer.java:43) ~[java-vk-bots-longpoll-api-1.5.2.jar:na]
    //	at api.longpoll.bots.adapters.deserializers.AttachmentDeserializer.deserialize(AttachmentDeserializer.java:28) ~[java-vk-bots-longpoll-api-1.5.2.jar:na]
    //	at com.google.gson.internal.bind.TreeTypeAdapter.read(TreeTypeAdapter.java:69) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.TypeAdapter$1.read(TypeAdapter.java:199) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper.read(TypeAdapterRuntimeTypeWrapper.java:41) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.CollectionTypeAdapterFactory$Adapter.read(CollectionTypeAdapterFactory.java:82) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.CollectionTypeAdapterFactory$Adapter.read(CollectionTypeAdapterFactory.java:61) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$1.read(ReflectiveTypeAdapterFactory.java:131) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$Adapter.read(ReflectiveTypeAdapterFactory.java:222) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$1.read(ReflectiveTypeAdapterFactory.java:131) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$Adapter.read(ReflectiveTypeAdapterFactory.java:222) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.Gson.fromJson(Gson.java:932) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.Gson.fromJson(Gson.java:1003) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.TreeTypeAdapter$GsonContextImpl.deserialize(TreeTypeAdapter.java:162) ~[gson-2.8.6.jar:na]
    //	at api.longpoll.bots.adapters.deserializers.EventDeserializer.deserialize(EventDeserializer.java:58) ~[java-vk-bots-longpoll-api-1.5.2.jar:na]
    //	at api.longpoll.bots.adapters.deserializers.EventDeserializer.deserialize(EventDeserializer.java:46) ~[java-vk-bots-longpoll-api-1.5.2.jar:na]
    //	at com.google.gson.internal.bind.TreeTypeAdapter.read(TreeTypeAdapter.java:69) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.TypeAdapter$1.read(TypeAdapter.java:199) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper.read(TypeAdapterRuntimeTypeWrapper.java:41) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.CollectionTypeAdapterFactory$Adapter.read(CollectionTypeAdapterFactory.java:82) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.CollectionTypeAdapterFactory$Adapter.read(CollectionTypeAdapterFactory.java:61) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$1.read(ReflectiveTypeAdapterFactory.java:131) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.internal.bind.ReflectiveTypeAdapterFactory$Adapter.read(ReflectiveTypeAdapterFactory.java:222) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.Gson.fromJson(Gson.java:932) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.Gson.fromJson(Gson.java:1003) ~[gson-2.8.6.jar:na]
    //	at com.google.gson.Gson.fromJson(Gson.java:975) ~[gson-2.8.6.jar:na]
    //	at api.longpoll.bots.methods.VkApiMethod.execute(VkApiMethod.java:79) ~[java-vk-bots-longpoll-api-1.5.2.jar:na]
    //	at api.longpoll.bots.methods.VkApiMethod.execute(VkApiMethod.java:61) ~[java-vk-bots-longpoll-api-1.5.2.jar:na]
    //	at api.longpoll.bots.server.LongPollClient.getUpdates(LongPollClient.java:36) ~[java-vk-bots-longpoll-api-1.5.2.jar:na]
    //	at api.longpoll.bots.server.InitializedLongPollClient.getUpdates(InitializedLongPollClient.java:23) ~[java-vk-bots-longpoll-api-1.5.2.jar:na]
    //	at api.longpoll.bots.BotsLongPoll.run(BotsLongPoll.java:26) ~[java-vk-bots-longpoll-api-1.5.2.jar:na]
    //	at dev.overwave.server.ChatBot.run(ChatBot.java:131) ~[classes/:na]
    //	at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:786) ~[spring-boot-2.5.1-20210524.103327-4.jar:2.5.1-SNAPSHOT]
    //	... 5 common frames omitted
}
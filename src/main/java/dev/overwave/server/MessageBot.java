package dev.overwave.server;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Chat;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MessageBot implements ApplicationRunner {

    @Value("${groupId}")
    int groupId;
    @Value("${accessToken}")
    String accessToken;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        GroupActor groupActor = new GroupActor(groupId, accessToken);


        int ts = vk.messages().getLongPollServer(groupActor).execute().getTs();

        while (true) {
            MessagesGetLongPollHistoryQuery historyQuery = vk.messages().getLongPollHistory(groupActor).ts(ts);
            List<Message> messages = historyQuery.execute().getMessages().getItems();

            for (Message message : messages) {
                System.out.println(message);

//                vk.messages().send(groupActor).message("" + message.getText())
                vk.messages().send(groupActor).message("?")
                        .userId(message.getFromId())
                        .randomId(random.nextInt())
                        .execute();

            }
            ts = vk.messages().getLongPollServer(groupActor).execute().getTs();
            Thread.sleep(100000);
        }
    }
}
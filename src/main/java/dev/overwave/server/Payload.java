package dev.overwave.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Payload {
    private final String action;

    public Payload(String payload) {
        this.action = payload;
    }

    public String getAction() {
        return action;
    }

    public JsonObject getAsObject() {
        JsonObject object = new JsonObject();
        object.add("action", new JsonPrimitive(action));
        return object;
    }
}

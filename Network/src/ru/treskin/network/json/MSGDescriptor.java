package ru.treskin.network.json;

import com.google.gson.*;

import java.util.List;

public class MSGDescriptor {
    private final Gson gson;
    public MSGDescriptor() {
        gson = new GsonBuilder().create();
    }
    public String toJson(int type, String name, String msg) {
        return gson.toJson(new Message(type, name, null, msg));
    }
    public String toJson(int type, String name, String to, String msg) {
        return gson.toJson(new Message(type, name, to, msg));
    }
    public String toJson(int type, String name, List<String> names) {
        return gson.toJson(new Message(type, name, names));
    }
    public Message fromJson(String msg) {
        return gson.fromJson(msg, Message.class);
    }
    public List<String> names(String msg) {
        return gson.fromJson(msg, Message.class).getUsers();
    }
}
package ru.treskin.network.json;

import java.util.List;

public class Message {
    private final int idType;
    private final String userName;
    private String userTo;
    private String msg;
    private List<String> usernames;

    public Message(int type, String name, String to, String msg) {
        idType = type;
        userName = name;
        this.msg = msg;
        this.userTo = to;
    }

    public Message(int type, String name, List<String> names) {
        idType = type;
        userName = name;
        this.usernames = names;
    }
    @Override
    public String toString() {
        return userName + (userTo == null || userTo.equals("All") ? "" : " (private) ") + ": " + msg;
    }
    public String getName() {
        return userName;
    }
    public String getMessage() {
        return msg;
    }
    public int getType() {
        return idType;
    }
    public List<String> getUsers() {
        return usernames;
    }
    public String getUserToReceive() {
        return userTo;
    }
}

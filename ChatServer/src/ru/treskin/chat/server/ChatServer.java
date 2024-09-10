package ru.treskin.chat.server;

import ru.treskin.chat.server.filter.MSGFilter;
import ru.treskin.network.TCPConnection;
import ru.treskin.network.TCPConnectionListener;
import ru.treskin.network.json.MSGDescriptor;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer implements TCPConnectionListener {
    private final Map<TCPConnection, Integer> connectionsRate;
    private final MSGFilter filter;
    private final MSGDescriptor serializer;
    private final static String SERVER_NAME = "SERVER";
    public final static int LIMIT_OF_BAD_MSG = 3;
    public final Map<String, TCPConnection> usernamesToTcp;
    public final Map<TCPConnection, String> tcpToUsernames;
    public static void main(String[] args) {
        new ChatServer();
    }
    private ChatServer() {
        serializer = new MSGDescriptor();
        connectionsRate = new HashMap<>();
        usernamesToTcp = new HashMap<>();
        tcpToUsernames = new HashMap<>();
        filter = new MSGFilter();
        System.out.println("Server is running");
        try {
            ServerSocket serverSocket = new ServerSocket(8189);
            while (true) {
                new TCPConnection(serverSocket.accept(), this);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> checkMessage(String value) {
        String[] words = value.split("[ ,]");
        List<String> currentBadWords = new ArrayList<>();
        for (String s : words) {
            int index = filter.checkWord(s);
            if (index > 0) currentBadWords.add(filter.wordAt(index));
        }
        return currentBadWords;
    }
    private boolean isNameCorrect(String name) {
        //checks that new user have unique nickname and nickname consist of letters only
        return !usernamesToTcp.containsKey(name) && name.matches("\\w+");
    }
    @Override
    public synchronized void onConnectionReady(TCPConnection connection) {
        connection.sendString(serializer.toJson(0, SERVER_NAME, usernamesToTcp.keySet()
                                                                        .stream()
                                                                        .toList()));
    }

    private boolean registerUser(TCPConnection connection, String msg) {
        int type = serializer.fromJson(msg).getType();
        String name = serializer.fromJson(msg).getName();
        if (type == 2) {
            if (isNameCorrect(name)) {
                usernamesToTcp.put(name, connection);
                tcpToUsernames.put(connection, name);
            } else {
                onDisconnect(connection);
                return false;
            }
        }
        return true;
    }
    private void changeUserBadWordsLimit(TCPConnection connection, List<String> badWords) {
        int curRate = connectionsRate.getOrDefault(connection, 0) + 1;
        connection.sendString(serializer
                .toJson(1, SERVER_NAME, "You used this bad words: " + badWords + "\nYou shouldn't do this more"));
        if (curRate > LIMIT_OF_BAD_MSG) {
            connection.sendString(serializer
                    .toJson(1, SERVER_NAME, "You sent to much bad messages! Godbye:)"));
            onDisconnect(connection);
            connection.closeConnection();
        } else connectionsRate.put(connection, curRate);
    }
    @Override
    public synchronized void onReceiveString(TCPConnection connection, String value) {
        String msg = serializer.fromJson(value).getMessage();
        List<String> badWords = checkMessage(msg);
        boolean canSend = true;
        if (badWords.isEmpty()) {
            canSend = registerUser(connection, value);
        } else {
            canSend = false;
            changeUserBadWordsLimit(connection, badWords);
        }
        if (canSend) {
            String userToSend = serializer.fromJson(value).getUserToReceive();
            if (userToSend == null || userToSend.equals("All")) sendToAll(value);
            else if (usernamesToTcp.containsKey(userToSend)) {
                usernamesToTcp.get(userToSend).sendString(value);
                connection.sendString(value);
            }
        }
    }

    @Override
    public synchronized void onDisconnect(TCPConnection connection) {
        connectionsRate.remove(connection);
        connection.closeConnection();
        usernamesToTcp.remove(tcpToUsernames.get(connection));
        sendToAll(serializer
                .toJson(3, SERVER_NAME, tcpToUsernames.get(connection)));
        tcpToUsernames.remove(connection);
    }

    @Override
    public synchronized void onException(TCPConnection connection, Exception e) {
        onDisconnect(connection);
        System.out.println("TCP exception: " + e);
    }
    private void sendToAll(String msg) {
        for (TCPConnection connection : tcpToUsernames.keySet()) connection.sendString(msg);
    }
}
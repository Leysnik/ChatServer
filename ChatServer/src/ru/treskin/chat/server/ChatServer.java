package ru.treskin.chat.server;

import ru.treskin.chat.server.filter.MSGFilter;
import ru.treskin.network.TCPConnection;
import ru.treskin.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer implements TCPConnectionListener {
    private final List<TCPConnection> connections;
    private final Map<TCPConnection, Integer> connectionsRate;
    private final MSGFilter filter;
    public final static int LIMIT_OF_BAD_MSG = 3;
    public static void main(String[] args) {
        new ChatServer();
    }
    private ChatServer() {
        connections = new ArrayList<>();
        connectionsRate = new HashMap<>();
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

    @Override
    public synchronized void onConnectionReady(TCPConnection connection) {
        connections.add(connection);
        sendToAll("New connection: " + connection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection connection, String value) {
        String[] words = value.split("[ ,]");
        List<String> currentBadWords = new ArrayList<>();
        for (String s : words) {
            int index = filter.checkWord(s);
            if (index > 0) currentBadWords.add(filter.wordAt(index));
        }

        if (currentBadWords.isEmpty()) sendToAll(value);
        else {
            int curRate = connectionsRate.getOrDefault(connection, 0) + 1;
            connection.sendString("SERVER : You used this bad words: " + currentBadWords + "\n You shouldn't do this more");
            if (curRate > LIMIT_OF_BAD_MSG) {
                connection.sendString("SERVER : You sent to much bad messages! Godbye:)");
                onDisconnect(connection);
                connection.closeConnection();
            } else connectionsRate.put(connection, curRate);

        }
    }

    @Override
    public synchronized void onDisconnect(TCPConnection connection) {
        connections.remove(connection);
        connection.closeConnection();
        sendToAll("Disconnected: " + connection);
    }

    @Override
    public synchronized void onException(TCPConnection connection, Exception e) {
        System.out.println("TCP exception: " + e);
    }
    private void sendToAll(String msg) {
        int n = connections.size();
        for (int i = 0; i < n; i++) connections.get(i).sendString(msg);
    }
}
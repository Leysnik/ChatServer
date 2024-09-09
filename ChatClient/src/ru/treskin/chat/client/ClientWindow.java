package ru.treskin.chat.client;

import ru.treskin.network.json.MSGDescriptor;
import ru.treskin.network.TCPConnection;
import ru.treskin.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {
    private static final String SERVER_ADDRESS = "10.110.126.155";
    private static final int PORT = 8189;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private final JTextArea textArea = new JTextArea();
    private final JTextField msgField = new JTextField("Hello world");
    private TCPConnection connection;
    private final String userName;
    private final List<String> usernames;
    private final MSGDescriptor serializer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }
    private ClientWindow() {
        serializer = new MSGDescriptor();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(textArea);
        msgField.addActionListener(this);
        add(scroll, BorderLayout.CENTER);
        add(msgField, BorderLayout.SOUTH);

        userName = JOptionPane.showInputDialog("Enter your nickname");
        usernames = new ArrayList<>();
        setVisible(true);
        try {
            connection = new TCPConnection(this, SERVER_ADDRESS, PORT);
        } catch (IOException e) {
            printMsg("TCP Exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = msgField.getText();
        if (!connection.isClosed()) {
            msgField.setText(null);
            connection.sendString(serializer.toJson(1, userName, msg));
        }
    }

    @Override
    public void onConnectionReady(TCPConnection connection) {
        System.out.println(usernames);
        connection.sendString(serializer.toJson(2, userName, userName));
    }

    @Override
    public void onReceiveString(TCPConnection connection, String value) {
        System.out.println(usernames);
        if (value != null && !value.isEmpty()) {
            int type = serializer.fromJson(value).getType();
            if (type == 0) {
                usernames.addAll(serializer.names(value));
            } else if (type == 2) {
                String name = serializer.fromJson(value).getMessage();
                usernames.add(name);
                printMsg(name + " is here now");
            } else if (type == 3) {
                usernames.remove(serializer.fromJson(value).getMessage());
            } else printMsg(serializer.fromJson(value).toString());
        }
    }

    @Override
    public void onDisconnect(TCPConnection connection) {
        connection.sendString(serializer.toJson(2, userName, userName));
        printMsg("Client disconnected: " + connection);
    }

    @Override
    public void onException(TCPConnection connection, Exception e) {
        printMsg("Connection Exception: " + e);
    }
    private synchronized void printMsg(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.append(msg + "\n");
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        });

    }
}
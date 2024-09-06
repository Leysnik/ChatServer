package ru.treskin.chat.client;

import ru.treskin.network.TCPConnection;
import ru.treskin.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {
    private static final String SERVER_ADDRESS = "10.110.90.37";
    private static final int PORT = 8189;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private final JTextArea textArea = new JTextArea();
    private final JTextField nameField = new JTextField("write your name");
    private final JTextField msgField = new JTextField("Hello world");
    private TCPConnection connection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }
    private ClientWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        msgField.addActionListener(this);
        add(textArea, BorderLayout.CENTER);
        add(msgField, BorderLayout.SOUTH);
        add(nameField, BorderLayout.NORTH);
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
        if (msg != null && !msg.isEmpty()) {
            msgField.setText(null);
            connection.sendString(nameField.getText() + ": " + msg);
        }
    }

    @Override
    public void onConnectionReady(TCPConnection connection) {
        printMsg("Client connected: " + connection);
    }

    @Override
    public void onReceiveString(TCPConnection connection, String value) {
        if (value != null && !value.isEmpty()) printMsg(value);
    }

    @Override
    public void onDisconnect(TCPConnection connection) {
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
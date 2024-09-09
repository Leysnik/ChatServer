package ru.treskin.chat.client;

import ru.treskin.network.json.MSGDescriptor;
import ru.treskin.network.TCPConnection;
import ru.treskin.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {
    private static final String SERVER_ADDRESS = "10.193.94.178";
    private static final int PORT = 8189;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private final JTextArea textArea = new JTextArea();
    private final JTextField msgField = new JTextField("Hello world");
    private final JComboBox<String> userToSend = new JComboBox<>();
    private final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    private TCPConnection connection;
    private final String userName;
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
        add(userToSend, BorderLayout.NORTH);
        addUser("All");
        userName = JOptionPane.showInputDialog("Enter your nickname");
        setVisible(true);
        try {
            connection = new TCPConnection(this, SERVER_ADDRESS, PORT);
        } catch (IOException e) {
            printMsg("TCP Exception: " + e);
        }
    }
    public void addUser(String user) {
        if (user.equals(userName)) return;
        model.addElement(user);
        userToSend.setModel(model);
        this.add(userToSend, BorderLayout.NORTH);
    }
    public void addUsers(List<String> users) {
        for (String user : users) addUser(user);
    }
    public void removeUser(String user) {
        model.removeElement(user);
        userToSend.setModel(model);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(userToSend.getItemCount());
        String msg = msgField.getText();
        if (!connection.isClosed()) {
            msgField.setText(null);
            connection.sendString(serializer.toJson(1, userName, (String) userToSend.getSelectedItem(), msg));
        }
    }

    @Override
    public void onConnectionReady(TCPConnection connection) {
        connection.sendString(serializer.toJson(2, userName, userName));
    }

    @Override
    public void onReceiveString(TCPConnection connection, String value) {
        if (value != null && !value.isEmpty()) {
            int type = serializer.fromJson(value).getType();
            if (type == 0) {
                addUsers(serializer.names(value));
            } else if (type == 2) {
                String name = serializer.fromJson(value).getMessage();
                addUser(name);
                printMsg(name + " is here now");
            } else if (type == 3) {
                removeUser(serializer.fromJson(value).getMessage());
            } else printMsg(serializer.fromJson(value).toString());
        }
    }

    @Override
    public void onDisconnect(TCPConnection connection) {
        connection.sendString(serializer.toJson(3, userName, userName));
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
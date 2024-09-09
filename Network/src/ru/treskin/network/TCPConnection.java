package ru.treskin.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final TCPConnectionListener listener;


    public TCPConnection(TCPConnectionListener listener, String addr, int port) throws IOException {
        this(new Socket(addr, port), listener);
    }
    public TCPConnection(Socket socket, TCPConnectionListener listener) throws IOException {
        this.socket = socket;
        this.listener = listener;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),
                StandardCharsets.UTF_8));
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()) {
                        String msg = in.readLine();
                        listener.onReceiveString(TCPConnection.this, msg);
                    }
                } catch (IOException e) {
                    listener.onException(TCPConnection.this, e);
                } finally {
                    listener.onDisconnect(TCPConnection.this);
                }

            }
        });
        rxThread.start();
    }
    public synchronized void sendString(String str) {
        try {
            out.write(str + "\r\n");
            out.flush();
        } catch (IOException e) {
            listener.onException(TCPConnection.this, e);
            closeConnection();
        }
    }
    public synchronized void closeConnection() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onException(TCPConnection.this, e);
        }
    }
    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + " : " + socket.getPort();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TCPConnection)) return false;
        TCPConnection tcp = (TCPConnection) obj;
        return socket.getPort() == tcp.socket.getPort() && socket.getInetAddress().equals(tcp.socket.getInetAddress());
    }

    @Override
    public int hashCode() {
        return socket.hashCode();
    }
}

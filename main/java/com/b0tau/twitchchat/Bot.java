package com.b0tau.twitchchat;

import java.io.*;
import java.net.Socket;

public abstract class Bot {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String url;
    private int port;
    private String hiddenUsers;



    public Bot(String url, int port, String hiddenUsers) {
        this.url = url;
        this.port = port;
        this.hiddenUsers = hiddenUsers;
        try {
            this.socket = new Socket(url, port);
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    abstract void connect();

    abstract void disconnect();


    public BufferedWriter getWriter() {
        return writer;
    }

    public void setWriter(BufferedWriter writer) {
        this.writer = writer;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getHiddenUsers() {
        return hiddenUsers;
    }

    public void setHiddenUsers(String hiddenUsers) {
        this.hiddenUsers = hiddenUsers;
    }

}

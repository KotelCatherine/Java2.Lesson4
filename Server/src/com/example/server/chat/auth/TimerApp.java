package com.example.server.chat.auth;

import java.io.IOException;
import java.net.Socket;
import java.util.TimerTask;

public class TimerApp extends TimerTask {

    private final Socket clientSocket;

    public TimerApp(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



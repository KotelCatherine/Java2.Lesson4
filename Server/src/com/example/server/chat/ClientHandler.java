package com.example.server.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    public static final String AUTH_COMMAND = "/auth";
    public static final String AUTH_OK_COMMAND = "/authOk";

    private final MyServer server;
    private final Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public ClientHandler(MyServer server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public void handle() {
        try {
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                authenticate();
                readMessages();
            } catch (IOException e) {
                System.err.println("Failed to process message from client");
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }).start();

    }

    private void authenticate() throws IOException {
        while (true) {
            String message = inputStream.readUTF();
            if (message.startsWith(AUTH_COMMAND)) {
                String[] parts = message.split(" ");
                String login = parts[1];
                String password = parts[2];

                String userName = this.server.getAuthService().getUsernameByLoginAndPassword(login, password);

                if (userName == null) {
                    sendMessage("Некорретные логин и пароль");
                } else {
                    sendMessage(String.format("%s %s", AUTH_OK_COMMAND, userName));
                    server.subscribe(this);
                    return;
                }
            }
        }
    }

    public void readMessages() throws IOException {
        while (true) {
            String message = inputStream.readUTF().trim();
            System.out.println("message = " + message);

            if (message.startsWith("/end")) {
                return;
            } else {
                processMessage(message);
            }
        }
    }

    public void processMessage(String message) {
        this.server.broadcastMessage(message, this);
    }

    public void sendMessage(String message) {
        try {
            this.outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            outputStream.close();
            inputStream.close();
            server.unsubscribe(this);
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to close connection");
            e.printStackTrace();
        }
    }
}

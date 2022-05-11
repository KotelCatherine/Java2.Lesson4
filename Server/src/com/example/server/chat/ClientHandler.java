package com.example.server.chat;

import com.example.command.Command;
import com.example.command.CommandType;
import com.example.command.commands.AuthCommandData;
import com.example.command.commands.PrivateMessageCommandData;
import com.example.command.commands.PublicMessageCommandData;
import com.example.server.chat.auth.TimerApp;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler {

    private final MyServer server;
    private final Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;


    private String userName;

    public ClientHandler(MyServer server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public void handle() {
        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
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
        Timer timer = new Timer();
        TimerApp timerTask = new TimerApp(clientSocket);
        timer.schedule(timerTask, 120 * 1000);

        while (true) {
            Command command = readCommand();

            if (command == null) {
                continue;
            }

            if (command.getType() == CommandType.AUTH) {
                AuthCommandData data = (AuthCommandData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();
                String userName = this.server.getAuthService().getUsernameByLoginAndPassword(login, password);

                if (userName == null) {
                    sendCommand(Command.errorCommand("Некорректные имя пользователя или пароль"));
                } else if (server.isUserNameBusy(userName)) {
                    sendCommand(Command.errorCommand("Такой пользователь уже существует"));
                } else {
                    timer.cancel();

                    this.userName = userName;
                    sendCommand(Command.authOkCommand(userName));
                    server.subscribe(this);
                    return;
                }
            }

        }
    }

    private Command readCommand() throws IOException {
        Command command = null;

        try {
            command = (Command) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to read Command class");
            e.printStackTrace();
        }

        return command;
    }

    private void readMessages() throws IOException {
        while (true) {
            Command command = readCommand();

            if (command == null) {
                continue;
            }

            switch (command.getType()) {
                case PRIVATE_MESSAGE: {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    String receiver = data.getReceiver();
                    String privateMessage = data.getMessage();
                    server.sendPrivateMessage(this, receiver, privateMessage);
                    break;
                }
                case PUBLIC_MESSAGE: {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    processMessage(data.getMessage());
                    break;
                }
            }
        }
    }

    private void processMessage(String message) throws IOException {
        this.server.broadcastMessage(message, this);
    }

    public void sendCommand(Command command) throws IOException {
        outputStream.writeObject(command);
    }

    public String getUserName() {
        return userName;
    }

    public void closeConnection() {
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

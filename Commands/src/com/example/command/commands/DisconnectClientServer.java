package com.example.command.commands;

import java.io.Serializable;

public class DisconnectClientServer implements Serializable {
    private final String disconnectMessage;

    public DisconnectClientServer(String disConnectMessage) {
        this.disconnectMessage = disConnectMessage;
    }

    public String getDisconnectMessage() {
        return disconnectMessage;
    }
}

package com.example.command;

public enum CommandType {
    AUTH,
    AUTH_OK,
    ERROR,
    PUBLIC_MESSAGE,
    CLIENT_MESSAGE,
    PRIVATE_MESSAGE,
    UPDATE_USERS_LIST,
    DISCONNECT_CLIENT_SOCKET
}

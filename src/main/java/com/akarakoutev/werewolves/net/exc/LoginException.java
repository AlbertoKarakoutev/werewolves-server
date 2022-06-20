package com.akarakoutev.werewolves.net.exc;

public class LoginException extends Exception {

    public LoginException() {
        super("Player is already logged in!");
    }

    public LoginException(String message) {
        super(message);
    }


}

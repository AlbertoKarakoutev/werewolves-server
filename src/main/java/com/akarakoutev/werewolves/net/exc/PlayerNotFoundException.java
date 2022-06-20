package com.akarakoutev.werewolves.net.exc;

public class PlayerNotFoundException extends Exception{

    public PlayerNotFoundException() {
        super("Player not found!");
    }

    public PlayerNotFoundException(String message) {
        super(message);
    }

    public PlayerNotFoundException(Throwable throwable) {
        super(throwable);
    }

    public PlayerNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }


}

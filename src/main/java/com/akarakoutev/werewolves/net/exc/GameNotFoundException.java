package com.akarakoutev.werewolves.net.exc;

public class GameNotFoundException extends Exception{

    public GameNotFoundException() {
        super("Game not found!");
    }

    public GameNotFoundException(String gameId) {
        super("Game " + gameId + " not found!");
    }

}

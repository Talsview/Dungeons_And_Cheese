package com.seujogo;

public class GameState {

    private int currentFloor = 1;

    public int getFloor() {
        return currentFloor;
    }

    public void nextFloor() {
        currentFloor++;
    }
}
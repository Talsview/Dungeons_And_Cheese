package com.seujogo;

import java.util.HashMap;
import java.util.Map;

public class GameState {

    private int currentFloor = 1;
    private int score = 0;
    private int coins = 0;

    private Map<String, Integer> upgrades;

    public GameState() {
        upgrades = new HashMap<>();
    }

    public int getFloor() {
        return currentFloor;
    }

    public void nextFloor() {
        currentFloor++;
    }

    public void setFloor(int floor) {
        this.currentFloor = floor;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int value) {
        score += value;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int value) {
        coins += value;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void addUpgrade(String name) {
        upgrades.put(name, upgrades.getOrDefault(name, 0) + 1);
    }

    public int getUpgradeLevel(String name) {
        return upgrades.getOrDefault(name, 0);
    }

    public Map<String, Integer> getUpgrades() {
        return upgrades;
    }

    public void reset() {
        currentFloor = 1;
        score = 0;
        coins = 0;
        upgrades.clear();
    }
}
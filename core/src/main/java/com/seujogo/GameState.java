package com.seujogo;

import java.util.HashMap;
import java.util.Map;

public class GameState {

    // =====================================================
    // ATRIBUTOS PRINCIPAIS
    // =====================================================

    private int currentFloor = 1;
    private int score = 0;
    private int coins = 0;

    private Map<String, Integer> upgrades;

    // =====================================================
    // CONSTRUTOR
    // =====================================================

    public GameState() {
        upgrades = new HashMap<>();
    }

    // =====================================================
    // PROGRESSÃO (ANDARES)
    // =====================================================

    public int getFloor() {
        return currentFloor;
    }

    /**
     * Avança para o próximo andar.
     */
    public void nextFloor() {
        currentFloor++;
    }

    public void setFloor(int floor) {
        this.currentFloor = floor;
    }

    // =====================================================
    // SCORE
    // =====================================================

    public int getScore() {
        return score;
    }

    /**
     * Adiciona pontos ao score atual.
     */
    public void addScore(int value) {
        score += value;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // =====================================================
    // MOEDAS
    // =====================================================

    public int getCoins() {
        return coins;
    }

    /**
     * Adiciona moedas ao jogador.
     */
    public void addCoins(int value) {
        coins += value;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    // =====================================================
    // UPGRADES
    // =====================================================

    /**
     * Adiciona um nível ao upgrade informado.
     */
    public void addUpgrade(String name) {
        upgrades.put(name, upgrades.getOrDefault(name, 0) + 1);
    }

    /**
     * Retorna o nível atual de um upgrade.
     */
    public int getUpgradeLevel(String name) {
        return upgrades.getOrDefault(name, 0);
    }

    public Map<String, Integer> getUpgrades() {
        return upgrades;
    }

    // =====================================================
    // RESET
    // =====================================================

    /**
     * Reseta completamente o estado do jogo.
     */
    public void reset() {
        currentFloor = 1;
        score = 0;
        coins = 0;
        upgrades.clear();
    }
}
package com.seujogo;

import java.util.Random;

public class DungeonGenerator {

    private Random random = new Random();

    public Room generateRoom() {
        Room room = new Room();

        // número aleatório de inimigos (1 a 5)
        int enemyCount = 1 + random.nextInt(5);

        room.setEnemyCount(enemyCount);

        return room;
    }
}
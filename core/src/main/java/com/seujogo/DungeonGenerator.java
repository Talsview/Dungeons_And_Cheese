package com.seujogo;

import java.util.Random;

public class DungeonGenerator {

    private Random random = new Random();

    public Room generateRoom() {
        Room room = new Room();

        int enemyCount = 1 + random.nextInt(5);
        room.setEnemyCount(enemyCount);

        int chance = random.nextInt(100);

        if (chance < 15) {
            room.setRoomType(RoomType.TREASURE);
        } else {
            room.setRoomType(RoomType.NORMAL);
        }

        room.spawnEnemies();
        return room;
    }
}
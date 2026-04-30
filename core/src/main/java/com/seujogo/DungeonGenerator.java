package com.seujogo;

import java.util.Random;

public class DungeonGenerator {

    // =====================================================
    // ATRIBUTOS
    // =====================================================

    private Random random = new Random();

    // =====================================================
    // GERAÇÃO DE SALAS
    // =====================================================

    /**
     * Cria uma nova sala, define sua quantidade de inimigos,
     * escolhe o tipo da sala e depois gera os inimigos.
     */
    public Room generateRoom() {
        Room room = new Room();

        int enemyCount = 1 + random.nextInt(5);
        int chance = random.nextInt(100);

        room.setEnemyCount(enemyCount);
        defineRoomType(room, chance);

        room.spawnEnemies();

        return room;
    }

    // =====================================================
    // CONFIGURAÇÃO DA SALA
    // =====================================================

    /**
     * Define o tipo da sala com base em uma chance aleatória.
     */
    private void defineRoomType(Room room, int chance) {
        if (chance < 10) {
            room.setRoomType(RoomType.TREASURE);
        }   else {
                room.setRoomType(RoomType.NORMAL);
        }
    }
}
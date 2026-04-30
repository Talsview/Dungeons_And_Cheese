package com.seujogo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class DungeonMap {

    private HashMap<String, Room> rooms = new HashMap<>();
    private HashSet<String> visited = new HashSet<>();

    private Random random = new Random();

    private int currentX = 0;
    private int currentY = 0;

    private int maxRooms;

    public DungeonMap(DungeonGenerator generator, int floor) {
        maxRooms = 4 + random.nextInt(3) + floor;

        Room start = generator.generateRoom();
        start.setRoomType(RoomType.SAFE);
        start.spawnEnemies();

        rooms.put("0,0", start);
        visited.add("0,0");

        generateMap(generator);
    }

    private void generateMap(DungeonGenerator generator) {
        List<int[]> frontier = new ArrayList<>();
        frontier.add(new int[]{0, 0});

        int created = 0;

        while (!frontier.isEmpty() && created < maxRooms) {
            int[] pos = frontier.remove(0);

            int x = pos[0];
            int y = pos[1];

            Room current = rooms.get(key(x, y));

            List<String> dirs = new ArrayList<>(Arrays.asList("UP", "DOWN", "LEFT", "RIGHT"));
            Collections.shuffle(dirs);

            int connections = 1 + random.nextInt(2);

            for (String d : dirs) {
                if (connections <= 0 || created >= maxRooms) {
                    break;
                }

                int nx = x;
                int ny = y;

                switch (d) {
                    case "UP":
                        ny++;
                        break;
                    case "DOWN":
                        ny--;
                        break;
                    case "LEFT":
                        nx--;
                        break;
                    case "RIGHT":
                        nx++;
                        break;
                    default:
                        break;
                }

                String nk = key(nx, ny);

                if (!rooms.containsKey(nk)) {
                    Room next = getOrCreate(nx, ny, generator);

                    connect(current, next, d);
                    frontier.add(new int[]{nx, ny});

                    created++;
                    connections--;
                }
            }
        }

        defineBoss();
        defineShop(generator);
    }

    private void defineBoss() {
        int maxDist = -1;
        String bossKey = null;

        for (String k : rooms.keySet()) {
            String[] p = k.split(",");

            int x = Integer.parseInt(p[0]);
            int y = Integer.parseInt(p[1]);

            int dist = Math.abs(x) + Math.abs(y);

            if (dist > maxDist) {
                maxDist = dist;
                bossKey = k;
            }
        }

        if (bossKey != null) {
            Room boss = rooms.get(bossKey);
            boss.setRoomType(RoomType.BOSS);
            boss.spawnEnemies();
        }
    }

    private void defineShop(DungeonGenerator generator) {
        int bestX = 0;
        int bestY = 0;
        int bestDistance = -1;

        for (String k : rooms.keySet()) {
            if (k.equals("0,0")) {
                continue;
            }

            Room room = rooms.get(k);

            if (room.getRoomType() == RoomType.BOSS || room.getRoomType() == RoomType.SHOP) {
                continue;
            }

            String[] parts = k.split(",");

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);

            int distance = Math.abs(x) + Math.abs(y);

            if (distance > bestDistance) {
                bestDistance = distance;
                bestX = x;
                bestY = y;
            }
        }

        if (bestDistance == -1) {
            return;
        }

        createShopNextTo(generator, bestX, bestY);
    }

    private void createShopNextTo(DungeonGenerator generator, int baseX, int baseY) {
        String[] directions = getPreferredShopDirections(baseX, baseY);

        for (String direction : directions) {
            int shopX = baseX;
            int shopY = baseY;

            switch (direction) {
                case "UP":
                    shopY++;
                    break;
                case "DOWN":
                    shopY--;
                    break;
                case "LEFT":
                    shopX--;
                    break;
                case "RIGHT":
                    shopX++;
                    break;
                default:
                    break;
            }

            if (rooms.containsKey(key(shopX, shopY))) {
                continue;
            }

            Room shop = generator.generateRoom();
            shop.setRoomType(RoomType.SHOP);
            shop.spawnEnemies();

            rooms.put(key(shopX, shopY), shop);
            connect(rooms.get(key(baseX, baseY)), shop, direction);

            return;
        }
    }

    private String[] getPreferredShopDirections(int x, int y) {
        if (Math.abs(x) >= Math.abs(y)) {
            if (x >= 0) {
                return new String[]{"RIGHT", "UP", "DOWN", "LEFT"};
            }

            return new String[]{"LEFT", "UP", "DOWN", "RIGHT"};
        }

        if (y >= 0) {
            return new String[]{"UP", "RIGHT", "LEFT", "DOWN"};
        }

        return new String[]{"DOWN", "RIGHT", "LEFT", "UP"};
    }

    private Room getOrCreate(int x, int y, DungeonGenerator generator) {
        String k = key(x, y);

        if (rooms.containsKey(k)) {
            return rooms.get(k);
        }

        Room r = generator.generateRoom();
        rooms.put(k, r);

        return r;
    }

    private void connect(Room a, Room b, String dir) {
        switch (dir) {
            case "UP":
                a.addDoor("UP");
                b.addDoor("DOWN");
                break;
            case "DOWN":
                a.addDoor("DOWN");
                b.addDoor("UP");
                break;
            case "LEFT":
                a.addDoor("LEFT");
                b.addDoor("RIGHT");
                break;
            case "RIGHT":
                a.addDoor("RIGHT");
                b.addDoor("LEFT");
                break;
            default:
                break;
        }
    }

    public boolean move(String dir) {
        Room current = getCurrentRoom();

        if (current == null) {
            return false;
        }

        int nx = currentX;
        int ny = currentY;

        switch (dir) {
            case "UP":
                if (!current.hasDoorUp()) return false;
                ny++;
                break;
            case "DOWN":
                if (!current.hasDoorDown()) return false;
                ny--;
                break;
            case "LEFT":
                if (!current.hasDoorLeft()) return false;
                nx--;
                break;
            case "RIGHT":
                if (!current.hasDoorRight()) return false;
                nx++;
                break;
            default:
                return false;
        }

        String nextKey = key(nx, ny);

        if (!rooms.containsKey(nextKey)) {
            return false;
        }

        currentX = nx;
        currentY = ny;

        visited.add(nextKey);

        return true;
    }

    private String key(int x, int y) {
        return x + "," + y;
    }

    public Room getCurrentRoom() {
        return rooms.get(key(currentX, currentY));
    }

    public HashMap<String, Room> getRooms() {
        return rooms;
    }

    public boolean isVisited(int x, int y) {
        return visited.contains(key(x, y));
    }

    public int getCurrentX() {
        return currentX;
    }

    public int getCurrentY() {
        return currentY;
    }
}
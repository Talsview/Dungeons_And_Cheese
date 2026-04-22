package com.seujogo;

import java.util.*;

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

    private String key(int x, int y) {
        return x + "," + y;
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
        }
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

                if (connections <= 0 || created >= maxRooms) break;

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

    public Room getCurrentRoom() {
        return rooms.get(key(currentX, currentY));
    }

    public boolean move(String dir) {

        Room current = getCurrentRoom();
        if (current == null) return false;

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
        if (!rooms.containsKey(nextKey)) return false;

        currentX = nx;
        currentY = ny;

        visited.add(nextKey);
        return true;
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
package com.seujogo;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.Iterator;

public class Room {

    private ArrayList<Enemy> enemies;
    private int enemyCount = 3;

    private boolean isSafe = false;
    private boolean isBoss = false;

    private RoomType roomType = RoomType.NORMAL;
    private boolean rewardCollected = false;

    private boolean doorUp = false;
    private boolean doorDown = false;
    private boolean doorLeft = false;
    private boolean doorRight = false;

    private Rectangle doorTop;
    private Rectangle doorBottom;
    private Rectangle doorLeftRect;
    private Rectangle doorRightRect;

    private Rectangle hatch;

    private static Texture doorUpTex;
    private static Texture doorDownTex;
    private static Texture doorLeftTex;
    private static Texture doorRightTex;

    private static Texture floorTex;

    private static Texture wallTopTex;
    private static Texture wallBottomTex;
    private static Texture wallLeftTex;
    private static Texture wallRightTex;

    private static Texture cornerTLTex;
    private static Texture cornerTRTex;
    private static Texture cornerBLTex;
    private static Texture cornerBRTex;

    private static Texture hatchTexture;

    public Room() {
        enemies = new ArrayList<>();

        int width = 800;
        int height = 600;
        int doorSize = 64;

        doorTop = new Rectangle(width / 2 - doorSize / 2, height - doorSize, doorSize, doorSize);
        doorBottom = new Rectangle(width / 2 - doorSize / 2, 0, doorSize, doorSize);
        doorLeftRect = new Rectangle(0, height / 2 - doorSize / 2, doorSize, doorSize);
        doorRightRect = new Rectangle(width - doorSize, height / 2 - doorSize / 2, doorSize, doorSize);

        hatch = new Rectangle(380, 280, 40, 40);

        if (doorUpTex == null) doorUpTex = new Texture("door_up.png");
        if (doorDownTex == null) doorDownTex = new Texture("door_down.png");
        if (doorLeftTex == null) doorLeftTex = new Texture("door_left.png");
        if (doorRightTex == null) doorRightTex = new Texture("door_right.png");

        if (floorTex == null) floorTex = new Texture("floor.png");

        if (wallTopTex == null) wallTopTex = new Texture("wall_top.png");
        if (wallBottomTex == null) wallBottomTex = new Texture("wall_bottom.png");
        if (wallLeftTex == null) wallLeftTex = new Texture("wall_left.png");
        if (wallRightTex == null) wallRightTex = new Texture("wall_right.png");

        if (cornerTLTex == null) cornerTLTex = new Texture("corner_tl.png");
        if (cornerTRTex == null) cornerTRTex = new Texture("corner_tr.png");
        if (cornerBLTex == null) cornerBLTex = new Texture("corner_bl.png");
        if (cornerBRTex == null) cornerBRTex = new Texture("corner_br.png");

        if (hatchTexture == null) hatchTexture = new Texture("hatch.png");
    }

    public void setDoors(boolean up, boolean down, boolean left, boolean right) {
        this.doorUp = up;
        this.doorDown = down;
        this.doorLeft = left;
        this.doorRight = right;
    }

    public void addDoor(String dir) {
        switch (dir) {
            case "UP":
                doorUp = true;
                break;
            case "DOWN":
                doorDown = true;
                break;
            case "LEFT":
                doorLeft = true;
                break;
            case "RIGHT":
                doorRight = true;
                break;
        }
    }

    public boolean hasDoorUp() {
        return doorUp;
    }

    public boolean hasDoorDown() {
        return doorDown;
    }

    public boolean hasDoorLeft() {
        return doorLeft;
    }

    public boolean hasDoorRight() {
        return doorRight;
    }

    public void setSafe(boolean safe) {
        this.isSafe = safe;
        if (safe) {
            this.roomType = RoomType.SAFE;
        }
    }

    public void setBoss(boolean boss) {
        this.isBoss = boss;
        if (boss) {
            this.roomType = RoomType.BOSS;
        }
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;

        switch (roomType) {
            case SAFE:
                this.isSafe = true;
                this.isBoss = false;
                break;
            case TREASURE:
                this.isSafe = false;
                this.isBoss = false;
                break;
            case BOSS:
                this.isBoss = true;
                this.isSafe = false;
                break;
            case NORMAL:
            default:
                this.isSafe = false;
                this.isBoss = false;
                break;
        }
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setEnemyCount(int count) {
        this.enemyCount = count;
    }

    public void spawnEnemies() {
        enemies.clear();

        if (isSafe || roomType == RoomType.TREASURE) return;

        int count = isBoss ? 1 : enemyCount;

        for (int i = 0; i < count; i++) {
            if (isBoss) {
                Enemy boss = new Enemy(300, 300);
                boss.setHealth(20);
                enemies.add(boss);
            } else {
                enemies.add(new Enemy(200 + i * 60, 200));
            }
        }
    }

    public void update(float delta, Player player, GameState gameState) {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            e.update(delta, player);
        }

        Array<Bullet> bullets = player.getBullets();

        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            Rectangle bb = b.getBounds();

            float wallThickness = 16f;
            float roomWidth = 800f;
            float roomHeight = 600f;

            boolean hitWall =
                bb.x < wallThickness ||
                bb.x + bb.width > roomWidth - wallThickness ||
                bb.y < wallThickness ||
                bb.y + bb.height > roomHeight - wallThickness;

            if (hitWall) {
                bullets.removeIndex(i);
                continue;
            }

            Iterator<Enemy> enemyIt = enemies.iterator();

            while (enemyIt.hasNext()) {
                Enemy e = enemyIt.next();

                if (bb.overlaps(e.getBounds())) {
                    e.takeDamage(1);

                    if (e.isDead()) {
                        gameState.addScore(10);
                        gameState.addCoins(1);
                        enemyIt.remove();
                    }

                    bullets.removeIndex(i);
                    break;
                }
            }
        }

        if (roomType == RoomType.TREASURE && !rewardCollected) {
            if (player.getBounds().overlaps(hatch)) {
                gameState.addCoins(5);
                gameState.addScore(25);
                rewardCollected = true;
            }
        }
    }

    public void resolveWallCollision(Player player) {
        Rectangle p = player.getBounds();

        float px = p.x;
        float py = p.y;
        float pw = p.width;
        float ph = p.height;

        float roomWidth = 800;
        float roomHeight = 600;
        float wallThickness = 16f;

        boolean inTopDoor =
            doorUp &&
            px + pw > doorTop.x &&
            px < doorTop.x + doorTop.width;

        boolean inBottomDoor =
            doorDown &&
            px + pw > doorBottom.x &&
            px < doorBottom.x + doorBottom.width;

        boolean inLeftDoor =
            doorLeft &&
            py + ph > doorLeftRect.y &&
            py < doorLeftRect.y + doorLeftRect.height;

        boolean inRightDoor =
            doorRight &&
            py + ph > doorRightRect.y &&
            py < doorRightRect.y + doorRightRect.height;

        if (px < wallThickness && !inLeftDoor) {
            px = wallThickness;
        }

        if (px + pw > roomWidth - wallThickness && !inRightDoor) {
            px = roomWidth - wallThickness - pw;
        }

        if (py < wallThickness && !inBottomDoor) {
            py = wallThickness;
        }

        if (py + ph > roomHeight - wallThickness && !inTopDoor) {
            py = roomHeight - wallThickness - ph;
        }

        player.setPosition(px, py);
    }

    public void render(SpriteBatch batch) {
        int tileSize = 16;
        int roomWidth = 800;
        int roomHeight = 600;

        float doorHWidth = 64f;
        float doorHHeight = 16f;

        float doorVWidth = 16f;
        float doorVHeight = 64f;

        float gapPadding = 4f;

        float upStartX = doorTop.x;
        float upEndX = doorTop.x + doorHWidth;

        float downStartX = doorBottom.x;
        float downEndX = doorBottom.x + doorHWidth;

        float leftStartY = doorLeftRect.y;
        float leftEndY = doorLeftRect.y + doorVHeight;

        float rightStartY = doorRightRect.y;
        float rightEndY = doorRightRect.y + doorVHeight;

        for (int x = 0; x < roomWidth; x += tileSize) {
            for (int y = 0; y < roomHeight; y += tileSize) {

                boolean top = y + tileSize >= roomHeight;
                boolean bottom = y == 0;
                boolean left = x == 0;
                boolean right = x + tileSize >= roomWidth;

                boolean isDoorGap = false;

                if (doorUp && top &&
                    x + tileSize > upStartX - gapPadding &&
                    x < upEndX + gapPadding) {
                    isDoorGap = true;
                }

                if (doorDown && bottom &&
                    x + tileSize > downStartX - gapPadding &&
                    x < downEndX + gapPadding) {
                    isDoorGap = true;
                }

                if (doorLeft && left &&
                    y + tileSize > leftStartY - gapPadding &&
                    y < leftEndY + gapPadding) {
                    isDoorGap = true;
                }

                if (doorRight && right &&
                    y + tileSize > rightStartY - gapPadding &&
                    y < rightEndY + gapPadding) {
                    isDoorGap = true;
                }

                if (isDoorGap) {
                    batch.draw(floorTex, x, y, tileSize, tileSize);
                } else if (top && left) {
                    batch.draw(cornerTLTex, x, y, tileSize, tileSize);
                } else if (top && right) {
                    batch.draw(cornerTRTex, x, y, tileSize, tileSize);
                } else if (bottom && left) {
                    batch.draw(cornerBLTex, x, y, tileSize, tileSize);
                } else if (bottom && right) {
                    batch.draw(cornerBRTex, x, y, tileSize, tileSize);
                } else if (top) {
                    batch.draw(wallTopTex, x, y, tileSize, tileSize);
                } else if (bottom) {
                    batch.draw(wallBottomTex, x, y, tileSize, tileSize);
                } else if (left) {
                    batch.draw(wallLeftTex, x, y, tileSize, tileSize);
                } else if (right) {
                    batch.draw(wallRightTex, x, y, tileSize, tileSize);
                } else {
                    batch.draw(floorTex, x, y, tileSize, tileSize);
                }
            }
        }

        for (Enemy e : enemies) {
            e.render(batch);
        }

        if (doorUp) {
            batch.draw(
                doorUpTex,
                doorTop.x,
                doorTop.y + (doorTop.height - doorHHeight),
                doorHWidth,
                doorHHeight
            );
        }

        if (doorDown) {
            batch.draw(
                doorDownTex,
                doorBottom.x,
                doorBottom.y,
                doorHWidth,
                doorHHeight
            );
        }

        if (doorLeft) {
            batch.draw(
                doorLeftTex,
                doorLeftRect.x,
                doorLeftRect.y,
                doorVWidth,
                doorVHeight
            );
        }

        if (doorRight) {
            batch.draw(
                doorRightTex,
                doorRightRect.x + (doorRightRect.width - doorVWidth),
                doorRightRect.y,
                doorVWidth,
                doorVHeight
            );
        }

        if (isBoss && isCleared()) {
            batch.draw(hatchTexture, hatch.x, hatch.y, hatch.width, hatch.height);
        }

        if (roomType == RoomType.TREASURE && !rewardCollected) {
            batch.draw(hatchTexture, hatch.x, hatch.y, hatch.width, hatch.height);
        }
    }

    public boolean isCleared() {
        return enemies.isEmpty();
    }

    public boolean isBossCleared() {
        return isBoss && enemies.isEmpty();
    }

    public boolean isPlayerOnHatch(Player player) {
        if (!isBoss || !isCleared()) return false;
        return player.getBounds().overlaps(hatch);
    }

    public String checkDoor(Player player) {
        if (!isCleared()) return null;

        Rectangle p = player.getBounds();

        if (doorUp && p.overlaps(doorTop)) return "UP";
        if (doorDown && p.overlaps(doorBottom)) return "DOWN";
        if (doorLeft && p.overlaps(doorLeftRect)) return "LEFT";
        if (doorRight && p.overlaps(doorRightRect)) return "RIGHT";

        return null;
    }

    public boolean isBossRoomActive() {
        return isBoss;
    }

    public int getBossHealth() {
        if (!isBoss || enemies.isEmpty()) return 0;
        return enemies.get(0).getHealth();
    }

    public void dispose() {
        // vazio por enquanto
        // as texturas são static e compartilhadas entre salas
    }
}
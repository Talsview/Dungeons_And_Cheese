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

    // 🔥 PORTAS DINÂMICAS
    private boolean doorUp = false;
    private boolean doorDown = false;
    private boolean doorLeft = false;
    private boolean doorRight = false;

    private Rectangle doorTop;
    private Rectangle doorBottom;
    private Rectangle doorLeftRect;
    private Rectangle doorRightRect;

    // 🚪 4 imagens de porta
    private Texture doorUpTex;
    private Texture doorDownTex;
    private Texture doorLeftTex;
    private Texture doorRightTex;
    
    //Textura Chao
    private Texture floorTex;

    // 🧱 PAREDES (opcional para decoração)
    private Texture wallTopTex;
    private Texture wallBottomTex;
    private Texture wallLeftTex;
    private Texture wallRightTex;

    // 🧱 CANTOS (opcional para decoração)
    private Texture cornerTLTex;
    private Texture cornerTRTex;
    private Texture cornerBLTex;
    private Texture cornerBRTex;

    // 🕳️ ESCOTILHA
    private Rectangle hatch;
    private Texture hatchTexture;

    public Room() {
        enemies = new ArrayList<>();

        int width = 800;
        int height = 600;

        int doorSize = 64; // escala 2x (32 * 2)

        doorTop = new Rectangle(width / 2 - doorSize / 2, height - doorSize, doorSize, doorSize);
        doorBottom = new Rectangle(width / 2 - doorSize / 2, 0, doorSize, doorSize);
        doorLeftRect = new Rectangle(0, height / 2 - doorSize / 2, doorSize, doorSize);
        doorRightRect = new Rectangle(width - doorSize, height / 2 - doorSize / 2, doorSize, doorSize);

        // 🚪 carrega uma textura para cada direção
        doorUpTex = new Texture("door_up.png");
        doorDownTex = new Texture("door_down.png");
        doorLeftTex = new Texture("door_left.png");
        doorRightTex = new Texture("door_right.png");

        // Textura do chão
        floorTex = new Texture("floor.png");

        // 🧱 texturas para paredes (opcional)
        wallTopTex = new Texture("wall_top.png");
        wallBottomTex = new Texture("wall_bottom.png");
        wallLeftTex = new Texture("wall_left.png");
        wallRightTex = new Texture("wall_right.png");

        // 🧱 texturas para cantos (opcional)
        cornerTLTex = new Texture("corner_tl.png");
        cornerTRTex = new Texture("corner_tr.png");
        cornerBLTex = new Texture("corner_bl.png");
        cornerBRTex = new Texture("corner_br.png");

        // 🕳️ define a escotilha no centro da sala
        hatch = new Rectangle(380, 280, 40, 40);
        hatchTexture = new Texture("hatch.png");
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
    }

    public void setBoss(boolean boss) {
        this.isBoss = boss;
    }

    public void setEnemyCount(int count) {
        this.enemyCount = count;
    }

    public void spawnEnemies() {

        enemies.clear();

        if (isSafe) return;

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

    public void update(float delta, Player player) {

        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            e.update(delta, player);
        }

        Array<Bullet> bullets = player.getBullets();

        for (int i = 0; i < bullets.size; i++) {
            Bullet b = bullets.get(i);

            Iterator<Enemy> enemyIt = enemies.iterator();

            while (enemyIt.hasNext()) {
                Enemy e = enemyIt.next();

                if (b.getBounds().overlaps(e.getBounds())) {
                    e.takeDamage(1);

                    if (e.isDead()) {
                        enemyIt.remove();
                    }

                    bullets.removeIndex(i);
                    i--;
                    break;
                }
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

    // 🔥 tamanho real da porta (achatada)
    float doorHWidth = 64f;
    float doorHHeight = 16f;

    float doorVWidth = 16f;
    float doorVHeight = 64f;

    float gapPadding = 4f;

    // 🔥 calcula área real da abertura
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

            // 🔥 UP
            if (doorUp && top &&
                x + tileSize > upStartX - gapPadding &&
                x < upEndX + gapPadding) {
                isDoorGap = true;
            }

            // 🔥 DOWN
            if (doorDown && bottom &&
                x + tileSize > downStartX - gapPadding &&
                x < downEndX + gapPadding) {
                isDoorGap = true;
            }

            // 🔥 LEFT
            if (doorLeft && left &&
                y + tileSize > leftStartY - gapPadding &&
                y < leftEndY + gapPadding) {
                isDoorGap = true;
            }

            // 🔥 RIGHT
            if (doorRight && right &&
                y + tileSize > rightStartY - gapPadding &&
                y < rightEndY + gapPadding) {
                isDoorGap = true;
            }

            if (isDoorGap) {
                batch.draw(floorTex, x, y, tileSize, tileSize);
            }
            else if (top && left) {
                batch.draw(cornerTLTex, x, y, tileSize, tileSize);
            }
            else if (top && right) {
                batch.draw(cornerTRTex, x, y, tileSize, tileSize);
            }
            else if (bottom && left) {
                batch.draw(cornerBLTex, x, y, tileSize, tileSize);
            }
            else if (bottom && right) {
                batch.draw(cornerBRTex, x, y, tileSize, tileSize);
            }
            else if (top) {
                batch.draw(wallTopTex, x, y, tileSize, tileSize);
            }
            else if (bottom) {
                batch.draw(wallBottomTex, x, y, tileSize, tileSize);
            }
            else if (left) {
                batch.draw(wallLeftTex, x, y, tileSize, tileSize);
            }
            else if (right) {
                batch.draw(wallRightTex, x, y, tileSize, tileSize);
            }
            else {
                batch.draw(floorTex, x, y, tileSize, tileSize);
            }
        }
    }

    for (Enemy e : enemies) {
        e.render(batch);
    }

    // 🔥 DESENHO DAS PORTAS (ACHATADAS E ALINHADAS CORRETAMENTE)

    if (doorUp)
        batch.draw(
            doorUpTex,
            doorTop.x,
            doorTop.y + (doorTop.height - doorHHeight),
            doorHWidth,
            doorHHeight
        );

    if (doorDown)
        batch.draw(
            doorDownTex,
            doorBottom.x,
            doorBottom.y,
            doorHWidth,
            doorHHeight
        );

    if (doorLeft)
        batch.draw(
            doorLeftTex,
            doorLeftRect.x,
            doorLeftRect.y,
            doorVWidth,
            doorVHeight
        );

    if (doorRight)
        batch.draw(
            doorRightTex,
            doorRightRect.x + (doorRightRect.width - doorVWidth),
            doorRightRect.y,
            doorVWidth,
            doorVHeight
        );

    if (isBoss && isCleared()) {
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
    doorUpTex.dispose();
    doorDownTex.dispose();
    doorLeftTex.dispose();
    doorRightTex.dispose();
    hatchTexture.dispose();
    floorTex.dispose();
    wallTopTex.dispose();
    wallBottomTex.dispose();
    wallLeftTex.dispose();
    wallRightTex.dispose();
    cornerTLTex.dispose();
    cornerTRTex.dispose();
    cornerBLTex.dispose();
    cornerBRTex.dispose();
    }

    
}
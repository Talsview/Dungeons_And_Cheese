package com.seujogo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Room {

    private final int ROOM_WIDTH = 800;
    private final int ROOM_HEIGHT = 600;
    private final int DOOR_SIZE = 64;
    private final int TILE_SIZE = 16;

    private final float WALL_THICKNESS = 16f;

    private ArrayList<Enemy> enemies;
    private int enemyCount = 3;
    private Random random = new Random();

    private boolean isSafe = false;
    private boolean isBoss = false;

    private RoomType roomType = RoomType.NORMAL;
    private boolean rewardCollected = false;

    private Rectangle damageShopZone = new Rectangle(260, 260, 100, 80);
    private Rectangle healShopZone = new Rectangle(440, 260, 100, 80);

    private boolean damageShopUsed = false;
    private boolean healShopUsed = false;
    private boolean shopUsed = false;

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
    private static Texture treasureRewardTex;

    private static Texture wallTopTex;
    private static Texture wallBottomTex;
    private static Texture wallLeftTex;
    private static Texture wallRightTex;

    private static Texture cornerTLTex;
    private static Texture cornerTRTex;
    private static Texture cornerBLTex;
    private static Texture cornerBRTex;

    private static Texture hatchTexture;

    private static Texture shopDamageTex;
    private static Texture shopHealTex;

    private static BitmapFont shopFont;

    public Room() {
        enemies = new ArrayList<>();

        createDoorRectangles();
        loadTexturesIfNeeded();
    }

    private void createDoorRectangles() {
        doorTop = new Rectangle(ROOM_WIDTH / 2f - DOOR_SIZE / 2f, ROOM_HEIGHT - DOOR_SIZE, DOOR_SIZE, DOOR_SIZE);
        doorBottom = new Rectangle(ROOM_WIDTH / 2f - DOOR_SIZE / 2f, 0, DOOR_SIZE, DOOR_SIZE);
        doorLeftRect = new Rectangle(0, ROOM_HEIGHT / 2f - DOOR_SIZE / 2f, DOOR_SIZE, DOOR_SIZE);
        doorRightRect = new Rectangle(ROOM_WIDTH - DOOR_SIZE, ROOM_HEIGHT / 2f - DOOR_SIZE / 2f, DOOR_SIZE, DOOR_SIZE);

        hatch = new Rectangle(380, 280, 40, 40);
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
            default:
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

            case BOSS:
                this.isBoss = true;
                this.isSafe = false;
                break;

            case TREASURE:
            case SHOP:
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

        if (isSafe || roomType == RoomType.TREASURE || roomType == RoomType.SHOP) {
            return;
        }

        int count = isBoss ? 1 : enemyCount;

        float[][] spawnPoints = {
            {180, 420},
            {620, 420},
            {180, 180},
            {620, 180},
            {400, 420},
            {400, 180}
        };

        for (int i = 0; i < count; i++) {
            float spawnX = spawnPoints[i % spawnPoints.length][0];
            float spawnY = spawnPoints[i % spawnPoints.length][1];

            if (isBoss) {
                enemies.add(new BossEnemy(376, 276));
            } else {
                enemies.add(new Enemy(spawnX, spawnY, getRandomEnemyType()));
            }
        }
    }

    private Enemy.EnemyType getRandomEnemyType() {
        int chance = random.nextInt(100);

        if (chance < 20) return Enemy.EnemyType.CHASER;
        if (chance < 40) return Enemy.EnemyType.SHOOTER;
        if (chance < 55) return Enemy.EnemyType.TANK;
        if (chance < 70) return Enemy.EnemyType.KAMIKAZE;
        if (chance < 80) return Enemy.EnemyType.SNIPER;
        if (chance < 90) return Enemy.EnemyType.SPLITTER;

        return Enemy.EnemyType.STRAFER;
    }

    public void update(float delta, Player player, GameState gameState) {
        updateEnemies(delta, player);
        updatePlayerBullets(player, gameState);
        updateTreasureReward(player, gameState);
        updateShop(player, gameState);
    }

    private void updateEnemies(float delta, Player player) {
        BossEnemy bossToSummon = null;

        Iterator<Enemy> it = enemies.iterator();

        while (it.hasNext()) {
            Enemy e = it.next();
            e.update(delta, player);

            if (e instanceof BossEnemy) {
                bossToSummon = (BossEnemy) e;
            }
        }

        if (bossToSummon != null) {
            spawnBossMinions(bossToSummon);
        }
    }

    private void updatePlayerBullets(Player player, GameState gameState) {
        Array<Bullet> bullets = player.getBullets();

        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            Rectangle bb = b.getBounds();

            if (bulletHitWall(bb)) {
                bullets.removeIndex(i);
                continue;
            }

            if (bulletHitEnemy(b, bb, player, gameState)) {
                bullets.removeIndex(i);
            }
        }
    }

    private boolean bulletHitWall(Rectangle bb) {
        return bb.x < WALL_THICKNESS
            || bb.x + bb.width > ROOM_WIDTH - WALL_THICKNESS
            || bb.y < WALL_THICKNESS
            || bb.y + bb.height > ROOM_HEIGHT - WALL_THICKNESS;
    }

    private boolean bulletHitEnemy(Bullet bullet, Rectangle bulletBounds, Player player, GameState gameState) {
        Iterator<Enemy> enemyIt = enemies.iterator();

        while (enemyIt.hasNext()) {
            Enemy e = enemyIt.next();

            if (!bulletBounds.overlaps(e.getBounds())) {
                continue;
            }

            e.takeDamage(player.getDamage());

            if (e.isDead()) {
                addEnemyReward(e, gameState);
                enemyIt.remove();
            }

            return true;
        }

        return false;
    }

    private void addEnemyReward(Enemy enemy, GameState gameState) {
        int scoreReward = 10;
        int coinReward = 1;

        if (enemy instanceof BossEnemy) {
            scoreReward = 120;
            coinReward = 12;
        } else {
            switch (enemy.getType()) {
                case CHASER:
                    scoreReward = 8;
                    coinReward = 1;
                    break;
                case TANK:
                    scoreReward = 18;
                    coinReward = 2;
                    break;
                case KAMIKAZE:
                    scoreReward = 14;
                    coinReward = 1;
                    break;
                case SNIPER:
                    scoreReward = 16;
                    coinReward = 2;
                    break;
                case SPLITTER:
                case STRAFER:
                    scoreReward = 15;
                    coinReward = 2;
                    break;
                case SHOOTER:
                default:
                    scoreReward = 12;
                    coinReward = 1;
                    break;
            }
        }

        gameState.addScore(scoreReward);
        gameState.addCoins(coinReward);
    }

    private void updateTreasureReward(Player player, GameState gameState) {
        if (roomType != RoomType.TREASURE || rewardCollected) {
            return;
        }

        if (player.getBounds().overlaps(hatch)) {
            gameState.addCoins(5);
            gameState.addScore(25);
            rewardCollected = true;
        }
    }

    private void updateShop(Player player, GameState gameState) {
        if (roomType != RoomType.SHOP) {
            return;
        }

        if (damageShopUsed && healShopUsed) {
            shopUsed = true;
            return;
        }

        Rectangle playerBounds = player.getBounds();
        int coins = gameState.getCoins();

        if (!damageShopUsed && playerBounds.overlaps(damageShopZone) && coins >= 8) {
            gameState.setCoins(coins - 8);
            player.increaseDamage(1);
            damageShopUsed = true;
            return;
        }

        if (!healShopUsed && playerBounds.overlaps(healShopZone) && coins >= 5) {
            gameState.setCoins(coins - 5);
            player.heal(20);
            healShopUsed = true;
        }
    }

    private void spawnBossMinions(BossEnemy boss) {
        int summons = boss.consumePendingSummons();

        if (summons <= 0) {
            return;
        }

        int maxMinions = boss.isEnraged() ? 5 : 4;
        int currentMinions = countCurrentMinions();

        if (currentMinions >= maxMinions) {
            return;
        }

        float[][] spawnPoints = {
            {120, 460},
            {660, 460},
            {120, 120},
            {660, 120},
            {400, 500},
            {400, 100}
        };

        for (int i = 0; i < summons && currentMinions < maxMinions; i++) {
            float spawnX = spawnPoints[(currentMinions + i) % spawnPoints.length][0];
            float spawnY = spawnPoints[(currentMinions + i) % spawnPoints.length][1];

            enemies.add(new Enemy(spawnX, spawnY, getBossMinionType(boss)));
            currentMinions++;
        }
    }

    private int countCurrentMinions() {
        int currentMinions = 0;

        for (Enemy e : enemies) {
            if (!(e instanceof BossEnemy)) {
                currentMinions++;
            }
        }

        return currentMinions;
    }

    private Enemy.EnemyType getBossMinionType(BossEnemy boss) {
        double r = Math.random();

        if (boss.isEnraged()) {
            if (r < 0.34) return Enemy.EnemyType.CHASER;
            if (r < 0.68) return Enemy.EnemyType.STRAFER;
            return Enemy.EnemyType.SPLITTER;
        }

        if (r < 0.5) {
            return Enemy.EnemyType.CHASER;
        }

        return Enemy.EnemyType.SHOOTER;
    }

    public void resolveWallCollision(Player player) {
        Rectangle p = player.getBounds();

        float px = p.x;
        float py = p.y;
        float pw = p.width;
        float ph = p.height;

        boolean inTopDoor = doorUp && px + pw > doorTop.x && px < doorTop.x + doorTop.width;
        boolean inBottomDoor = doorDown && px + pw > doorBottom.x && px < doorBottom.x + doorBottom.width;
        boolean inLeftDoor = doorLeft && py + ph > doorLeftRect.y && py < doorLeftRect.y + doorLeftRect.height;
        boolean inRightDoor = doorRight && py + ph > doorRightRect.y && py < doorRightRect.y + doorRightRect.height;

        if (px < WALL_THICKNESS && !inLeftDoor) {
            px = WALL_THICKNESS;
        }

        if (px + pw > ROOM_WIDTH - WALL_THICKNESS && !inRightDoor) {
            px = ROOM_WIDTH - WALL_THICKNESS - pw;
        }

        if (py < WALL_THICKNESS && !inBottomDoor) {
            py = WALL_THICKNESS;
        }

        if (py + ph > ROOM_HEIGHT - WALL_THICKNESS && !inTopDoor) {
            py = ROOM_HEIGHT - WALL_THICKNESS - ph;
        }

        player.setPosition(px, py);
    }

    public void render(SpriteBatch batch) {
        renderTiles(batch);
        renderShop(batch);
        renderEnemies(batch);
        renderDoors(batch);
        renderHatch(batch);
    }

    private void renderTiles(SpriteBatch batch) {
        float doorHWidth = 64f;
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

        for (int x = 0; x < ROOM_WIDTH; x += TILE_SIZE) {
            for (int y = 0; y < ROOM_HEIGHT; y += TILE_SIZE) {
                boolean top = y + TILE_SIZE >= ROOM_HEIGHT;
                boolean bottom = y == 0;
                boolean left = x == 0;
                boolean right = x + TILE_SIZE >= ROOM_WIDTH;

                boolean isDoorGap = isDoorGap(
                    x,
                    y,
                    top,
                    bottom,
                    left,
                    right,
                    upStartX,
                    upEndX,
                    downStartX,
                    downEndX,
                    leftStartY,
                    leftEndY,
                    rightStartY,
                    rightEndY,
                    gapPadding
                );

                drawTile(batch, x, y, top, bottom, left, right, isDoorGap);
            }
        }
    }

    private boolean isDoorGap(
        int x,
        int y,
        boolean top,
        boolean bottom,
        boolean left,
        boolean right,
        float upStartX,
        float upEndX,
        float downStartX,
        float downEndX,
        float leftStartY,
        float leftEndY,
        float rightStartY,
        float rightEndY,
        float gapPadding
    ) {
        if (doorUp && top && x + TILE_SIZE > upStartX - gapPadding && x < upEndX + gapPadding) {
            return true;
        }

        if (doorDown && bottom && x + TILE_SIZE > downStartX - gapPadding && x < downEndX + gapPadding) {
            return true;
        }

        if (doorLeft && left && y + TILE_SIZE > leftStartY - gapPadding && y < leftEndY + gapPadding) {
            return true;
        }

        return doorRight && right && y + TILE_SIZE > rightStartY - gapPadding && y < rightEndY + gapPadding;
    }

    private void drawTile(SpriteBatch batch, int x, int y, boolean top, boolean bottom, boolean left, boolean right, boolean isDoorGap) {
        if (isDoorGap) {
            batch.draw(floorTex, x, y, TILE_SIZE, TILE_SIZE);
        } else if (top && left) {
            batch.draw(cornerTLTex, x, y, TILE_SIZE, TILE_SIZE);
        } else if (top && right) {
            batch.draw(cornerTRTex, x, y, TILE_SIZE, TILE_SIZE);
        } else if (bottom && left) {
            batch.draw(cornerBLTex, x, y, TILE_SIZE, TILE_SIZE);
        } else if (bottom && right) {
            batch.draw(cornerBRTex, x, y, TILE_SIZE, TILE_SIZE);
        } else if (top) {
            batch.draw(wallTopTex, x, y, TILE_SIZE, TILE_SIZE);
        } else if (bottom) {
            batch.draw(wallBottomTex, x, y, TILE_SIZE, TILE_SIZE);
        } else if (left) {
            batch.draw(wallLeftTex, x, y, TILE_SIZE, TILE_SIZE);
        } else if (right) {
            batch.draw(wallRightTex, x, y, TILE_SIZE, TILE_SIZE);
        } else {
            batch.draw(floorTex, x, y, TILE_SIZE, TILE_SIZE);
        }
    }

    private void renderShop(SpriteBatch batch) {
        if (roomType != RoomType.SHOP || shopUsed) {
            return;
        }

        if (!damageShopUsed) {
            batch.draw(shopDamageTex, damageShopZone.x, damageShopZone.y, damageShopZone.width, damageShopZone.height);
            shopFont.draw(batch, "DANO +1", damageShopZone.x + 10f, damageShopZone.y + 105f);
            shopFont.draw(batch, "8 MOEDAS", damageShopZone.x + 7f, damageShopZone.y - 8f);
        }

        if (!healShopUsed) {
            batch.draw(shopHealTex, healShopZone.x, healShopZone.y, healShopZone.width, healShopZone.height);
            shopFont.draw(batch, "CURA +20", healShopZone.x + 7f, healShopZone.y + 105f);
            shopFont.draw(batch, "5 MOEDAS", healShopZone.x + 7f, healShopZone.y - 8f);
        }
    }

    private void renderEnemies(SpriteBatch batch) {
        for (Enemy e : enemies) {
            e.render(batch);
        }
    }

    private void renderDoors(SpriteBatch batch) {
        float doorHWidth = 64f;
        float doorHHeight = 16f;

        float doorVWidth = 16f;
        float doorVHeight = 64f;

        if (doorUp) {
            batch.draw(doorUpTex, doorTop.x, doorTop.y + (doorTop.height - doorHHeight), doorHWidth, doorHHeight);
        }

        if (doorDown) {
            batch.draw(doorDownTex, doorBottom.x, doorBottom.y, doorHWidth, doorHHeight);
        }

        if (doorLeft) {
            batch.draw(doorLeftTex, doorLeftRect.x, doorLeftRect.y, doorVWidth, doorVHeight);
        }

        if (doorRight) {
            batch.draw(doorRightTex, doorRightRect.x + (doorRightRect.width - doorVWidth), doorRightRect.y, doorVWidth, doorVHeight);
        }
    }

    private void renderHatch(SpriteBatch batch) {
        if (isBoss && isCleared()) {
            batch.draw(hatchTexture, hatch.x, hatch.y, hatch.width, hatch.height);
        }

        if (roomType == RoomType.TREASURE && !rewardCollected) {
            batch.draw(treasureRewardTex, hatch.x, hatch.y, hatch.width, hatch.height);
        }
    }

    public boolean isCleared() {
        return enemies.isEmpty();
    }

    public boolean isBossCleared() {
        return isBoss && enemies.isEmpty();
    }

    public boolean isPlayerOnHatch(Player player) {
        if (!isBoss || !isCleared()) {
            return false;
        }

        return player.getBounds().overlaps(hatch);
    }

    public String checkDoor(Player player) {
        if (!isCleared()) {
            return null;
        }

        Rectangle p = player.getBounds();

        if (doorUp && p.overlaps(doorTop)) return "UP";
        if (doorDown && p.overlaps(doorBottom)) return "DOWN";
        if (doorLeft && p.overlaps(doorLeftRect)) return "LEFT";
        if (doorRight && p.overlaps(doorRightRect)) return "RIGHT";

        return null;
    }

    public boolean isBossRoomActive() {
        return isBoss && !enemies.isEmpty() && enemies.get(0) instanceof BossEnemy;
    }

    public int getBossHealth() {
        BossEnemy boss = getBoss();
        return boss == null ? 0 : boss.getHealth();
    }

    public int getBossMaxHealth() {
        BossEnemy boss = getBoss();
        return boss == null ? 0 : boss.getMaxHealth();
    }

    private BossEnemy getBoss() {
        for (Enemy e : enemies) {
            if (e instanceof BossEnemy) {
                return (BossEnemy) e;
            }
        }

        return null;
    }

    private void loadTexturesIfNeeded() {
        if (doorUpTex == null) doorUpTex = new Texture("door_up.png");
        if (doorDownTex == null) doorDownTex = new Texture("door_down.png");
        if (doorLeftTex == null) doorLeftTex = new Texture("door_left.png");
        if (doorRightTex == null) doorRightTex = new Texture("door_right.png");

        if (floorTex == null) floorTex = new Texture("floor.png");
        if (treasureRewardTex == null) treasureRewardTex = new Texture("treasure_floor.png");

        if (wallTopTex == null) wallTopTex = new Texture("wall_top.png");
        if (wallBottomTex == null) wallBottomTex = new Texture("wall_bottom.png");
        if (wallLeftTex == null) wallLeftTex = new Texture("wall_left.png");
        if (wallRightTex == null) wallRightTex = new Texture("wall_right.png");

        if (cornerTLTex == null) cornerTLTex = new Texture("corner_tl.png");
        if (cornerTRTex == null) cornerTRTex = new Texture("corner_tr.png");
        if (cornerBLTex == null) cornerBLTex = new Texture("corner_bl.png");
        if (cornerBRTex == null) cornerBRTex = new Texture("corner_br.png");

        if (hatchTexture == null) hatchTexture = new Texture("hatch.png");

        if (shopDamageTex == null) shopDamageTex = new Texture("shop_damage.png");
        if (shopHealTex == null) shopHealTex = new Texture("shop_heal.png");

        if (shopFont == null) {
            shopFont = new BitmapFont();
        }
    }

    public void dispose() {
        // vazio por enquanto
        // as texturas são static e compartilhadas entre salas
    }
}
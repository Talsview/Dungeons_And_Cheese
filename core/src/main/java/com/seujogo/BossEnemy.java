package com.seujogo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BossEnemy extends Enemy {

    // =====================================================
    // CLASSES INTERNAS
    // =====================================================

    private static class DangerZone {
        float x;
        float y;
        float radius;
        float timer;
        float explodeAt;
        float lingerTime;
        int damage;
        boolean exploded;
        boolean dealtDamage;

        DangerZone(float x, float y, float radius, float timer, float explodeAt, float lingerTime, int damage) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.timer = timer;
            this.explodeAt = explodeAt;
            this.lingerTime = lingerTime;
            this.damage = damage;
        }
    }

    private static class LaserAttack {
        float x1;
        float y1;
        float x2;
        float y2;
        float width;
        float timer;
        float telegraphTime;
        float activeTime;
        int damage;
        boolean active;
        boolean dealtDamage;

        LaserAttack(float x1, float y1, float x2, float y2, float width, float timer, float telegraphTime, float activeTime, int damage) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.width = width;
            this.timer = timer;
            this.telegraphTime = telegraphTime;
            this.activeTime = activeTime;
            this.damage = damage;
        }
    }

    // =====================================================
    // TEXTURAS
    // =====================================================

    private static Texture warningTexture;
    private static Texture pixelTexture;

    // =====================================================
    // ATRIBUTOS
    // =====================================================

    private final Random random = new Random();
    private final List<DangerZone> dangerZones = new ArrayList<>();
    private final List<LaserAttack> lasers = new ArrayList<>();

    private final int maxHealth = 110;

    private float moveTimer = 0f;
    private float strafeDirection = 1f;

    private float attackCooldown = 0.4f;
    private float telegraphTimer = 0f;
    private int queuedAttack = 0;

    private float barrageTimer = 0f;
    private float barrageInterval = 0.07f;
    private int barrageShotsLeft = 0;
    private int barragePattern = 0;
    private int barrageWaveIndex = 0;
    private float barrageBaseAngle = 0f;

    private float summonCooldown = 3.5f;
    private int pendingSummons = 0;

    private boolean spiralActive = false;
    private float spiralTimer = 0f;
    private float spiralShotTimer = 0f;
    private float spiralAngle = 0f;
    private float spiralDirection = 1f;

    private boolean enrageTriggered = false;

    // =====================================================
    // CONSTRUTOR
    // =====================================================

    public BossEnemy(float x, float y) {
        super(x, y, EnemyType.TANK);

        this.health = maxHealth;
        this.speed = 110f;
        this.touchDamage = 28;
        this.bounds = new Rectangle(x - 8, y - 8, 64, 64);

        createWarningTextureIfNeeded();
        createPixelTextureIfNeeded();
    }

    // =====================================================
    // ATUALIZAÇÃO
    // =====================================================

    @Override
    public void update(float delta, Player player) {
        triggerEnrageIfNeeded();

        float playerCenterX = player.getBounds().x + player.getBounds().width / 2f;
        float playerCenterY = player.getBounds().y + player.getBounds().height / 2f;

        float bossCenterX = x + bounds.width / 2f;
        float bossCenterY = y + bounds.height / 2f;

        float dx = playerCenterX - bossCenterX;
        float dy = playerCenterY - bossCenterY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        updateCooldowns(delta);
        updateMovement(delta, dx, dy, length);

        clampInsideRoom();
        bounds.setPosition(x - 8, y - 8);

        updateContactDamage(player);
        updateDangerZones(delta, player);
        updateLasers(delta, player);
        updateSpiral(delta, bossCenterX, bossCenterY);
        updateAttackLogic(delta, player, bossCenterX, bossCenterY, playerCenterX, playerCenterY, dx, dy, length);
        updateBullets(delta, player);
    }

    private void updateCooldowns(float delta) {
        if (contactCooldown > 0f) {
            contactCooldown -= delta;
        }

        if (summonCooldown > 0f) {
            summonCooldown -= delta;
        }
    }

    private void updateContactDamage(Player player) {
        if (bounds.overlaps(player.getBounds()) && contactCooldown <= 0f) {
            player.takeDamage(touchDamage);
            contactCooldown = contactDelay;
        }
    }

    // =====================================================
    // MOVIMENTO
    // =====================================================

    @Override
    protected void updateMovement(float delta, float dx, float dy, float length) {
        if (length == 0f) {
            return;
        }

        float targetDistance = getPhase() == 1 ? 180f : (getPhase() == 2 ? 140f : 105f);
        float currentSpeed = getPhase() == 1 ? 120f : (getPhase() == 2 ? 155f : 195f);

        if (isEnraged()) {
            currentSpeed += 35f;
        }

        speed = currentSpeed;

        moveTimer -= delta;

        if (moveTimer <= 0f) {
            moveTimer = isEnraged() ? 0.35f + random.nextFloat() * 0.2f : 0.5f + random.nextFloat() * 0.3f;
            strafeDirection = random.nextBoolean() ? 1f : -1f;
        }

        float nx = dx / length;
        float ny = dy / length;

        float px = -ny * strafeDirection;
        float py = nx * strafeDirection;

        if (length > targetDistance + 16f) {
            x += nx * speed * delta;
            y += ny * speed * delta;
        } else if (length < targetDistance - 16f) {
            x -= nx * speed * delta;
            y -= ny * speed * delta;
        }

        float strafeSpeed = speed * (isEnraged() ? 1.4f : (getPhase() == 3 ? 1.2f : 1.0f));

        x += px * strafeSpeed * delta;
        y += py * strafeSpeed * delta;
    }

    private void clampInsideRoom() {
        float minX = 20f;
        float minY = 20f;
        float maxX = 800f - bounds.width - 20f;
        float maxY = 600f - bounds.height - 20f;

        if (x < minX) x = minX;
        if (x > maxX) x = maxX;
        if (y < minY) y = minY;
        if (y > maxY) y = maxY;
    }

    // =====================================================
    // LÓGICA DE ATAQUES
    // =====================================================

    private void updateAttackLogic(
        float delta,
        Player player,
        float bossCenterX,
        float bossCenterY,
        float playerCenterX,
        float playerCenterY,
        float dx,
        float dy,
        float length
    ) {
        updateTelegraph(delta, player, bossCenterX, bossCenterY, playerCenterX, playerCenterY, dx, dy, length);
        updateBarrage(delta, playerCenterX, playerCenterY, bossCenterX, bossCenterY);

        if (telegraphTimer > 0f || barrageShotsLeft > 0 || spiralActive) {
            return;
        }

        attackCooldown -= delta;

        if (attackCooldown > 0f) {
            return;
        }

        queuedAttack = chooseAttack();
        telegraphTimer = getTelegraphTime(queuedAttack);
    }

    private void updateTelegraph(
        float delta,
        Player player,
        float bossCenterX,
        float bossCenterY,
        float playerCenterX,
        float playerCenterY,
        float dx,
        float dy,
        float length
    ) {
        if (telegraphTimer <= 0f) {
            return;
        }

        telegraphTimer -= delta;

        if (telegraphTimer <= 0f) {
            executeQueuedAttack(player, bossCenterX, bossCenterY, playerCenterX, playerCenterY, dx, dy, length);
            queuedAttack = 0;
        }
    }

    private void updateBarrage(float delta, float playerCenterX, float playerCenterY, float bossCenterX, float bossCenterY) {
        if (barrageShotsLeft <= 0) {
            return;
        }

        barrageTimer -= delta;

        if (barrageTimer <= 0f) {
            fireBarragePattern(playerCenterX, playerCenterY, bossCenterX, bossCenterY);
            barrageShotsLeft--;
            barrageWaveIndex++;
            barrageTimer = barrageInterval;
        }
    }

    private float getTelegraphTime(int queuedAttack) {
        switch (queuedAttack) {
            case 1:
                return isEnraged() ? 0.18f : 0.26f;
            case 2:
                return isEnraged() ? 0.24f : 0.35f;
            case 3:
                return isEnraged() ? 0.32f : 0.48f;
            case 4:
                return isEnraged() ? 0.25f : 0.4f;
            case 5:
                return isEnraged() ? 0.22f : 0.34f;
            case 6:
                return isEnraged() ? 0.2f : 0.3f;
            default:
                return 0.25f;
        }
    }

    private int chooseAttack() {
        int roll = random.nextInt(100);

        if (isEnraged()) {
            if (summonCooldown <= 0f && roll > 90) return 6;
            if (roll < 28) return 1;
            if (roll < 48) return 2;
            if (roll < 66) return 3;
            if (roll < 82) return 4;
            if (roll < 94) return 5;
            return 6;
        }

        int phase = getPhase();

        if (phase == 1) {
            if (roll < 52) return 1;
            if (roll < 72) return 2;
            if (roll < 86) return 3;
            return 4;
        }

        if (phase == 2) {
            if (roll < 38) return 1;
            if (roll < 56) return 2;
            if (roll < 72) return 3;
            if (roll < 88) return 4;
            if (roll < 96) return 5;
            return 6;
        }

        if (summonCooldown <= 0f && roll > 84) return 6;
        if (roll < 32) return 1;
        if (roll < 48) return 2;
        if (roll < 64) return 3;
        if (roll < 80) return 4;
        if (roll < 92) return 5;

        return 6;
    }

    private void executeQueuedAttack(
        Player player,
        float bossCenterX,
        float bossCenterY,
        float playerCenterX,
        float playerCenterY,
        float dx,
        float dy,
        float length
    ) {
        switch (queuedAttack) {
            case 1:
                startBarragePattern(playerCenterX, playerCenterY, bossCenterX, bossCenterY);
                attackCooldown = isEnraged() ? 0.18f : 0.32f;
                break;

            case 2:
                performRadialAttackWithGap(playerCenterX, playerCenterY, bossCenterX, bossCenterY);
                attackCooldown = isEnraged() ? 0.25f : 0.48f;
                break;

            case 3:
                createAoePattern(player);
                attackCooldown = isEnraged() ? 0.32f : 0.6f;
                break;

            case 4:
                startSpiral();
                attackCooldown = isEnraged() ? 0.22f : 0.42f;
                break;

            case 5:
                createLaserAttack(playerCenterX, playerCenterY, bossCenterX, bossCenterY);
                attackCooldown = isEnraged() ? 0.28f : 0.55f;
                break;

            case 6:
                executeSummonOrWallAttack();
                attackCooldown = isEnraged() ? 0.22f : 0.45f;
                break;

            default:
                attackCooldown = 0.4f;
                break;
        }
    }

    private void executeSummonOrWallAttack() {
        if (summonCooldown <= 0f) {
            pendingSummons += isEnraged() ? 3 : (getPhase() == 3 ? 2 : 1);
            summonCooldown = isEnraged() ? 4.5f : 6f;
            return;
        }

        createWallAttack();
    }

    // =====================================================
    // ATAQUE DE RAJADA
    // =====================================================

    private void startBarragePattern(float playerCenterX, float playerCenterY, float bossCenterX, float bossCenterY) {
        barrageWaveIndex = 0;
        barrageBaseAngle = angleTo(playerCenterX - bossCenterX, playerCenterY - bossCenterY);

        int phase = getPhase();
        barragePattern = random.nextInt(3);

        if (phase == 1) {
            barrageShotsLeft = 6;
            barrageInterval = 0.085f;
        } else if (phase == 2) {
            barrageShotsLeft = 8;
            barrageInterval = 0.07f;
        } else {
            barrageShotsLeft = 10;
            barrageInterval = 0.058f;
        }

        if (isEnraged()) {
            barrageShotsLeft += 1;
            barrageInterval = 0.045f;
        }

        barrageTimer = 0f;
    }

    private void fireBarragePattern(float playerCenterX, float playerCenterY, float bossCenterX, float bossCenterY) {
        float angleToPlayer = angleTo(playerCenterX - bossCenterX, playerCenterY - bossCenterY);
        float speedValue = isEnraged() ? 380f : (getPhase() == 3 ? 340f : 300f);

        switch (barragePattern) {
            case 0:
                fireStraightBurst(bossCenterX, bossCenterY, angleToPlayer, speedValue);
                break;

            case 1:
                float offset = barrageWaveIndex % 2 == 0 ? -5f : 5f;
                fireStraightBurst(bossCenterX, bossCenterY, angleToPlayer + offset, speedValue);
                break;

            case 2:
                fireSmallWallWithGap(bossCenterX, bossCenterY, angleToPlayer, speedValue);
                break;

            default:
                fireStraightBurst(bossCenterX, bossCenterY, angleToPlayer, speedValue);
                break;
        }
    }

    private void fireStraightBurst(float x, float y, float angleDeg, float speed) {
        float rad = (float) Math.toRadians(angleDeg);
        spawnBulletDir(x, y, (float) Math.cos(rad), (float) Math.sin(rad), speed);
    }

    private void fireSmallWallWithGap(float x, float y, float centerAngle, float speed) {
        fireSpreadWithGap(x, y, centerAngle, 7, 8f, 3, speed);
    }

    private void fireSpreadWithGap(
        float startX,
        float startY,
        float centerAngleDeg,
        int totalBullets,
        float spacingDeg,
        int gapIndex,
        float speedValue
    ) {
        float startAngle = centerAngleDeg - ((totalBullets - 1) * spacingDeg) / 2f;

        for (int i = 0; i < totalBullets; i++) {
            if (i == gapIndex) {
                continue;
            }

            float angleDeg = startAngle + i * spacingDeg;
            float angleRad = (float) Math.toRadians(angleDeg);

            spawnBulletDir(startX, startY, (float) Math.cos(angleRad), (float) Math.sin(angleRad), speedValue);
        }
    }

    // =====================================================
    // ATAQUE RADIAL
    // =====================================================

    private void performRadialAttackWithGap(float playerCenterX, float playerCenterY, float bossCenterX, float bossCenterY) {
        int bulletsCount = getPhase() == 1 ? 20 : (getPhase() == 2 ? 28 : 36);
        int gapSize = getPhase() == 1 ? 2 : (getPhase() == 2 ? 3 : 3);
        float speedValue = getPhase() == 1 ? 250f : (getPhase() == 2 ? 295f : 345f);

        if (isEnraged()) {
            bulletsCount = 42;
            gapSize = 3;
            speedValue = 395f;
        }

        float angleToPlayer = angleTo(playerCenterX - bossCenterX, playerCenterY - bossCenterY);
        float baseAngle = angleToPlayer + (random.nextBoolean() ? 18f : -18f);
        float step = 360f / bulletsCount;

        int gapStart = random.nextInt(bulletsCount);

        for (int i = 0; i < bulletsCount; i++) {
            if (isInsideGap(i, gapStart, gapSize, bulletsCount)) {
                continue;
            }

            float angleDeg = baseAngle + step * i;
            float angleRad = (float) Math.toRadians(angleDeg);

            spawnBulletDir(bossCenterX, bossCenterY, (float) Math.cos(angleRad), (float) Math.sin(angleRad), speedValue);
        }
    }

    private boolean isInsideGap(int index, int gapStart, int gapSize, int bulletsCount) {
        for (int g = 0; g < gapSize; g++) {
            if (index == (gapStart + g) % bulletsCount) {
                return true;
            }
        }

        return false;
    }

    // =====================================================
    // ATAQUE ESPIRAL
    // =====================================================

    private void startSpiral() {
        spiralActive = true;
        spiralTimer = isEnraged() ? 2.3f : (getPhase() == 3 ? 1.8f : 1.3f);
        spiralShotTimer = 0f;
        spiralDirection = random.nextBoolean() ? 1f : -1f;
    }

    private void updateSpiral(float delta, float bossCenterX, float bossCenterY) {
        if (!spiralActive) {
            return;
        }

        spiralTimer -= delta;
        spiralShotTimer -= delta;

        float interval = isEnraged() ? 0.035f : (getPhase() == 3 ? 0.05f : 0.07f);
        float angleStep = isEnraged() ? 18f : (getPhase() == 3 ? 15f : 12f);
        float speedValue = isEnraged() ? 355f : (getPhase() == 3 ? 315f : 270f);

        while (spiralShotTimer <= 0f) {
            fireSpiralPair(bossCenterX, bossCenterY, speedValue);
            spiralShotTimer += interval;
            spiralAngle += angleStep * spiralDirection;
        }

        if (spiralTimer <= 0f) {
            spiralActive = false;
        }
    }

    private void fireSpiralPair(float bossCenterX, float bossCenterY, float speedValue) {
        float angle1 = (float) Math.toRadians(spiralAngle);
        float angle2 = (float) Math.toRadians(spiralAngle + 180f);

        spawnBulletDir(bossCenterX, bossCenterY, (float) Math.cos(angle1), (float) Math.sin(angle1), speedValue);
        spawnBulletDir(bossCenterX, bossCenterY, (float) Math.cos(angle2), (float) Math.sin(angle2), speedValue);

        if (getPhase() >= 2 || isEnraged()) {
            float angle3 = (float) Math.toRadians(spiralAngle + 90f);
            float angle4 = (float) Math.toRadians(spiralAngle + 270f);

            spawnBulletDir(bossCenterX, bossCenterY, (float) Math.cos(angle3), (float) Math.sin(angle3), speedValue - 20f);
            spawnBulletDir(bossCenterX, bossCenterY, (float) Math.cos(angle4), (float) Math.sin(angle4), speedValue - 20f);
        }
    }

    // =====================================================
    // ATAQUE DE PAREDE
    // =====================================================

    private void createWallAttack() {
        int side = random.nextInt(4);
        int lanes = isEnraged() ? 12 : (getPhase() == 3 ? 10 : 8);
        int gapCount = isEnraged() ? 1 : 2;

        boolean[] gaps = new boolean[lanes];

        for (int g = 0; g < gapCount; g++) {
            gaps[random.nextInt(lanes)] = true;
        }

        for (int i = 0; i < lanes; i++) {
            if (gaps[i]) {
                continue;
            }

            float x;
            float y;
            float dirX;
            float dirY;
            float t = (i + 0.5f) / lanes;

            switch (side) {
                case 0:
                    x = 10f;
                    y = 40f + t * 520f;
                    dirX = 1f;
                    dirY = 0f;
                    break;

                case 1:
                    x = 790f;
                    y = 40f + t * 520f;
                    dirX = -1f;
                    dirY = 0f;
                    break;

                case 2:
                    x = 40f + t * 720f;
                    y = 590f;
                    dirX = 0f;
                    dirY = -1f;
                    break;

                default:
                    x = 40f + t * 720f;
                    y = 10f;
                    dirX = 0f;
                    dirY = 1f;
                    break;
            }

            spawnBulletDir(x, y, dirX, dirY, isEnraged() ? 300f : 255f);
        }
    }

    // =====================================================
    // ATAQUE LASER
    // =====================================================

    private void createLaserAttack(float playerCenterX, float playerCenterY, float bossCenterX, float bossCenterY) {
        float dx = playerCenterX - bossCenterX;
        float dy = playerCenterY - bossCenterY;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        if (len == 0f) {
            len = 1f;
        }

        float nx = dx / len;
        float ny = dy / len;

        float length = 1000f;
        float x2 = bossCenterX + nx * length;
        float y2 = bossCenterY + ny * length;

        lasers.add(new LaserAttack(
            bossCenterX,
            bossCenterY,
            x2,
            y2,
            isEnraged() ? 30f : 24f,
            isEnraged() ? 0.9f : 1.1f,
            isEnraged() ? 0.35f : 0.5f,
            isEnraged() ? 0.55f : 0.6f,
            isEnraged() ? 28 : 18
        ));
    }

    private void updateLasers(float delta, Player player) {
        Iterator<LaserAttack> it = lasers.iterator();

        while (it.hasNext()) {
            LaserAttack laser = it.next();
            laser.timer -= delta;

            if (!laser.active && laser.timer <= laser.activeTime) {
                laser.active = true;
            }

            if (laser.active && !laser.dealtDamage && playerIntersectsLaser(player, laser)) {
                player.takeDamage(laser.damage);
                laser.dealtDamage = true;
            }

            if (laser.timer <= 0f) {
                it.remove();
            }
        }
    }

    private boolean playerIntersectsLaser(Player player, LaserAttack laser) {
        Rectangle p = player.getBounds();

        float centerX = p.x + p.width / 2f;
        float centerY = p.y + p.height / 2f;

        float dist = distancePointToSegment(centerX, centerY, laser.x1, laser.y1, laser.x2, laser.y2);

        return dist <= laser.width;
    }

    // =====================================================
    // ATAQUE AOE
    // =====================================================

    private void createAoePattern(Player player) {
        Rectangle p = player.getBounds();

        float px = p.x + p.width / 2f;
        float py = p.y + p.height / 2f;

        dangerZones.add(new DangerZone(px, py, 58f, 0.95f, 0.18f, 0.28f, isEnraged() ? 26 : 18));

        if (getPhase() >= 2 || isEnraged()) {
            dangerZones.add(new DangerZone(px + 70f, py, 44f, 0.82f, 0.16f, 0.22f, 12));
            dangerZones.add(new DangerZone(px - 70f, py, 44f, 0.82f, 0.16f, 0.22f, 12));
            dangerZones.add(new DangerZone(px, py + 70f, 44f, 0.82f, 0.16f, 0.22f, 12));
            dangerZones.add(new DangerZone(px, py - 70f, 44f, 0.82f, 0.16f, 0.22f, 12));
        }

        if (getPhase() == 3 || isEnraged()) {
            dangerZones.add(new DangerZone(px + 105f, py + 105f, 36f, 0.74f, 0.14f, 0.18f, 10));
            dangerZones.add(new DangerZone(px - 105f, py + 105f, 36f, 0.74f, 0.14f, 0.18f, 10));
            dangerZones.add(new DangerZone(px + 105f, py - 105f, 36f, 0.74f, 0.14f, 0.18f, 10));
            dangerZones.add(new DangerZone(px - 105f, py - 105f, 36f, 0.74f, 0.14f, 0.18f, 10));
        }

        if (isEnraged()) {
            dangerZones.add(new DangerZone(120f, 300f, 52f, 0.88f, 0.18f, 0.24f, 10));
            dangerZones.add(new DangerZone(680f, 300f, 52f, 0.88f, 0.18f, 0.24f, 10));
        }
    }

    private void updateDangerZones(float delta, Player player) {
        Iterator<DangerZone> it = dangerZones.iterator();

        while (it.hasNext()) {
            DangerZone zone = it.next();
            zone.timer -= delta;

            if (!zone.exploded && zone.timer <= zone.explodeAt) {
                zone.exploded = true;

                if (!zone.dealtDamage && isPlayerInsideZone(player, zone)) {
                    player.takeDamage(zone.damage);
                    zone.dealtDamage = true;
                }
            }

            if (zone.timer <= -zone.lingerTime) {
                it.remove();
            }
        }
    }

    private boolean isPlayerInsideZone(Player player, DangerZone zone) {
        Rectangle p = player.getBounds();

        float centerX = p.x + p.width / 2f;
        float centerY = p.y + p.height / 2f;

        float dx = centerX - zone.x;
        float dy = centerY - zone.y;

        return dx * dx + dy * dy <= zone.radius * zone.radius;
    }

    // =====================================================
    // TIROS
    // =====================================================

    private void spawnBulletDir(float startX, float startY, float dirX, float dirY, float speedValue) {
        EnemyBullet bullet = new EnemyBullet(startX, startY, startX + dirX * 100f, startY + dirY * 100f);
        bullet.setSpeed(speedValue);
        bullets.add(bullet);
    }

    private void firePredictedShot(float playerCenterX, float playerCenterY, float bossCenterX, float bossCenterY) {
        float offsetX = random.nextFloat() * 18f - 9f;
        float offsetY = random.nextFloat() * 18f - 9f;

        float targetX = playerCenterX + offsetX;
        float targetY = playerCenterY + offsetY;

        EnemyBullet bullet = new EnemyBullet(bossCenterX, bossCenterY, targetX, targetY);
        bullet.setSpeed(isEnraged() ? 460f : (getPhase() == 3 ? 410f : 360f));

        bullets.add(bullet);
    }

    // =====================================================
    // RENDERIZAÇÃO
    // =====================================================

    @Override
    public void render(SpriteBatch batch) {
        renderDangerZones(batch);
        renderLasers(batch);
        renderBoss(batch);
        renderBullets(batch);
    }

    private void renderBoss(SpriteBatch batch) {
        Color oldColor = batch.getColor().cpy();

        if (telegraphTimer > 0f) {
            if (((int) (telegraphTimer * 24f)) % 2 == 0) {
                batch.setColor(Color.SCARLET);
            } else {
                batch.setColor(Color.ORANGE);
            }
        } else if (isEnraged()) {
            batch.setColor(1f, 0.25f, 0.25f, 1f);
        } else if (getPhase() == 2) {
            batch.setColor(1f, 0.88f, 0.88f, 1f);
        } else if (getPhase() == 3) {
            batch.setColor(1f, 0.65f, 0.65f, 1f);
        }

        batch.draw(texture, x, y, bounds.width, bounds.height);
        batch.setColor(oldColor);
    }

    private void renderBullets(SpriteBatch batch) {
        for (EnemyBullet b : bullets) {
            b.render(batch);
        }
    }

    private void renderDangerZones(SpriteBatch batch) {
        Color oldColor = batch.getColor().cpy();

        for (DangerZone zone : dangerZones) {
            float size = zone.radius * 2f;
            float alpha = zone.exploded ? 0.92f : 0.45f;

            if (zone.exploded) {
                batch.setColor(1f, 0.15f, 0.15f, alpha);
            } else {
                batch.setColor(1f, 0.65f, 0.1f, alpha);
            }

            batch.draw(warningTexture, zone.x - zone.radius, zone.y - zone.radius, size, size);
        }

        batch.setColor(oldColor);
    }

    private void renderLasers(SpriteBatch batch) {
        Color oldColor = batch.getColor().cpy();

        for (LaserAttack laser : lasers) {
            float dx = laser.x2 - laser.x1;
            float dy = laser.y2 - laser.y1;

            float length = (float) Math.sqrt(dx * dx + dy * dy);
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

            if (laser.active) {
                batch.setColor(1f, 0.1f, 0.1f, 0.8f);
            } else {
                batch.setColor(1f, 0.8f, 0.2f, 0.45f);
            }

            batch.draw(
                pixelTexture,
                laser.x1,
                laser.y1 - laser.width / 2f,
                0,
                laser.width / 2f,
                length,
                laser.width,
                1f,
                1f,
                angle,
                0,
                0,
                1,
                1,
                false,
                false
            );
        }

        batch.setColor(oldColor);
    }

    // =====================================================
    // ESTADO DO BOSS
    // =====================================================

    private void triggerEnrageIfNeeded() {
        if (!enrageTriggered && health <= (int) (maxHealth * 0.10f)) {
            enrageTriggered = true;
            attackCooldown = 0.1f;
            spiralActive = false;
            barrageShotsLeft = 0;
        }
    }

    public boolean isEnraged() {
        return enrageTriggered;
    }

    public int getPhase() {
        if (isEnraged()) {
            return 4;
        }

        float ratio = (float) health / (float) maxHealth;

        if (ratio > 0.66f) return 1;
        if (ratio > 0.33f) return 2;

        return 3;
    }

    @Override
    public boolean isBossLike() {
        return true;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int consumePendingSummons() {
        int amount = pendingSummons;
        pendingSummons = 0;
        return amount;
    }

    // =====================================================
    // UTILITÁRIOS
    // =====================================================

    private float angleTo(float dx, float dy) {
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    private float distancePointToSegment(float px, float py, float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;

        if (dx == 0f && dy == 0f) {
            dx = px - x1;
            dy = py - y1;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        float t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0f, Math.min(1f, t));

        float cx = x1 + t * dx;
        float cy = y1 + t * dy;

        float ddx = px - cx;
        float ddy = py - cy;

        return (float) Math.sqrt(ddx * ddx + ddy * ddy);
    }

    private void createWarningTextureIfNeeded() {
        if (warningTexture != null) {
            return;
        }

        Pixmap pixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);

        pixmap.setColor(1f, 1f, 1f, 0.18f);
        pixmap.fillCircle(64, 64, 60);

        pixmap.setColor(1f, 1f, 1f, 0.65f);
        pixmap.drawCircle(64, 64, 60);
        pixmap.drawCircle(64, 64, 59);

        warningTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    private void createPixelTextureIfNeeded() {
        if (pixelTexture != null) {
            return;
        }

        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        p.setColor(Color.WHITE);
        p.fill();

        pixelTexture = new Texture(p);
        p.dispose();
    }
}
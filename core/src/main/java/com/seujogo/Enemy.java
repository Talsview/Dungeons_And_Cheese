package com.seujogo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Enemy {

    // =====================================================
    // ENUMS
    // =====================================================

    public enum EnemyType {
        CHASER,
        SHOOTER,
        TANK,
        KAMIKAZE,
        SNIPER,
        SPLITTER,
        STRAFER
    }

    // =====================================================
    // ATRIBUTOS
    // =====================================================

    protected float x;
    protected float y;
    protected float speed;

    protected int health;
    protected int touchDamage;

    protected EnemyType type;

    protected static Texture texture;
    protected Rectangle bounds;

    protected Array<EnemyBullet> bullets = new Array<>();

    protected float shootCooldown = 0f;
    protected float shootDelay;
    protected float minDistance;

    protected float contactCooldown = 0f;
    protected float contactDelay = 0.5f;

    // =====================================================
    // CONSTRUTORES
    // =====================================================

    public Enemy(float x, float y) {
        this(x, y, EnemyType.SHOOTER);
    }

    public Enemy(float x, float y, EnemyType type) {
        this.x = x;
        this.y = y;
        this.type = type;

        loadTextureIfNeeded();
        configureByType(type);

        bounds = new Rectangle(x, y, 32, 32);
    }

    // =====================================================
    // CONFIGURAÇÃO
    // =====================================================

    /**
     * Configura atributos base do inimigo de acordo com seu tipo.
     */
    protected void configureByType(EnemyType type) {
        switch (type) {
            case CHASER:
                speed = 130f;
                health = 2;
                touchDamage = 10;
                shootDelay = 999f;
                minDistance = 0f;
                break;

            case TANK:
                speed = 55f;
                health = 6;
                touchDamage = 15;
                shootDelay = 1.8f;
                minDistance = 90f;
                break;

            case KAMIKAZE:
                speed = 190f;
                health = 1;
                touchDamage = 18;
                shootDelay = 999f;
                minDistance = 0f;
                contactDelay = 0.15f;
                break;

            case SNIPER:
                speed = 60f;
                health = 2;
                touchDamage = 4;
                shootDelay = 2.2f;
                minDistance = 250f;
                break;

            case SPLITTER:
                speed = 70f;
                health = 3;
                touchDamage = 6;
                shootDelay = 1.6f;
                minDistance = 120f;
                break;

            case STRAFER:
                speed = 90f;
                health = 3;
                touchDamage = 6;
                shootDelay = 1.3f;
                minDistance = 160f;
                break;

            case SHOOTER:
            default:
                speed = 80f;
                health = 3;
                touchDamage = 5;
                shootDelay = 1.2f;
                minDistance = 140f;
                break;
        }
    }

    private void loadTextureIfNeeded() {
        if (texture == null) {
            texture = new Texture("enemy.png");
        }
    }

    // =====================================================
    // ATUALIZAÇÃO
    // =====================================================

    public void update(float delta, Player player) {
        float playerX = player.getBounds().x;
        float playerY = player.getBounds().y;

        float dx = playerX - x;
        float dy = playerY - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        updateContactCooldown(delta);
        updateMovement(delta, dx, dy, length);

        bounds.setPosition(x, y);

        updateContactDamage(player);
        updateShooting(delta, playerX, playerY, dx, dy, length);
        updateBullets(delta, player);
    }

    private void updateContactCooldown(float delta) {
        if (contactCooldown > 0f) {
            contactCooldown -= delta;
        }
    }

    private void updateContactDamage(Player player) {
        if (bounds.overlaps(player.getBounds()) && contactCooldown <= 0f) {
            player.takeDamage(touchDamage);
            contactCooldown = contactDelay;

            if (type == EnemyType.KAMIKAZE) {
                health = 0;
            }
        }
    }

    // =====================================================
    // MOVIMENTO
    // =====================================================

    /**
     * Atualiza o movimento do inimigo com base no tipo e na distância até o jogador.
     */
    protected void updateMovement(float delta, float dx, float dy, float length) {
        if (length == 0f) {
            return;
        }

        switch (type) {
            case CHASER:
            case KAMIKAZE:
                moveTowards(delta, dx, dy, length);
                break;

            case SNIPER:
                if (length < minDistance) {
                    moveAway(delta, dx, dy, length);
                }
                break;

            case STRAFER:
                updateStraferMovement(delta, dx, dy, length);
                break;

            case TANK:
            case SHOOTER:
            case SPLITTER:
            default:
                if (length > minDistance) {
                    moveTowards(delta, dx, dy, length);
                }
                break;
        }
    }

    private void updateStraferMovement(float delta, float dx, float dy, float length) {
        if (length > minDistance + 20f) {
            moveTowards(delta, dx, dy, length);
        } else if (length < minDistance - 20f) {
            moveAway(delta, dx, dy, length);
        }
    }

    private void moveTowards(float delta, float dx, float dy, float length) {
        x += (dx / length) * speed * delta;
        y += (dy / length) * speed * delta;
    }

    private void moveAway(float delta, float dx, float dy, float length) {
        x -= (dx / length) * speed * delta;
        y -= (dy / length) * speed * delta;
    }

    // =====================================================
    // TIROS
    // =====================================================

    /**
     * Controla o disparo dos inimigos que possuem ataque à distância.
     */
    protected void updateShooting(float delta, float playerX, float playerY, float dx, float dy, float length) {
        if (type == EnemyType.CHASER || type == EnemyType.KAMIKAZE) {
            return;
        }

        if (shootCooldown > 0f) {
            shootCooldown -= delta;
        }

        if (shootCooldown <= 0f && length != 0f) {
            if (type == EnemyType.SPLITTER) {
                shootSplitterPattern(dx, dy);
            } else {
                shootSingleBullet(playerX, playerY);
            }

            shootCooldown = shootDelay;
        }
    }

    private void shootSplitterPattern(float dx, float dy) {
        float angle = (float) Math.atan2(dy, dx);

        for (int i = -1; i <= 1; i++) {
            float spread = i * 0.30f;

            float dirX = (float) Math.cos(angle + spread);
            float dirY = (float) Math.sin(angle + spread);

            EnemyBullet bullet = new EnemyBullet(
                x + 16f,
                y + 16f,
                x + 16f + dirX * 100f,
                y + 16f + dirY * 100f
            );

            bullets.add(bullet);
        }
    }

    private void shootSingleBullet(float playerX, float playerY) {
        EnemyBullet bullet = new EnemyBullet(x + 16f, y + 16f, playerX + 16f, playerY + 16f);

        if (type == EnemyType.SNIPER) {
            bullet.setSpeed(350f);
        }

        bullets.add(bullet);
    }

    /**
     * Atualiza os projéteis do inimigo e aplica dano caso atinjam o jogador.
     */
    protected void updateBullets(float delta, Player player) {
        for (int i = bullets.size - 1; i >= 0; i--) {
            EnemyBullet b = bullets.get(i);
            b.update(delta);

            if (b.getBounds().overlaps(player.getBounds())) {
                player.takeDamage(getBulletDamage());
                b.setActive(false);
            }

            if (!b.isActive()) {
                bullets.removeIndex(i);
            }
        }
    }

    private int getBulletDamage() {
        switch (type) {
            case TANK:
                return 8;

            case SNIPER:
                return 10;

            case SPLITTER:
                return 4;

            default:
                return 5;
        }
    }

    // =====================================================
    // RENDERIZAÇÃO
    // =====================================================

    public void render(SpriteBatch batch) {
        float drawSize = getDrawSize();
        float offset = (32f - drawSize) / 2f;

        batch.draw(texture, x - offset, y - offset, drawSize, drawSize);

        renderBullets(batch);
    }

    private void renderBullets(SpriteBatch batch) {
        for (EnemyBullet b : bullets) {
            b.render(batch);
        }
    }

    private float getDrawSize() {
        switch (type) {
            case TANK:
                return 40f;

            case CHASER:
                return 28f;

            case KAMIKAZE:
                return 24f;

            case SNIPER:
                return 26f;

            case SPLITTER:
                return 34f;

            case STRAFER:
                return 30f;

            case SHOOTER:
            default:
                return 32f;
        }
    }

    // =====================================================
    // VIDA E ESTADO
    // =====================================================

    public void takeDamage(int dmg) {
        health -= dmg;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean isBossLike() {
        return false;
    }

    // =====================================================
    // GETTERS E SETTERS
    // =====================================================

    public Rectangle getBounds() {
        return bounds;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHealth() {
        return health;
    }

    public EnemyType getType() {
        return type;
    }
}
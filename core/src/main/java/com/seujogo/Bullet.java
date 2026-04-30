package com.seujogo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Bullet {

    // =====================================================
    // ENUMS
    // =====================================================

    public enum Owner {
        PLAYER,
        ENEMY
    }

    public enum BulletType {
        NORMAL(420f, 1, 10, 10, 6, 6, 3f, "bullet.png", false, 0),
        FAST(700f, 1, 8, 8, 5, 5, 2f, "bullet.png", false, 0),
        HEAVY(280f, 3, 18, 18, 11, 11, 4f, "bullet.png", false, 8),
        ENEMY(330f, 1, 10, 10, 6, 6, 4f, "enemy_bullet.png", false, 0),
        BOSS(260f, 2, 20, 20, 13, 13, 7f, "boss_bullet.png", false, 4),
        PIERCING(500f, 1, 12, 12, 7, 7, 3f, "bullet.png", true, 0);

        public final float speed;
        public final int damage;
        public final float width;
        public final float height;
        public final float hitboxWidth;
        public final float hitboxHeight;
        public final float lifeTime;
        public final String texturePath;
        public final boolean piercing;
        public final float knockback;

        BulletType(
            float speed,
            int damage,
            float width,
            float height,
            float hitboxWidth,
            float hitboxHeight,
            float lifeTime,
            String texturePath,
            boolean piercing,
            float knockback
        ) {
            this.speed = speed;
            this.damage = damage;
            this.width = width;
            this.height = height;
            this.hitboxWidth = hitboxWidth;
            this.hitboxHeight = hitboxHeight;
            this.lifeTime = lifeTime;
            this.texturePath = texturePath;
            this.piercing = piercing;
            this.knockback = knockback;
        }
    }

    // =====================================================
    // TEXTURAS COMPARTILHADAS
    // =====================================================

    private static Texture normalTexture;
    private static Texture enemyTexture;
    private static Texture bossTexture;

    // =====================================================
    // ATRIBUTOS DE POSIÇÃO E MOVIMENTO
    // =====================================================

    private float x;
    private float y;
    private float dirX;
    private float dirY;
    private float speed;
    private float rotation;

    // =====================================================
    // ATRIBUTOS DE TAMANHO, HITBOX E TEMPO DE VIDA
    // =====================================================

    private float width;
    private float height;
    private float hitboxWidth;
    private float hitboxHeight;
    private float lifeTime;
    private float lifeTimer;

    // =====================================================
    // ATRIBUTOS DE COMBATE E ESTADO
    // =====================================================

    private int damage;
    private float knockback;

    private boolean active = true;
    private boolean piercing;
    private boolean rotateSprite = true;

    private Owner owner;
    private BulletType type;

    // =====================================================
    // COMPONENTES VISUAIS E COLISÃO
    // =====================================================

    private Rectangle bounds;
    private Texture texture;
    private Color color = Color.WHITE.cpy();

    // =====================================================
    // CONSTRUTORES
    // =====================================================

    public Bullet(float x, float y, float targetX, float targetY) {
        this(x, y, targetX, targetY, BulletType.NORMAL, Owner.PLAYER);
    }

    public Bullet(float x, float y, float targetX, float targetY, BulletType type, Owner owner) {
        this.x = x - type.width / 2f;
        this.y = y - type.height / 2f;
        this.type = type;
        this.owner = owner;

        this.speed = type.speed;
        this.damage = type.damage;
        this.width = type.width;
        this.height = type.height;
        this.hitboxWidth = type.hitboxWidth;
        this.hitboxHeight = type.hitboxHeight;
        this.lifeTime = type.lifeTime;
        this.piercing = type.piercing;
        this.knockback = type.knockback;

        this.bounds = new Rectangle();

        calculateDirection(targetX, targetY);
        loadTexture();
        updateBounds();
    }

    // =====================================================
    // CRIAÇÃO AUXILIAR
    // =====================================================

    public static Bullet fromDirection(float x, float y, float dirX, float dirY, BulletType type, Owner owner) {
        Bullet bullet = new Bullet(x, y, x + dirX, y + dirY, type, owner);
        bullet.setDirection(dirX, dirY);
        return bullet;
    }

    // =====================================================
    // ATUALIZAÇÃO
    // =====================================================

    public void update(float delta) {
        if (!active) {
            return;
        }

        x += dirX * speed * delta;
        y += dirY * speed * delta;

        lifeTimer += delta;

        updateBounds();
        checkLifeTime();
        checkScreenLimits();
    }

    private void checkLifeTime() {
        if (lifeTimer >= lifeTime) {
            destroy();
        }
    }

    private void checkScreenLimits() {
        if (x < -80 || x > 880 || y < -80 || y > 680) {
            destroy();
        }
    }

    // =====================================================
    // DIREÇÃO E HITBOX
    // =====================================================

    private void calculateDirection(float targetX, float targetY) {
        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        float dx = targetX - centerX;
        float dy = targetY - centerY;

        setDirection(dx, dy);
    }

    public void setDirection(float dx, float dy) {
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length == 0) {
            dirX = 1;
            dirY = 0;
        } else {
            dirX = dx / length;
            dirY = dy / length;
        }

        rotation = (float) Math.toDegrees(Math.atan2(dirY, dirX));
    }

    private void updateBounds() {
        float hitboxX = x + (width - hitboxWidth) / 2f;
        float hitboxY = y + (height - hitboxHeight) / 2f;

        bounds.set(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }

    // =====================================================
    // RENDERIZAÇÃO
    // =====================================================

    public void render(SpriteBatch batch) {
        if (!active) {
            return;
        }

        Color oldColor = batch.getColor().cpy();

        batch.setColor(color);

        if (rotateSprite) {
            renderRotated(batch);
        } else {
            batch.draw(texture, x, y, width, height);
        }

        batch.setColor(oldColor);
    }

    private void renderRotated(SpriteBatch batch) {
        batch.draw(
            texture,
            x,
            y,
            width / 2f,
            height / 2f,
            width,
            height,
            1f,
            1f,
            rotation,
            0,
            0,
            texture.getWidth(),
            texture.getHeight(),
            false,
            false
        );
    }

    // =====================================================
    // COLISÃO E ESTADO
    // =====================================================

    public void onHit() {
        if (!piercing) {
            destroy();
        }
    }

    public void destroy() {
        active = false;
    }

    public boolean canHitPlayer() {
        return owner == Owner.ENEMY;
    }

    public boolean canHitEnemy() {
        return owner == Owner.PLAYER;
    }

    // =====================================================
    // TEXTURAS
    // =====================================================

    private void loadTexture() {
        switch (type) {
            case ENEMY:
                if (enemyTexture == null) {
                    enemyTexture = new Texture("enemy_bullet.png");
                }
                texture = enemyTexture;
                break;

            case BOSS:
                if (bossTexture == null) {
                    bossTexture = new Texture("boss_bullet.png");
                }
                texture = bossTexture;
                break;

            default:
                if (normalTexture == null) {
                    normalTexture = new Texture("bullet.png");
                }
                texture = normalTexture;
                break;
        }
    }

    public static void disposeTextures() {
        if (normalTexture != null) {
            normalTexture.dispose();
            normalTexture = null;
        }

        if (enemyTexture != null) {
            enemyTexture.dispose();
            enemyTexture = null;
        }

        if (bossTexture != null) {
            bossTexture.dispose();
            bossTexture = null;
        }
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isActive() {
        return active;
    }

    public int getDamage() {
        return damage;
    }

    public Owner getOwner() {
        return owner;
    }

    public BulletType getType() {
        return type;
    }

    public float getKnockback() {
        return knockback;
    }

    public float getDirX() {
        return dirX;
    }

    public float getDirY() {
        return dirY;
    }

    // =====================================================
    // SETTERS
    // =====================================================

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        updateBounds();
    }

    public void setHitboxSize(float hitboxWidth, float hitboxHeight) {
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        updateBounds();
    }

    public void setLifeTime(float lifeTime) {
        this.lifeTime = lifeTime;
    }

    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }

    public void setRotateSprite(boolean rotateSprite) {
        this.rotateSprite = rotateSprite;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }
}
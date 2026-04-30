package com.seujogo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class EnemyBullet {

    // =====================================================
    // ATRIBUTOS
    // =====================================================

    private float x;
    private float y;

    private float speed = 180f;

    private float dirX;
    private float dirY;

    private static Texture texture;
    private Rectangle bounds;

    private boolean active = true;

    // =====================================================
    // CONSTRUTOR
    // =====================================================

    /**
     * Cria um projétil inimigo indo na direção de um alvo.
     */
    public EnemyBullet(float startX, float startY, float targetX, float targetY) {
        this.x = startX;
        this.y = startY;

        calculateDirection(startX, startY, targetX, targetY);
        loadTextureIfNeeded();

        bounds = new Rectangle(x, y, 12, 12);
    }

    // =====================================================
    // ATUALIZAÇÃO
    // =====================================================

    public void update(float delta) {
        if (!active) {
            return;
        }

        move(delta);
        updateBounds();
        checkBounds();
    }

    private void move(float delta) {
        x += dirX * speed * delta;
        y += dirY * speed * delta;
    }

    private void updateBounds() {
        bounds.setPosition(x, y);
    }

    /**
     * Desativa o projétil se sair da tela.
     */
    private void checkBounds() {
        if (x < -20 || x > 820 || y < -20 || y > 620) {
            active = false;
        }
    }

    // =====================================================
    // DIREÇÃO
    // =====================================================

    private void calculateDirection(float startX, float startY, float targetX, float targetY) {
        float dx = targetX - startX;
        float dy = targetY - startY;

        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length != 0) {
            dirX = dx / length;
            dirY = dy / length;
        }
    }

    // =====================================================
    // RENDERIZAÇÃO
    // =====================================================

    public void render(SpriteBatch batch) {
        if (!active) {
            return;
        }

        batch.draw(texture, x, y, 12, 12);
    }

    // =====================================================
    // TEXTURA
    // =====================================================

    private void loadTextureIfNeeded() {
        if (texture == null) {
            texture = new Texture("enemy_bullet.png");
        }
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
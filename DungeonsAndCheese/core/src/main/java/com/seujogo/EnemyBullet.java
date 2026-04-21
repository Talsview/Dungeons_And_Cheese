package com.seujogo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class EnemyBullet {

    private float x, y;
    private float speed = 180f;

    private float dirX, dirY;

    private static Texture texture; // 🔥 UMA SÓ PRA TODAS AS BALAS
    private Rectangle bounds;

    private boolean active = true;

    public EnemyBullet(float startX, float startY, float targetX, float targetY) {
        this.x = startX;
        this.y = startY;

        float dx = targetX - startX;
        float dy = targetY - startY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length != 0) {
            dirX = dx / length;
            dirY = dy / length;
        }

        // 🔥 carrega só uma vez
        if (texture == null) {
            texture = new Texture("enemy_bullet.png");
        }

        bounds = new Rectangle(x, y, 12, 12);
    }

    public void update(float delta) {
        x += dirX * speed * delta;
        y += dirY * speed * delta;

        bounds.setPosition(x, y);

        if (x < -20 || x > 820 || y < -20 || y > 620) {
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, 12, 12);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
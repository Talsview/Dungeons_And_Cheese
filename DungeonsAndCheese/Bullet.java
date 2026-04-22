package com.seujogo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Bullet {

    private float x, y;
    private float speed = 400f;

    private float dirX, dirY;

    private static Texture texture;
    private Rectangle bounds;

    private boolean active = true;

    public Bullet(float x, float y, float targetX, float targetY) {
        this.x = x;
        this.y = y;

        float dx = targetX - x;
        float dy = targetY - y;

        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length != 0) {
            dirX = dx / length;
            dirY = dy / length;
        } else {
            dirX = 0;
            dirY = 0;
        }

        if (texture == null) {
            texture = new Texture("bullet.png");
        }

        bounds = new Rectangle(x, y, 8, 8);
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
        batch.draw(texture, x, y, 8, 8);
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
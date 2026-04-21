package com.seujogo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Bullet {

    private float x, y;
    private float speed = 400;

    private float dirX, dirY;

    private Texture texture;
    private Rectangle bounds;

    public Bullet(float x, float y, float targetX, float targetY) {
        this.x = x;
        this.y = y;

        float dx = targetX - x;
        float dy = targetY - y;

        float length = (float) Math.sqrt(dx * dx + dy * dy);

        dirX = dx / length;
        dirY = dy / length;

        texture = new Texture("bullet.png");
        bounds = new Rectangle(x, y, 8, 8);
    }

    public void update(float delta) {
        x += dirX * speed * delta;
        y += dirY * speed * delta;

        bounds.setPosition(x, y);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, 8, 8);
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
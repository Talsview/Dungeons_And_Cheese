package com.seujogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Player {

    private float x, y;
    private float speed = 200;
    private int health = 100;

    private Texture texture;
    private Rectangle bounds;

    private Array<Bullet> bullets = new Array<>();

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
        texture = new Texture("player.png");
        bounds = new Rectangle(x, y, 32, 32);
    }

    public void update(float delta) {

        if (Gdx.input.isKeyPressed(Input.Keys.W)) y += speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) y -= speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) x -= speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) x += speed * delta;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            mouse.y = Gdx.graphics.getHeight() - mouse.y;

            bullets.add(new Bullet(x + 16, y + 16, mouse.x, mouse.y));
        }

        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);

            if (!b.isActive()) {
                bullets.removeIndex(i);
            }
        }

        bounds.setPosition(x, y);
    }

    public void clearBullets() {
        bullets.clear();
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, 32, 32);

        for (Bullet b : bullets) {
            b.render(batch);
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void takeDamage(int dmg) {
        health -= dmg;
    }

    public int getHealth() {
        return health;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        bounds.setPosition(x, y);
    }

    public Array<Bullet> getBullets() {
        return bullets;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
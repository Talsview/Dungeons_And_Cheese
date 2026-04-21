package com.seujogo;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Enemy {

    private float x, y;
    private float speed = 80;

    private int health = 3;

    private Texture texture;
    private Rectangle bounds;

    private Array<EnemyBullet> bullets = new Array<>();

    private float shootCooldown = 0f;
    private float shootDelay = 1.2f; // mais lento que o player

    private float minDistance = 140f; // distância mínima do player

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;
        texture = new Texture("enemy.png");
        bounds = new Rectangle(x, y, 32, 32);
    }

    public void update(float delta, Player player) {

        float playerX = player.getBounds().x;
        float playerY = player.getBounds().y;

        float dx = playerX - x;
        float dy = playerY - y;

        float length = (float) Math.sqrt(dx * dx + dy * dy);

        // anda só se estiver longe do player
        if (length > minDistance && length != 0) {
            x += (dx / length) * speed * delta;
            y += (dy / length) * speed * delta;
        }

        bounds.setPosition(x, y);

        // cooldown do tiro
        if (shootCooldown > 0) {
            shootCooldown -= delta;
        }

        // atira no player
        if (shootCooldown <= 0 && length != 0) {
            bullets.add(new EnemyBullet(x + 16, y + 16, playerX + 16, playerY + 16));
            shootCooldown = shootDelay;
        }

        // atualiza balas
        for (EnemyBullet b : bullets) {
            b.update(delta);

            if (b.getBounds().overlaps(player.getBounds())) {
                player.takeDamage(5);
                b.setActive(false);
            }
        }

        // remove balas inativas
        for (int i = bullets.size - 1; i >= 0; i--) {
            if (!bullets.get(i).isActive()) {
                bullets.removeIndex(i);
            }
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, 32, 32);

        for (EnemyBullet b : bullets) {
            b.render(batch);
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void takeDamage(int dmg) {
        health -= dmg;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHealth() {
        return health;
    }
}
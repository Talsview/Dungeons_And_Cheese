package com.seujogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Player {

    // =====================================================
    // ATRIBUTOS PRINCIPAIS
    // =====================================================

    private float x;
    private float y;
    private float speed = 200f;

    private int health = 100;
    private int damage = 1;

    private Texture texture;
    private Rectangle bounds;

    private Array<Bullet> bullets = new Array<>();

    // =====================================================
    // DASH
    // =====================================================

    private float dashSpeed = 760f;
    private float dashDuration = 0.14f;
    private float dashTimer = 0f;

    private float dashCooldown = 0f;
    private float dashCooldownTime = 0.55f;

    private float dashDirX = 0f;
    private float dashDirY = -1f;

    private float lastMoveX = 0f;
    private float lastMoveY = -1f;

    // =====================================================
    // INVULNERABILIDADE E FEEDBACK
    // =====================================================

    private float invulnTimer = 0f;
    private float invulnDuration = 0.5f;

    private float damageFlashTimer = 0f;
    private float damageFlashDuration = 0.18f;

    // =====================================================
    // CONSTRUTOR
    // =====================================================

    public Player(float x, float y) {
        this.x = x;
        this.y = y;

        texture = new Texture("player.png");
        bounds = new Rectangle(x, y, 32, 32);
    }

    // =====================================================
    // ATUALIZAÇÃO
    // =====================================================

    public void update(float delta) {
        updateTimers(delta);

        float moveX = getMoveX();
        float moveY = getMoveY();

        float[] normalizedMovement = normalizeMovement(moveX, moveY);
        moveX = normalizedMovement[0];
        moveY = normalizedMovement[1];

        handleDashInput();
        updateMovement(delta, moveX, moveY);
        handleShootInput();
        updateBullets(delta);

        bounds.setPosition(x, y);
    }

    private void updateTimers(float delta) {
        if (dashCooldown > 0f) {
            dashCooldown -= delta;
        }

        if (invulnTimer > 0f) {
            invulnTimer -= delta;
        }

        if (damageFlashTimer > 0f) {
            damageFlashTimer -= delta;
        }
    }

    // =====================================================
    // MOVIMENTO
    // =====================================================

    private float getMoveX() {
        float moveX = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX += 1f;

        return moveX;
    }

    private float getMoveY() {
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= 1f;

        return moveY;
    }

    private float[] normalizeMovement(float moveX, float moveY) {
        float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);

        if (length > 0f) {
            moveX /= length;
            moveY /= length;

            lastMoveX = moveX;
            lastMoveY = moveY;
        }

        return new float[]{moveX, moveY};
    }

    private void updateMovement(float delta, float moveX, float moveY) {
        if (dashTimer > 0f) {
            dashTimer -= delta;

            x += dashDirX * dashSpeed * delta;
            y += dashDirY * dashSpeed * delta;
            return;
        }

        x += moveX * speed * delta;
        y += moveY * speed * delta;
    }

    // =====================================================
    // DASH
    // =====================================================

    private void handleDashInput() {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            return;
        }

        if (dashCooldown > 0f || dashTimer > 0f) {
            return;
        }

        dashDirX = lastMoveX;
        dashDirY = lastMoveY;

        dashTimer = dashDuration;
        dashCooldown = dashCooldownTime;

        invulnTimer = Math.max(invulnTimer, dashDuration + 0.08f);
    }

    // =====================================================
    // TIROS
    // =====================================================

    private void handleShootInput() {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            return;
        }

        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        mouse.y = Gdx.graphics.getHeight() - mouse.y;

        bullets.add(new Bullet(x + 16, y + 16, mouse.x, mouse.y));
    }

    private void updateBullets(float delta) {
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);

            if (!b.isActive()) {
                bullets.removeIndex(i);
            }
        }
    }

    public void clearBullets() {
        bullets.clear();
    }

    // =====================================================
    // RENDERIZAÇÃO
    // =====================================================

    public void render(SpriteBatch batch) {
        if (shouldRenderPlayer()) {
            batch.draw(texture, x, y, 32, 32);
        }

        renderBullets(batch);
    }

    private void renderBullets(SpriteBatch batch) {
        for (Bullet b : bullets) {
            b.render(batch);
        }
    }

    private boolean shouldRenderPlayer() {
        if (invulnTimer <= 0f) {
            return true;
        }

        float blinkInterval = isDashing() ? 0.04f : 0.08f;
        int frame = (int) (invulnTimer / blinkInterval);

        return frame % 2 == 0;
    }

    // =====================================================
    // VIDA E DANO
    // =====================================================

    public void takeDamage(int dmg) {
        if (invulnTimer > 0f) {
            return;
        }

        health -= dmg;

        if (health < 0) {
            health = 0;
        }

        invulnTimer = invulnDuration;
        damageFlashTimer = damageFlashDuration;
    }

    public void heal(int amount) {
        health += amount;

        if (health > 100) {
            health = 100;
        }
    }

    public int getDamage() {
        return damage;
    }

    public void increaseDamage(int amount) {
        damage += amount;
    }

    // =====================================================
    // GETTERS E SETTERS
    // =====================================================

    public Rectangle getBounds() {
        return bounds;
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

    public boolean isInvulnerable() {
        return invulnTimer > 0f;
    }

    public boolean isDashing() {
        return dashTimer > 0f;
    }

    public float getDashCooldown() {
        return dashCooldown;
    }

    public float getDashCooldownPercent() {
        if (dashCooldown <= 0f) {
            return 1f;
        }

        return 1f - (dashCooldown / dashCooldownTime);
    }

    public float getDamageFlashTimer() {
        return damageFlashTimer;
    }

    // =====================================================
    // DISPOSE
    // =====================================================

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
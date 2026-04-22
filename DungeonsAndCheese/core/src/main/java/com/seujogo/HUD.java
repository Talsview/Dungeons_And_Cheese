package com.seujogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;

public class HUD {

    private BitmapFont font;

    private float displayedPlayerHealth = 100f;
    private float displayedBossHealth = 20f;

    private final float PLAYER_BAR_SPEED = 60f;
    private final float BOSS_BAR_SPEED = 25f;

    public HUD() {
        font = new BitmapFont();
        font.setColor(Color.WHITE);
    }

    public void render(SpriteBatch batch, Player player, GameState gameState, Room room) {

        float delta = Gdx.graphics.getDeltaTime();

        displayedPlayerHealth = animateValue(
            displayedPlayerHealth,
            player.getHealth(),
            PLAYER_BAR_SPEED,
            delta
        );

        String vidaBar = createBar(displayedPlayerHealth, 100f, 20);

        font.setColor(getHealthColor(displayedPlayerHealth, 100f));
        font.draw(
            batch,
            "Vida: " + vidaBar + " " + player.getHealth() + "/100",
            10,
            590
        );

        font.setColor(Color.WHITE);
        font.draw(batch, "Fase: " + gameState.getFloor(), 10, 560);
        font.draw(batch, "Score: " + gameState.getScore(), 10, 530);
        font.draw(batch, "Moedas: " + gameState.getCoins(), 10, 500);
        font.draw(batch, "Sala: " + getRoomTypeText(room.getRoomType()), 10, 470);

        if (room.isBossRoomActive()) {
            float bossMaxHealth = 20f;

            displayedBossHealth = animateValue(
                displayedBossHealth,
                room.getBossHealth(),
                BOSS_BAR_SPEED,
                delta
            );

            String bossBar = createBar(displayedBossHealth, bossMaxHealth, 26);

            font.setColor(getHealthColor(displayedBossHealth, bossMaxHealth));
            font.draw(
                batch,
                "BOSS: " + bossBar + " " + room.getBossHealth() + "/" + (int) bossMaxHealth,
                220,
                590
            );
        } else {
            displayedBossHealth = 20f;
        }

        font.setColor(Color.WHITE);
    }

    private float animateValue(float displayed, float target, float speed, float delta) {
        if (displayed > target) {
            displayed -= speed * delta;
            if (displayed < target) displayed = target;
        } else if (displayed < target) {
            displayed += speed * delta;
            if (displayed > target) displayed = target;
        }
        return displayed;
    }

    private String createBar(float current, float max, int size) {

    if (current < 0) current = 0;
    if (current > max) current = max;

    float percent = current / max;
    float totalFilled = percent * size;

    int fullBlocks = (int) totalFilled;
    boolean hasPartial = (totalFilled - fullBlocks) > 0.3f;

    StringBuilder bar = new StringBuilder("[");

    for (int i = 0; i < size; i++) {
        if (i < fullBlocks) {
            bar.append("#");
        } else if (i == fullBlocks && hasPartial) {
            bar.append("=");
        } else {
            bar.append("-");
        }
    }

    bar.append("]");
    return bar.toString();
}

    private Color getHealthColor(float current, float max) {
        float percent = current / max;

        if (percent > 0.6f) {
            return Color.GREEN;
        } else if (percent > 0.3f) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    private String getRoomTypeText(RoomType roomType) {
        switch (roomType) {
            case SAFE:
                return "Segura";
            case TREASURE:
                return "Tesouro";
            case BOSS:
                return "Boss";
            case NORMAL:
            default:
                return "Normal";
        }
    }

    public void dispose() {
        font.dispose();
    }
}
package com.seujogo;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;

public class HUD {

    private BitmapFont font;

    public HUD() {
        font = new BitmapFont();
        font.setColor(Color.WHITE);
    }

    public void render(SpriteBatch batch, Player player, int floor, Room room) {

        // ❤️ vida do jogador
        font.draw(batch, "Vida: " + player.getHealth(), 10, 590);

        // 🧀 fase
        font.draw(batch, "Fase: " + floor, 10, 560);

        // 👑 boss
        if (room.isBossRoomActive()) {
            font.draw(batch, "BOSS HP: " + room.getBossHealth(), 320, 590);
        }
    }

    public void dispose() {
        font.dispose();
    }
}
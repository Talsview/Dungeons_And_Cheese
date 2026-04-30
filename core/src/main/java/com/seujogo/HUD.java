package com.seujogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;

public class HUD {

    // =====================================================
    // CONSTANTES
    // =====================================================

    private final float PLAYER_BAR_SPEED = 80f;
    private final float BOSS_BAR_SPEED = 35f;

    // =====================================================
    // ATRIBUTOS
    // =====================================================

    private BitmapFont font;
    private Texture pixel;

    private float displayedPlayerHealth = 100f;
    private float displayedBossHealth = 60f;

    private final Color colorBlackBackground = new Color(0f, 0f, 0f, 0.55f);
    private final Color colorBossBackground = new Color(0f, 0f, 0f, 0.6f);
    private final Color colorHighlight = new Color(1f, 1f, 1f, 0.22f);
    private final Color colorBossHighlight = new Color(1f, 1f, 1f, 0.2f);
    private final Color colorInfoText = new Color(1f, 1f, 1f, 0.78f);
    private final Color colorBossBar = new Color(1f, 0.1f, 0.18f, 1f);
    private final Color colorBossText = new Color(1f, 0.25f, 0.3f, 1f);

    // =====================================================
    // CONSTRUTOR
    // =====================================================

    public HUD() {
        createFont();
        createPixelTexture();
    }

    // =====================================================
    // RENDERIZAÇÃO PRINCIPAL
    // =====================================================

    public void render(SpriteBatch batch, Player player, GameState gameState, Room room) {
        float delta = Gdx.graphics.getDeltaTime();

        displayedPlayerHealth = animateValue(
            displayedPlayerHealth,
            player.getHealth(),
            PLAYER_BAR_SPEED,
            delta
        );

        drawHud(batch, player, gameState, room);

        if (room.isBossRoomActive()) {
            drawBossBar(batch, room, delta);
        } else {
            displayedBossHealth = 60f;
        }

        resetColors(batch);
    }

    // =====================================================
    // HUD DO JOGADOR
    // =====================================================

    /**
     * Desenha a vida do jogador, fase, score, moedas e tipo da sala.
     */
    private void drawHud(SpriteBatch batch, Player player, GameState gameState, Room room) {
        float x = 12f;
        float y = Gdx.graphics.getHeight() - 18f;

        float hpPercent = MathUtils.clamp(displayedPlayerHealth / 100f, 0f, 1f);

        drawRect(batch, x, y, 190f, 8f, colorBlackBackground);
        drawRect(batch, x, y, 190f * hpPercent, 8f, getHealthColor(displayedPlayerHealth, 100f));
        drawRect(batch, x, y + 6f, 190f * hpPercent, 2f, colorHighlight);

        font.setColor(Color.WHITE);
        font.draw(batch, player.getHealth() + "/100", x + 198f, y + 8f);

        font.setColor(colorInfoText);
        font.draw(
            batch,
            "Fase " + gameState.getFloor()
                + "   Score " + gameState.getScore()
                + "   Moedas " + gameState.getCoins()
                + "   " + getRoomTypeText(room.getRoomType()),
            x,
            y - 8f
        );
    }

    // =====================================================
    // HUD DO BOSS
    // =====================================================

    /**
     * Desenha a barra de vida do boss quando a sala de boss está ativa.
     */
    private void drawBossBar(SpriteBatch batch, Room room, float delta) {
        float bossMaxHealth = room.getBossMaxHealth();

        displayedBossHealth = animateValue(
            displayedBossHealth,
            room.getBossHealth(),
            BOSS_BAR_SPEED,
            delta
        );

        float w = 360f;
        float h = 10f;
        float x = Gdx.graphics.getWidth() / 2f - w / 2f;
        float y = 18f;

        float percent = MathUtils.clamp(displayedBossHealth / bossMaxHealth, 0f, 1f);

        drawRect(batch, x, y, w, h, colorBossBackground);
        drawRect(batch, x, y, w * percent, h, colorBossBar);
        drawRect(batch, x, y + 7f, w * percent, 3f, colorBossHighlight);

        font.setColor(colorBossText);
        font.draw(batch, "BOSS", x + w / 2f - 18f, y + 23f);
    }

    // =====================================================
    // FORMAS E CORES
    // =====================================================

    /**
     * Desenha um retângulo usando uma textura branca de 1 pixel.
     */
    private void drawRect(SpriteBatch batch, float x, float y, float w, float h, Color color) {
        batch.setColor(color);
        batch.draw(pixel, x, y, w, h);
        batch.setColor(Color.WHITE);
    }

    private void resetColors(SpriteBatch batch) {
        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

    private Color getHealthColor(float current, float max) {
        float percent = current / max;

        if (percent > 0.6f) {
            return new Color(0.15f, 0.9f, 0.25f, 1f);
        }

        if (percent > 0.3f) {
            return new Color(1f, 0.78f, 0.1f, 1f);
        }

        return new Color(1f, 0.12f, 0.08f, 1f);
    }

    // =====================================================
    // ANIMAÇÃO
    // =====================================================

    /**
     * Suaviza a transição entre o valor exibido e o valor real.
     */
    private float animateValue(float displayed, float target, float speed, float delta) {
        if (displayed > target) {
            displayed -= speed * delta;

            if (displayed < target) {
                displayed = target;
            }
        } else if (displayed < target) {
            displayed += speed * delta;

            if (displayed > target) {
                displayed = target;
            }
        }

        return displayed;
    }

    // =====================================================
    // TEXTO
    // =====================================================

    private String getRoomTypeText(RoomType roomType) {
        switch (roomType) {
            case SAFE:
                return "SEGURA";

            case TREASURE:
                return "TESOURO";

            case BOSS:
                return "BOSS";

            case SHOP:
                return "LOJA";

            case NORMAL:
            default:
                return "NORMAL";
        }
    }

    // =====================================================
    // CRIAÇÃO DE RECURSOS
    // =====================================================

    private void createFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("PressStart2P-Regular.ttf")
        );

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 14;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        font = generator.generateFont(parameter);
        font.setColor(Color.WHITE);

        generator.dispose();
    }

    private void createPixelTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        pixel = new Texture(pixmap);
        pixmap.dispose();
    }

    // =====================================================
    // DISPOSE
    // =====================================================

    public void dispose() {
        font.dispose();
        pixel.dispose();
    }
}
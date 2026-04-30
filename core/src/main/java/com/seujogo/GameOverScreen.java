package com.seujogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameOverScreen implements Screen {

    private MainGame game;
    private SpriteBatch batch;
    private BitmapFont font;

    private int floor;

    public GameOverScreen(MainGame game, int floor) {
        this.game = game;
        this.floor = floor;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;

        // TÍTULO
        font.getData().setScale(2f);
        drawCentered("GAME OVER", centerX, centerY + 120);

        // INFO
        font.getData().setScale(1.3f);
        drawCentered("Você chegou até a fase " + floor, centerX, centerY + 40);

        // DICA
        font.getData().setScale(1f);
        drawCentered("Pressione ENTER para tentar novamente", centerX, centerY - 80);

        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game));
        }
    }

    private void drawCentered(String text, float x, float y) {
        float textWidth = font.draw(batch, text, 0, 0).width;
        font.draw(batch, text, x - textWidth / 2f, y);
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
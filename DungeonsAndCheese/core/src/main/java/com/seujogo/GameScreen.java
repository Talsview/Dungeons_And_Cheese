package com.seujogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameScreen implements Screen {

    private MainGame game;

    private SpriteBatch batch;
    private Player player;
    private Room currentRoom;
    private DungeonGenerator generator;
    private DungeonMap dungeon;

    private HUD hud;
    private Minimap minimap;

    private int floor = 1;

    // 🔥 cooldown para evitar glitch na transição
    private float doorCooldown = 0f;
    private final float DOOR_DELAY = 0.25f;

    public GameScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        player = new Player(384, 284);
        generator = new DungeonGenerator();

        dungeon = new DungeonMap(generator, floor);
        currentRoom = dungeon.getCurrentRoom();

        hud = new HUD();
        minimap = new Minimap();
    }

    private void update(float delta) {

        minimap.update();

        // 🔥 reduz cooldown da porta
        if (doorCooldown > 0) {
            doorCooldown -= delta;
        }

        player.update(delta);
        currentRoom.update(delta, player);
        currentRoom.resolveWallCollision(player);

        String door = null;

        // 🔥 só verifica porta se o cooldown acabou
        if (doorCooldown <= 0) {
            door = currentRoom.checkDoor(player);
        }

        if (door != null) {

            if (dungeon.move(door)) {
                currentRoom = dungeon.getCurrentRoom();

                // 🔥 limpa balas ao trocar de sala
                player.clearBullets();

                // 🔥 nasce mais para dentro da sala
                switch (door) {
                    case "UP":
                        player.setPosition(400, 120);
                        break;
                    case "DOWN":
                        player.setPosition(400, 420);
                        break;
                    case "LEFT":
                        player.setPosition(620, 300);
                        break;
                    case "RIGHT":
                        player.setPosition(120, 300);
                        break;
                }

                // 🔥 ativa cooldown para não trocar de novo instantaneamente
                doorCooldown = DOOR_DELAY;
            }
        }

        // 🕳️ ESCOTILHA → próxima fase
        if (currentRoom.isPlayerOnHatch(player)) {
            floor++;

            dungeon = new DungeonMap(generator, floor);
            currentRoom = dungeon.getCurrentRoom();

            player.setPosition(100, 100);
            player.clearBullets(); // 🔥 limpa balas ao trocar de fase
            doorCooldown = DOOR_DELAY;
        }

        // 💀 MORTE
        if (player.getHealth() <= 0) {
            game.setScreen(new GameOverScreen(game, floor));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        currentRoom.render(batch);
        player.render(batch);
        hud.render(batch, player, floor, currentRoom);
        batch.end();

        minimap.render(dungeon);
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        hud.dispose();
        minimap.dispose();
    }
}
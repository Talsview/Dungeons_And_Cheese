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
    private GameState gameState;

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
        gameState = new GameState();

        dungeon = new DungeonMap(generator, gameState.getFloor());
        currentRoom = dungeon.getCurrentRoom();

        hud = new HUD();
        minimap = new Minimap();
    }

    private void update(float delta) {

        minimap.update();

        if (doorCooldown > 0) {
            doorCooldown -= delta;
        }

        player.update(delta);
        currentRoom.update(delta, player, gameState);
        currentRoom.resolveWallCollision(player);

        String door = null;

        if (doorCooldown <= 0) {
            door = currentRoom.checkDoor(player);
        }

        if (door != null) {
            if (dungeon.move(door)) {
                currentRoom = dungeon.getCurrentRoom();

                player.clearBullets();

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

                doorCooldown = DOOR_DELAY;
            }
        }

        if (currentRoom.isPlayerOnHatch(player)) {
            gameState.nextFloor();

            dungeon = new DungeonMap(generator, gameState.getFloor());
            currentRoom = dungeon.getCurrentRoom();

            player.setPosition(100, 100);
            player.clearBullets();
            doorCooldown = DOOR_DELAY;
        }

        if (player.getHealth() <= 0) {
            game.setScreen(new GameOverScreen(game, gameState.getFloor()));
            return;
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        currentRoom.render(batch);
        player.render(batch);
        hud.render(batch, player, gameState, currentRoom);
        batch.end();

        minimap.render(dungeon);
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (hud != null) hud.dispose();
        if (minimap != null) minimap.dispose();
        if (player != null) player.dispose();
    }
}
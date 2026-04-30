package com.seujogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameScreen implements Screen {

    // =====================================================
    // ATRIBUTOS
    // =====================================================

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

    private float restartTimer = 0f;
    private final float RESTART_HOLD_TIME = 1.2f;

    // =====================================================
    // CONSTRUTOR
    // =====================================================

    public GameScreen(MainGame game) {
        this.game = game;
    }

    // =====================================================
    // CICLO DE VIDA DA TELA
    // =====================================================

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

    @Override
    public void render(float delta) {
        update(delta);
        clearScreen();
        renderGame();
        renderMinimap();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (hud != null) hud.dispose();
        if (minimap != null) minimap.dispose();
        if (player != null) player.dispose();
    }

    // =====================================================
    // ATUALIZAÇÃO
    // =====================================================

    private void update(float delta) {
        handleRestartInput(delta);

        minimap.update(dungeon);
        updateDoorCooldown(delta);

        player.update(delta);
        currentRoom.update(delta, player, gameState);

        updateMinimapVisibility();

        currentRoom.resolveWallCollision(player);

        handleDoorTransition();
        handleFloorTransition();
        handleGameOver();
    }

    private void updateDoorCooldown(float delta) {
        if (doorCooldown > 0f) {
            doorCooldown -= delta;
        }
    }

    private void updateMinimapVisibility() {
        if (currentRoom.getRoomType() == RoomType.SAFE || currentRoom.isCleared()) {
            minimap.setVisible(true);
        } else {
            minimap.setVisible(false);
        }
    }

    // =====================================================
    // REINICIAR ANDAR ATUAL
    // =====================================================

    private void handleRestartInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            restartTimer += delta;

            if (restartTimer >= RESTART_HOLD_TIME) {
                restartCurrentFloor();
            }
        } else {
            restartTimer = 0f;
        }
    }

    private void restartCurrentFloor() {
        int currentFloor = gameState.getFloor();

        dungeon = new DungeonMap(generator, currentFloor);
        currentRoom = dungeon.getCurrentRoom();

        player.setPosition(384, 284);
        player.clearBullets();

        doorCooldown = DOOR_DELAY;
        restartTimer = 0f;
    }

    // =====================================================
    // TRANSIÇÃO ENTRE SALAS
    // =====================================================

    private void handleDoorTransition() {
        if (doorCooldown > 0f) {
            return;
        }

        String door = currentRoom.checkDoor(player);

        if (door == null) {
            return;
        }

        if (!dungeon.move(door)) {
            return;
        }

        currentRoom = dungeon.getCurrentRoom();

        player.clearBullets();
        repositionPlayerAfterDoor(door);

        doorCooldown = DOOR_DELAY;
    }

    private void repositionPlayerAfterDoor(String door) {
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

            default:
                break;
        }
    }

    // =====================================================
    // TRANSIÇÃO DE ANDAR
    // =====================================================

    private void handleFloorTransition() {
        if (!currentRoom.isPlayerOnHatch(player)) {
            return;
        }

        gameState.nextFloor();

        dungeon = new DungeonMap(generator, gameState.getFloor());
        currentRoom = dungeon.getCurrentRoom();

        player.setPosition(100, 100);
        player.clearBullets();

        doorCooldown = DOOR_DELAY;
    }

    // =====================================================
    // GAME OVER
    // =====================================================

    private void handleGameOver() {
        if (player.getHealth() <= 0) {
            game.setScreen(new GameOverScreen(game, gameState.getFloor()));
        }
    }

    // =====================================================
    // RENDERIZAÇÃO
    // =====================================================

    private void clearScreen() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void renderGame() {
        batch.begin();

        currentRoom.render(batch);
        player.render(batch);
        hud.render(batch, player, gameState, currentRoom);

        batch.end();
    }

    private void renderMinimap() {
        minimap.render(dungeon);
    }
}
package com.seujogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Minimap {

    private ShapeRenderer shape;
    private boolean expanded = false;
    private boolean visible = true;

    private float pulseTimer = 0f;
    private float openAnimation = 0f;

    public Minimap() {
        shape = new ShapeRenderer();
    }

    public void update(DungeonMap dungeon) {
        float delta = Gdx.graphics.getDeltaTime();

        pulseTimer += delta * 4f;

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            expanded = !expanded;
        }

        updateOpenAnimation(delta);
    }

    private void updateOpenAnimation(float delta) {
        if (expanded) {
            openAnimation += delta * 5f;

            if (openAnimation > 1f) {
                openAnimation = 1f;
            }
        } else {
            openAnimation -= delta * 5f;

            if (openAnimation < 0f) {
                openAnimation = 0f;
            }
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void render(DungeonMap dungeon) {
        if (!visible) {
            return;
        }

        enableBlend();

        if (expanded || openAnimation > 0f) {
            renderExpanded(dungeon);
        } else {
            renderMini(dungeon);
        }

        disableBlend();
    }

    private void renderMini(DungeonMap dungeon) {
        int mapWidth = 160;
        int mapHeight = 160;

        int offsetX = Gdx.graphics.getWidth() - mapWidth - 20;
        int offsetY = Gdx.graphics.getHeight() - mapHeight - 20;

        drawPanel(offsetX, offsetY, mapWidth, mapHeight, 0.65f);

        int centerX = offsetX + mapWidth / 2;
        int centerY = offsetY + mapHeight / 2;

        drawMap(dungeon, 14, centerX, centerY, true);
        drawBorder(offsetX, offsetY, mapWidth, mapHeight, Color.WHITE);
    }

    private void renderExpanded(DungeonMap dungeon) {
        float alpha = openAnimation;

        drawExpandedBackground(alpha);

        int panelWidth = 520;
        int panelHeight = 420;

        int offsetX = Gdx.graphics.getWidth() / 2 - panelWidth / 2;
        int offsetY = Gdx.graphics.getHeight() / 2 - panelHeight / 2;

        drawPanel(offsetX, offsetY, panelWidth, panelHeight, 0.85f * alpha);

        int centerX = Gdx.graphics.getWidth() / 2;
        int centerY = Gdx.graphics.getHeight() / 2;

        int size = (int) (28 * openAnimation);

        if (size < 18) {
            size = 18;
        }

        drawMap(dungeon, size, centerX, centerY, true);
        drawBorder(offsetX, offsetY, panelWidth, panelHeight, Color.WHITE);
    }

    private void drawExpandedBackground(float alpha) {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.78f * alpha);
        shape.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shape.end();
    }

    private void drawPanel(int x, int y, int width, int height, float alpha) {
        shape.begin(ShapeRenderer.ShapeType.Filled);

        shape.setColor(0.02f, 0.02f, 0.04f, alpha);
        shape.rect(x, y, width, height);

        shape.setColor(0.10f, 0.10f, 0.14f, alpha);
        shape.rect(x + 4, y + 4, width - 8, height - 8);

        shape.end();
    }

    private void drawBorder(int x, int y, int width, int height, Color color) {
        shape.begin(ShapeRenderer.ShapeType.Line);

        shape.setColor(color);
        shape.rect(x, y, width, height);
        shape.rect(x + 4, y + 4, width - 8, height - 8);

        shape.end();
    }

    private void drawMap(DungeonMap dungeon, int size, int centerX, int centerY, boolean fogEnabled) {
        int currentX = dungeon.getCurrentX();
        int currentY = dungeon.getCurrentY();

        drawConnections(dungeon, size, centerX, centerY, currentX, currentY);
        drawRooms(dungeon, size, centerX, centerY, currentX, currentY, fogEnabled);
    }

    private void drawConnections(DungeonMap dungeon, int size, int centerX, int centerY, int currentX, int currentY) {
        shape.begin(ShapeRenderer.ShapeType.Filled);

        for (String key : dungeon.getRooms().keySet()) {
            int[] position = parseKey(key);
            int x = position[0];
            int y = position[1];

            Room room = dungeon.getRooms().get(key);

            boolean visible = dungeon.isVisited(x, y) || isDiscovered(dungeon, x, y);

            if (!visible) {
                continue;
            }

            int drawX = centerX + (x - currentX) * size;
            int drawY = centerY + (y - currentY) * size;

            shape.setColor(0.55f, 0.55f, 0.65f, dungeon.isVisited(x, y) ? 0.9f : 0.35f);

            drawDoorConnections(dungeon, room, x, y, drawX, drawY, size);
        }

        shape.end();
    }

    private void drawDoorConnections(DungeonMap dungeon, Room room, int mapX, int mapY, int drawX, int drawY, int size) {
        int lineThickness = Math.max(2, size / 7);
        float halfSize = size / 2f;

        if (room.hasDoorUp() && isConnectionVisible(dungeon, mapX, mapY + 1)) {
            shape.rect(drawX - lineThickness / 2f, drawY, lineThickness, halfSize);
        }

        if (room.hasDoorDown() && isConnectionVisible(dungeon, mapX, mapY - 1)) {
            shape.rect(drawX - lineThickness / 2f, drawY - halfSize, lineThickness, halfSize);
        }

        if (room.hasDoorLeft() && isConnectionVisible(dungeon, mapX - 1, mapY)) {
            shape.rect(drawX - halfSize, drawY - lineThickness / 2f, halfSize, lineThickness);
        }

        if (room.hasDoorRight() && isConnectionVisible(dungeon, mapX + 1, mapY)) {
            shape.rect(drawX, drawY - lineThickness / 2f, halfSize, lineThickness);
        }
    }

    private void drawRooms(DungeonMap dungeon, int size, int centerX, int centerY, int currentX, int currentY, boolean fogEnabled) {
        shape.begin(ShapeRenderer.ShapeType.Filled);

        for (String key : dungeon.getRooms().keySet()) {
            int[] position = parseKey(key);
            int x = position[0];
            int y = position[1];

            Room room = dungeon.getRooms().get(key);

            boolean visited = dungeon.isVisited(x, y);
            boolean discovered = isDiscovered(dungeon, x, y);

            if (fogEnabled && !visited && !discovered) {
                continue;
            }

            int drawX = centerX + (x - currentX) * size;
            int drawY = centerY + (y - currentY) * size;

            drawRoom(room, x, y, drawX, drawY, size, visited, currentX, currentY);
        }

        shape.end();

        drawRoomOutlines(dungeon, size, centerX, centerY, currentX, currentY, fogEnabled);
    }

    private void drawRoom(Room room, int x, int y, int drawX, int drawY, int size, boolean visited, int currentX, int currentY) {
        if (!visited) {
            shape.setColor(0.22f, 0.22f, 0.26f, 0.85f);
        } else {
            shape.setColor(getRoomColor(room.getRoomType()));
        }

        float roomSize = size * 0.72f;
        float rx = drawX - roomSize / 2f;
        float ry = drawY - roomSize / 2f;

        shape.rect(rx, ry, roomSize, roomSize);

        if (x == currentX && y == currentY) {
            drawCurrentRoomMarker(drawX, drawY, roomSize, size);
        }

        if (visited) {
            drawRoomIcon(room.getRoomType(), drawX, drawY, size);
        }
    }

    private void drawCurrentRoomMarker(int drawX, int drawY, float roomSize, int size) {
        float pulse = 1f + (float) Math.sin(pulseTimer) * 0.18f;
        float currentSize = roomSize * pulse;

        shape.setColor(0.2f, 0.65f, 1f, 0.45f);
        shape.rect(drawX - currentSize / 2f, drawY - currentSize / 2f, currentSize, currentSize);

        shape.setColor(0.1f, 0.55f, 1f, 1f);
        shape.circle(drawX, drawY, Math.max(4f, size * 0.22f));
    }

    private void drawRoomOutlines(DungeonMap dungeon, int size, int centerX, int centerY, int currentX, int currentY, boolean fogEnabled) {
        shape.begin(ShapeRenderer.ShapeType.Line);

        for (String key : dungeon.getRooms().keySet()) {
            int[] position = parseKey(key);
            int x = position[0];
            int y = position[1];

            boolean visited = dungeon.isVisited(x, y);
            boolean discovered = isDiscovered(dungeon, x, y);

            if (fogEnabled && !visited && !discovered) {
                continue;
            }

            int drawX = centerX + (x - currentX) * size;
            int drawY = centerY + (y - currentY) * size;

            float roomSize = size * 0.72f;
            float rx = drawX - roomSize / 2f;
            float ry = drawY - roomSize / 2f;

            setRoomOutlineColor(x, y, currentX, currentY, visited);
            shape.rect(rx, ry, roomSize, roomSize);
        }

        shape.end();
    }

    private void setRoomOutlineColor(int x, int y, int currentX, int currentY, boolean visited) {
        if (x == currentX && y == currentY) {
            shape.setColor(0.35f, 0.8f, 1f, 1f);
        } else if (visited) {
            shape.setColor(1f, 1f, 1f, 0.85f);
        } else {
            shape.setColor(0.65f, 0.65f, 0.7f, 0.45f);
        }
    }

    private boolean isDiscovered(DungeonMap dungeon, int x, int y) {
        return dungeon.isVisited(x, y)
            || dungeon.isVisited(x + 1, y)
            || dungeon.isVisited(x - 1, y)
            || dungeon.isVisited(x, y + 1)
            || dungeon.isVisited(x, y - 1);
    }

    private boolean isConnectionVisible(DungeonMap dungeon, int x, int y) {
        String key = x + "," + y;

        if (!dungeon.getRooms().containsKey(key)) {
            return false;
        }

        return dungeon.isVisited(x, y) || isDiscovered(dungeon, x, y);
    }

    private Color getRoomColor(RoomType type) {
        switch (type) {
            case SAFE:
                return new Color(0.15f, 0.8f, 0.35f, 1f);

            case TREASURE:
                return new Color(1f, 0.85f, 0.15f, 1f);

            case BOSS:
                return new Color(0.9f, 0.12f, 0.12f, 1f);

            case SHOP:
                return new Color(0.75f, 0.25f, 1f, 1f);

            case NORMAL:
            default:
                return new Color(0.82f, 0.82f, 0.9f, 1f);
        }
    }

    private void drawRoomIcon(RoomType type, float x, float y, int size) {
        float iconSize = Math.max(3f, size * 0.18f);

        switch (type) {
            case SAFE:
                drawSafeIcon(x, y, iconSize);
                break;

            case TREASURE:
                drawTreasureIcon(x, y, iconSize);
                break;

            case BOSS:
                drawBossIcon(x, y, iconSize);
                break;

            case SHOP:
                drawShopIcon(x, y, iconSize);
                break;

            case NORMAL:
            default:
                break;
        }
    }

    private void drawSafeIcon(float x, float y, float iconSize) {
        shape.setColor(0f, 0.25f, 0.05f, 1f);
        shape.rect(x - iconSize / 2f, y - iconSize * 1.5f, iconSize, iconSize * 3f);
        shape.rect(x - iconSize * 1.5f, y - iconSize / 2f, iconSize * 3f, iconSize);
    }

    private void drawTreasureIcon(float x, float y, float iconSize) {
        shape.setColor(0.55f, 0.35f, 0f, 1f);
        shape.circle(x, y, iconSize * 1.2f);
    }

    private void drawBossIcon(float x, float y, float iconSize) {
        shape.setColor(0.35f, 0f, 0f, 1f);
        shape.triangle(
            x, y + iconSize * 1.8f,
            x - iconSize * 1.8f, y - iconSize,
            x + iconSize * 1.8f, y - iconSize
        );
    }

    private void drawShopIcon(float x, float y, float iconSize) {
        shape.setColor(0.25f, 0f, 0.35f, 1f);
        shape.rect(x - iconSize * 1.3f, y - iconSize, iconSize * 2.6f, iconSize * 2f);
    }

    private void enableBlend() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void disableBlend() {
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private int[] parseKey(String key) {
        String[] p = key.split(",");

        return new int[]{
            Integer.parseInt(p[0]),
            Integer.parseInt(p[1])
        };
    }

    public void dispose() {
        shape.dispose();
    }
}
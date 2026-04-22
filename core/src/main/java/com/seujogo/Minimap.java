package com.seujogo;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Minimap {

    private ShapeRenderer shape;
    private boolean expanded = false;

    public Minimap() {
        shape = new ShapeRenderer();
    }

    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            expanded = !expanded;
        }
    }

    public void render(DungeonMap dungeon) {
        if (expanded) {
            renderExpanded(dungeon);
        } else {
            renderMini(dungeon);
        }
    }

    private void renderMini(DungeonMap dungeon) {
        int size = 10;

        int mapWidth = 150;
        int mapHeight = 150;

        int offsetX = Gdx.graphics.getWidth() - mapWidth - 20;
        int offsetY = Gdx.graphics.getHeight() - mapHeight - 20;

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0, 0, 0, 0.5f);
        shape.rect(offsetX, offsetY, mapWidth, mapHeight);
        shape.end();

        drawMapCentered(dungeon, size, offsetX + mapWidth / 2, offsetY + mapHeight / 2, true);

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.WHITE);
        shape.rect(offsetX, offsetY, mapWidth, mapHeight);
        shape.end();
    }

    private void renderExpanded(DungeonMap dungeon) {
        int size = 20;

        int centerX = Gdx.graphics.getWidth() / 2;
        int centerY = Gdx.graphics.getHeight() / 2;

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0, 0, 0, 0.7f);
        shape.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shape.end();

        drawMapCentered(dungeon, size, centerX, centerY, true);
    }

    private void drawMapCentered(DungeonMap dungeon, int size, int centerX, int centerY, boolean fogEnabled) {
        shape.begin(ShapeRenderer.ShapeType.Filled);

        int currentX = dungeon.getCurrentX();
        int currentY = dungeon.getCurrentY();

        for (String key : dungeon.getRooms().keySet()) {
            String[] p = key.split(",");
            int x = Integer.parseInt(p[0]);
            int y = Integer.parseInt(p[1]);

            int drawX = centerX + (x - currentX) * size;
            int drawY = centerY + (y - currentY) * size;

            boolean visited = dungeon.isVisited(x, y);

            if (!visited && fogEnabled) {
                shape.setColor(0.2f, 0.2f, 0.2f, 0.85f);
            } else if (x == currentX && y == currentY) {
                shape.setColor(Color.BLUE);
            } else if (x == 0 && y == 0) {
                shape.setColor(Color.GREEN);
            } else {
                shape.setColor(Color.WHITE);
            }

            shape.rect(drawX, drawY, size, size);
        }

        shape.end();
    }

    public void dispose() {
        shape.dispose();
    }
}
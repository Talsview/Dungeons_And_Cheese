package com.seujogo.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.seujogo.MainGame;

public class Lwjgl3Launcher {

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return;
        new Lwjgl3Application(new MainGame(), getConfig());
    }

    private static Lwjgl3ApplicationConfiguration getConfig() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        config.setTitle("Dungeons & Cheese");
        config.setWindowedMode(800, 600);
        config.useVsync(true);

        return config;
    }
}
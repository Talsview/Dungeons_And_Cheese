package com.seujogo;

import com.badlogic.gdx.Game;

/**
 * Classe principal do jogo.
 * Responsável por iniciar e gerenciar as telas (Screens).
 */
public class MainGame extends Game {

    // =====================================================
    // CICLO DE VIDA
    // =====================================================

    @Override
    public void create() {
        // Define a primeira tela do jogo
        setScreen(new GameScreen(this));
    }
}
package gamelogic.gamerenderer.impl;

import gamelogic.controllers.GameController;
import gamelogic.entities.worm.Worm;
import gamelogic.gamerenderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.List;

public class GameRendererImpl implements GameRenderer {
    
    private final int shaderProgram;
    private final int blockVertexArray;
    private final GameController gameController;
    private final List<Worm> worms;
    
    public GameRendererImpl(int shaderProgram, int blockVertexArray, GameController gameController, List<Worm> worms) {
        this.shaderProgram = shaderProgram;
        this.blockVertexArray = blockVertexArray;
        this.gameController = gameController;
        this.worms = worms;
    }
    
    @Override
    public void render() {
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL20.glUseProgram(shaderProgram);
        GL30.glBindVertexArray(blockVertexArray);
        for (Worm worm : worms) {
            gameController.drawWorm(worm);
        }
        gameController.drawFood();
        gameController.drawBorders();
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
    }
}

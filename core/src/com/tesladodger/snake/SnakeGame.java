package com.tesladodger.snake;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Random;

import com.tesladodger.snake.ai.AStar;
import com.tesladodger.snake.managers.FileManager;
import com.tesladodger.snake.objects.Food;
import com.tesladodger.snake.objects.Snake;


public class SnakeGame extends ApplicationAdapter {

    private Random ran = new Random();

    private Snake snake;
    private Food food;

    private AStar aStar;
    private Deque<Integer> moveQueue;
    private int move;

    private ShapeRenderer shapeRenderer;

    private boolean buttonPressed;
    private boolean ateTail;

    private boolean aiMode;

    private FileManager fileManager;
    private int delay;
    private boolean showFPS;

    private Stage stage;
    private FreeTypeFontGenerator ftfg;
    private Label scoreLabel;
    private StringBuilder strB;

    private long startTime;


    @Override
    public void create() {

        shapeRenderer = new ShapeRenderer();

        // Initialize the snake and food objects.
        int dir = ran.nextInt(2);          //  0 or 1
        int mag = ran.nextInt(2) * 2 - 1;  // -1 or 1
        int raX = ran.nextInt(11) + 15;    // 15 to 25
        int raY = ran.nextInt(11) + 10;    // 10 to 20
        snake = new Snake(dir, mag, raX, raY);
        // noinspection Convert2Diamond
        snake.tail = new ArrayList<Integer>();
        snake.tail.add(snake.x);
        snake.tail.add(snake.y);
        snake.justAte = false;
        ateTail = false;

        food = new Food();
        food.reset(snake.tail);

        /*  Boolean to avoid clicking twice before the counter to move is up,  *
         * preventing the player from turning the snake back on itself.        *
         *  It is set true upon click and reset after the snake moves.         */
        buttonPressed = false;

        // Initialize AI stuff.
        aiMode = false;
        aStar = new AStar();
        //noinspection Convert2Diamond
        moveQueue = new ArrayDeque<Integer>();  // Used as FIFO.
        move = 0;

        // Read or create the config file.
        fileManager = new FileManager();
        fileManager.setup();
        delay = fileManager.getUserDelay();
        int fontSize = fileManager.getFontSize();
        showFPS = fileManager.getShowFPS();

        // Set the stage.
        ftfg = new FreeTypeFontGenerator(Gdx.files.internal("Hack-Bold.ttf"));
        FreeTypeFontParameter ftfp = new FreeTypeFontParameter();
        ftfp.size = fontSize;
        BitmapFont font = ftfg.generateFont(ftfp);
        scoreLabel = new Label("Send Nudes", new Label.LabelStyle(font, Color.GRAY));
        strB = new StringBuilder();
        stage = new Stage();
        updateScore();
        stage.addActor(scoreLabel);

        // Start the chronometer.
        startTime = System.currentTimeMillis();
    }


    @Override
    public void render() {
        Gdx.graphics.setWindowedMode(640, 480);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.draw();

        // Render the snake.
        for (int i = 0; i < snake.tail.size(); i += 2) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(snake.tail.get(i), snake.tail.get(i+1), snake.side, snake.side);
            shapeRenderer.end();
        }

        // Render the food.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(food.x, food.y, snake.side, snake.side);
        shapeRenderer.end();

        // Check user input.
        if (Gdx.input.isKeyJustPressed(Keys.DPAD_LEFT) && snake.moveX == 0 && !buttonPressed && !aiMode) {
            snake.moveX = - snake.side;
            snake.moveY = 0;
            buttonPressed = true;
        }
        else if (Gdx.input.isKeyJustPressed(Keys.DPAD_RIGHT) && snake.moveX == 0 && !buttonPressed && !aiMode) {
            snake.moveX = snake.side;
            snake.moveY = 0;
            buttonPressed = true;
        }
        else if (Gdx.input.isKeyJustPressed(Keys.DPAD_UP) && snake.moveY == 0 && !buttonPressed && !aiMode) {
            snake.moveX = 0;
            snake.moveY = snake.side;
            buttonPressed = true;
        }
        else if (Gdx.input.isKeyJustPressed(Keys.DPAD_DOWN) && snake.moveY == 0 && !buttonPressed && !aiMode) {
            snake.moveX = 0;
            snake.moveY = - snake.side;
            buttonPressed = true;
        }
        else if (Gdx.input.isKeyJustPressed(Keys.A)) {
            aiMode = !aiMode;
            if (aiMode) delay = fileManager.getAiDelay();
            else delay = fileManager.getUserDelay();
            moveQueue.clear();
        }


        // Move at 10 fps for the ultimate gaming experience.
        if (System.currentTimeMillis() - startTime >= delay) {

            // Control AI movement.
            if (aiMode) {
                if (moveQueue.isEmpty()) {
                    //long t = System.currentTimeMillis();
                    moveQueue = aStar.algorithm(food.x, food.y, snake.tail, false);
                    //System.out.println("Calc time: " + (System.currentTimeMillis() - t));
                }
                if (moveQueue.isEmpty()) {
                    System.out.println("Shit");
                    moveQueue = aStar.algorithm(snake.x, snake.y, snake.tail, true);
                    System.out.println(moveQueue.size());
                }
                if (!moveQueue.isEmpty()) {
                    move = moveQueue.removeFirst();
                }
                if (move == 0 && snake.moveX == 0) {
                    snake.moveX = snake.side;
                    snake.moveY = 0;
                }
                else if (move == 1 && snake.moveY == 0) {
                    snake.moveX = 0;
                    snake.moveY = snake.side;
                }
                else if (move == 2 && snake.moveX == 0) {
                    snake.moveX = - snake.side;
                    snake.moveY = 0;
                }
                else if (move == 3 && snake.moveY == 0) {
                    snake.moveX = 0;
                    snake.moveY = - snake.side;
                }
            }

            snake.move();

            buttonPressed = false;

            // Reset the timer.
            startTime = System.currentTimeMillis();

            // Eating the food.
            if (snake.x == food.x && snake.y == food.y) {
                snake.justAte = true;  // This only has effect in the next move.
                food.reset(snake.tail);
            }

            updateScore();

            // Eating the tail.
            ateTail = snake.checkAteTail();
            // Update the high score.
            if (ateTail) {
                fileManager.update(snake.tail.size() / 2 - 1);
                snake.restart();
            }

        } // End of the delay.

    } // End of the render loop.


    private void updateScore() {
        strB.setLength(0);
        strB.append("High Score: ").append(fileManager.getHs());
        strB.append(" | Current: ").append((snake.tail.size() / 2) - 1);
        strB.append(" | Average: ").append(fileManager.getHsAve());
        strB.append(" | Games: ").append(fileManager.getNumGames());
        if (showFPS) {
            strB.append(" | FPS: ").append(Gdx.graphics.getFramesPerSecond());
        }
        if (aiMode) {
            strB.append(" | AI ON");
        }
        else {
            strB.append(" | Press A to toggle AI");
        }
        scoreLabel.setText(strB);
    }


    @Override
    public void resize(int x, int y) {

    }


    @Override
    public void dispose () {
        shapeRenderer.dispose();
        stage.dispose();
        ftfg.dispose();
    }

}

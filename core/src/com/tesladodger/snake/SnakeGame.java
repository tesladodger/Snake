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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;



public class SnakeGame extends ApplicationAdapter {

    private Random ran = new Random();

    private class Snake {
        private int side = Gdx.graphics.getHeight() / 30;
        private int x = (ran.nextInt(11) + 15) * this.side;  // Start with random position in the middle,
        private int y = (ran.nextInt(11) + 10) * this.side;  // from 10 to 20 on the grid.
        private int moveX;
        private int moveY;
        Snake(int direction, int magnitude) {  // Start with a random direction
            if (direction == 0) {
                this.moveX = magnitude * this.side;
            } else {
                this.moveY = magnitude * this.side;
            }
        }

        private boolean justAte;
        private List<Integer> tail;

        private void move() {
            x = x + moveX;
            y = y + moveY;
            buttonPressed = false;

            if (justAte){        // If it just ate,
                tail.add(x);     // Just add the new head to the end of the list,
                tail.add(y);
                justAte = false;
            }
            else {
                for (int i = 0; i < tail.size() - 2; i++) {
                    tail.set(i, tail.get(i + 2)); // otherwise move every coordinate two places to the front,
                }
                tail.set(tail.size() - 2, x);     // and overwrite the new head to the end.
                tail.set(tail.size() - 1, y);
            }
        }
    }

    private class Food {
        private int side = snake.side;
        private int x;
        private int y;

        private int getLocation(int bound) {
            return ran.nextInt(bound) * food.side;
        }
    }

    private Snake snake;
    private Food food;

    private ShapeRenderer shapeRenderer;

    private boolean buttonPressed;

    // From the config file
    private int hs;
    private int delay;
    private int fontSize;
    private boolean showFPS;

    private Stage stage;
    private FreeTypeFontGenerator ftfg;
    private Label scoreLabel;
    private StringBuilder strB;

    private long startTime;

    @Override
    public void create() {

        shapeRenderer = new ShapeRenderer();

        int dir = ran.nextInt(2);         //  0 or 1
        int mag = ran.nextInt(2) * 2 - 1; // -1 or 1
        snake      = new Snake(dir, mag);
        snake.tail = new ArrayList<Integer>();
        snake.tail.add(snake.x);
        snake.tail.add(snake.y);
        snake.justAte = false;

        food   = new Food();
        food.x = food.getLocation(40);
        food.y = food.getLocation(30);

        buttonPressed = false;  // This is to avoid clicking twice before the counter to move is up,
        // making it impossible to turn the snake back on itself.

        boolean fileExists = new File("snake.conf").isFile();
        if (!fileExists) {
            try {
                PrintWriter pr = new PrintWriter("snake.conf", "UTF-8");
                pr.print("0 ");
                pr.print("100 ");
                pr.print("12 ");
                pr.print("false");
                pr.close();
            }
            catch (IOException e) {
                System.err.println("Caught IOException: " + e.getMessage());
            }
            hs = 0; delay = 100; fontSize = 12; showFPS = false;  // The file can't be read if it doesn't exist...
        }
        else {
            try {
                Scanner fs = new Scanner(new File("snake.conf"));
                String[] line = fs.nextLine().split(" ");
                hs = Integer.parseInt(line[0]);
                delay = Integer.parseInt(line[1]);
                fontSize = Integer.parseInt(line[2]);
                showFPS = Boolean.parseBoolean(line[3]);

                fs.close();
            }
            catch (IOException e) {
                System.err.println("Caught IOException: " + e.getMessage());
            }
            catch (NumberFormatException e) {
                System.err.println("Caught NumberFormatException: " + e.getMessage());
            }
        }

        ftfg = new FreeTypeFontGenerator(Gdx.files.internal("Hack-Bold.ttf"));
        FreeTypeFontParameter ftfp = new FreeTypeFontParameter();
        ftfp.size = fontSize;
        BitmapFont font = ftfg.generateFont(ftfp);
        scoreLabel = new Label("Send Nudes", new Label.LabelStyle(font, Color.GRAY));
        strB       = new StringBuilder();
        stage      = new Stage();
        scoreLabel = updateScore();
        stage.addActor(scoreLabel);

        startTime = System.currentTimeMillis();
    }

    @Override
    public void render() {
        Gdx.graphics.setWindowedMode(640, 480);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.draw();

        // Render the snake
        for (int i = 0; i <= snake.tail.size() -1; i = i + 2) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(snake.tail.get(i), snake.tail.get(i + 1), snake.side, snake.side);
            shapeRenderer.end();
        }

        // Render the food
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(food.x, food.y, food.side, food.side);
        shapeRenderer.end();

        // Check user input
        if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT) && snake.moveX == 0 && !buttonPressed) {
            snake.moveX = - snake.side;
            snake.moveY = 0;
            buttonPressed = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT) && snake.moveX == 0 && !buttonPressed) {
            snake.moveX = snake.side;
            snake.moveY = 0;
            buttonPressed = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.DPAD_UP) && snake.moveY == 0 && !buttonPressed) {
            snake.moveX = 0;
            snake.moveY = snake.side;
            buttonPressed = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.DPAD_DOWN) && snake.moveY == 0 && !buttonPressed) {
            snake.moveX = 0;
            snake.moveY = - snake.side;
            buttonPressed = true;
        }

        // Move at 10 fps for the ultimate gaming experience
        if (System.currentTimeMillis() -  startTime >= delay) {

            snake.move();

            // Check out of bounds and move accordingly
            int wH = Gdx.graphics.getHeight();
            int wW = Gdx.graphics.getWidth();
            if (snake.x >= wW) {
                snake.x = -16;
                snake.move();
            }
            else if (snake.x < -1) {
                snake.x = wW;
                snake.move();
            }
            else if (snake.y >= wH) {
                snake.y = -16;
                snake.move();
            }
            else if (snake.y < 0) {
                snake.y = wH;
                snake.move();
            }

            startTime = System.currentTimeMillis();

            // Eating the food
            if (snake.x == food.x && snake.y == food.y) {
                snake.justAte = true;
                food.x = food.getLocation(40);
                food.y = food.getLocation(30);
            }
            scoreLabel = updateScore();

            // Eating the tail
            for (int i = 0; i < snake.tail.size() - 2; i = i + 2) {
                if (snake.tail.get(i).equals(snake.tail.get(snake.tail.size() - 2))
                        && snake.tail.get(i + 1).equals(snake.tail.get(snake.tail.size() - 1)) ) {
                    if ((snake.tail.size() / 2) - 1 > hs) {
                        hs = (snake.tail.size() / 2) - 1;
                        try {
                            PrintWriter pr = new PrintWriter("snake.conf", "UTF-8");
                            pr.print(hs + " " + delay + " " + fontSize + " " + showFPS);
                            pr.close();
                        }
                        catch (IOException e) {
                            System.err.println("Caught IOException: " + e.getMessage());
                        }
                    }
                    snake.tail.clear();
                    snake.tail.add(snake.x);
                    snake.tail.add(snake.y);
                }
            }

        } // End of the delay

    }

    private Label updateScore() {
        strB.setLength(0);
        strB.append("High Score: ").append(hs);
        strB.append("  |  Current: ").append((snake.tail.size() / 2) - 1);
        if (showFPS) {
            strB.append("  FPS: ").append(Gdx.graphics.getFramesPerSecond());
        }
        scoreLabel.setText(strB);
        return scoreLabel;
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

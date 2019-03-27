package com.tesladodger.snake;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.ApplicationAdapter;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;


public class SnakeGame extends ApplicationAdapter {

    private Random ran = new Random();

    private class Snake {
        private int side = Gdx.graphics.getHeight() / 30;
        private int x = (ran.nextInt(11) + 10) * this.side;  // Start with random position in the middle,
        private int y = (ran.nextInt(11) + 10) * this.side;  // from 10 to 20 on the grid.
        private int moveX;
        private int moveY;
        Snake(int direction, int magnitude) {                       // Start with a random direction
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
                tail.add(x);     // Just add the new head to the end of the list
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

        private int getLocation() {
            return ran.nextInt(29) * food.side;
        }
    }

    private Snake snake;
    private Food food;

    private ShapeRenderer shapeRenderer;

    private boolean buttonPressed;

    private int hs;
    private Stage stage;
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
        food.x = food.getLocation();
        food.y = food.getLocation();

        buttonPressed = false;

        boolean fileExists = new File("hs.txt").isFile();
        if (!fileExists) {
            try {
                PrintWriter pr = new PrintWriter("hs.txt", "UTF-8");
                pr.print("");
                pr.close();
            }
            catch (IOException e) {
                System.err.println("Caught IOException: " + e.getMessage());
            }
        }
        else {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader("hs.txt"));
                String hsString = reader.readLine();
                reader.close();
                hs = Integer.parseInt(hsString);
            }
            catch (IOException e) {
                System.err.println("Caught IOException: " + e.getMessage());
            }
            catch (NumberFormatException e) {
                System.err.println("Caught NumberFormatException: " + e.getMessage());
            }
        }

        BitmapFont font = new BitmapFont();
        scoreLabel = new Label("Send Nudes", new Label.LabelStyle(font, Color.GRAY));
        strB       = new StringBuilder();
        stage      = new Stage();
        scoreLabel = updateScore();
        stage.addActor(scoreLabel);

        startTime = System.currentTimeMillis();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.draw();

        // Create the snake
        for (int i = 0; i <= snake.tail.size() -1; i = i + 2) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(snake.tail.get(i), snake.tail.get(i + 1), snake.side, snake.side);
            shapeRenderer.end();
        }

        // Create the food
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(food.x, food.y, food.side, food.side);
        shapeRenderer.end();

        // Eating the food
        if (snake.x == food.x && snake.y == food.y) {
            snake.justAte = true;
            food.x = food.getLocation();
            food.y = food.getLocation();
        }

        // Eating the tail
        for (int i = 0; i < snake.tail.size() - 2; i = i + 2) {
            if (snake.tail.get(i).equals(snake.tail.get(snake.tail.size() - 2))
                    && snake.tail.get(i + 1).equals(snake.tail.get(snake.tail.size() - 1)) ) {
                if ((snake.tail.size() / 2) - 1 > hs) {
                    hs = (snake.tail.size() / 2) - 1;
                    try {
                        PrintWriter pr = new PrintWriter("hs.txt", "UTF-8");
                        pr.print(hs);
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

        // World bounds
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

        // User input
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
        if (System.currentTimeMillis() -  startTime >= 100) {
            snake.move();
            startTime = System.currentTimeMillis();
            scoreLabel = updateScore();
        }

    }

    private Label updateScore() {
        strB.setLength(0);
        strB.append("High Score: ").append(hs);
        strB.append("  |  Current: ").append((snake.tail.size() / 2) - 1);
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
    }
}

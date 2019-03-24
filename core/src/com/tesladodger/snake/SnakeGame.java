package com.tesladodger.snake;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class SnakeGame extends ApplicationAdapter {

    private class Snake {
        private int side = Gdx.graphics.getHeight() / 30;
        private int x = 0;
        private int y = 0;
        private int moveX = side;
        private int moveY = 0;

        private boolean justAte;
        private List<Integer> tail;

        private void move() {
            x = x + moveX;
            y = y + moveY;
            buttonPressed = false;

            if (justAte){
                tail.add(x);     // Just add the new head to the end of the list
                tail.add(y);     // if it just ate.
                justAte = false;
            }
            else {
                for (int i = 0; i < tail.size() - 2; i++) {
                    tail.set(i, tail.get(i + 2));
                }
                tail.set(tail.size() - 2, x);   // Move every coordinate two places (one snake bit)
                tail.set(tail.size() - 1, y);   // to the front, and add the new head to the end.
            }
        }
    }

    private class Food {
        private int side = snake.side;
        private int x;
        private int y;
        private Random ran = new Random();

        private int getLocation() {
            return ran.nextInt(29) * food.side;
        }
    }

    private Snake snake;
    private Food food;

    private boolean buttonPressed;

    private ShapeRenderer shapeRenderer;

    private long startTime = System.currentTimeMillis();
	
	@Override
	public void create() {

        shapeRenderer = new ShapeRenderer();

        snake = new Snake();
        snake.tail = new ArrayList<Integer>();
        snake.tail.add(snake.x);
        snake.tail.add(snake.y);
        snake.justAte = false;

        food = new Food();
        food.x = food.getLocation();
        food.y = food.getLocation();

        buttonPressed = false;

	}

	@Override
	public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

        // Eating the snake
        for (int i = 0; i < snake.tail.size() - 2; i = i + 2) {
            if (snake.tail.get(i).equals(snake.tail.get(snake.tail.size() - 2)) && snake.tail.get(i + 1).equals(snake.tail.get(snake.tail.size() - 1)) ) {
                System.out.println("Noob... ");
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
        }

	}

    @Override
    public void resize(int x, int y) {
    }
	
	@Override
	public void dispose () {
        shapeRenderer.dispose();
	}
}

package com.tesladodger.snake.objects;

import java.util.List;
import java.util.Random;


public final class Food {
    public int x;
    public int y;


    /**
     * Method set the food in a random position and prevent it from appearing where the snake is.
     * @param tail coordinates of the tail;
     */
    public final void reset(List<Integer> tail) {
        Random ran = new Random();

        boolean foodInSnake = true;
        outerLoop:
        while (foodInSnake) {
            x = (ran.nextInt(40)*16);
            y = (ran.nextInt(30)*16);
            for (int i = 0; i < tail.size() - 1; i += 2) {
                if (tail.get(i) == x && tail.get(i+1) == y) {
                    // Go to the beginning of the while.
                    continue outerLoop;
                }
            }
            foodInSnake = false;
        }
    }

}

package com.tesladodger.snake.ai;

import java.util.ArrayList;
import java.util.List;


/*  Finds the closest path to the food.
 *
 *  The end of the tail list (effectively the current snake head) is the start
 * and the food is the goal. The rest of the tail list are obstacles.
 *
 *  The dumb path currently implemented doesn't check collisions with the
 * tail. I need to find if there's a collision and implement A*. If the path
 * is clear, the dumb alg. will do.
 *
 *  I'm considering, in order to not get trapped, if a square is between two
 * sections of the tail it's an obstacle as well. This will have to be tested.
 */


public class SnakeAI {
    private int[] foodLocation = new int[2];
    private List<Integer> moveArray = new ArrayList<Integer>();
    private int headX;
    private int headY;


    private int manhattanDist(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }


    private int[] getClosestFood(int foodX, int foodY){
        foodLocation[0] = foodX;
        foodLocation[1] = foodY;

        int dist = manhattanDist(foodX, foodY, headX, headY);

        int temp = manhattanDist(foodX + 40, foodY, headX, headY);
        if (temp < dist) {
            foodLocation[0] += 40;
            dist = temp;
        }

        temp = manhattanDist(foodX, foodY + 30, headX, headY);
        if (temp < dist) {
            foodLocation[0] = foodX;
            foodLocation[1] = foodY + 30;
            dist = temp;
        }

        temp = manhattanDist(foodX - 40, foodY, headX, headY);
        //System.out.println(temp);
        if (temp < dist) {
            foodLocation[0] = foodX - 40;
            foodLocation[1] = foodY;
            dist = temp;
        }

        temp = manhattanDist(foodX, foodY - 30, headX, headY);
        if (temp < dist) {
            foodLocation[0] = foodX;
            foodLocation[1] = foodY - 30;
        }

        return foodLocation;
    }



    public List<Integer> getMoves(int foodX, int foodY, List<Integer> snake) {
        // I want the grid positions, not the pixel ones:
        foodX = foodX / 16;
        foodY = foodY / 16;
        headX = snake.get(snake.size() - 2) / 16;
        headY = snake.get(snake.size() - 1) / 16;


        // Find where the closest food is
        foodLocation = getClosestFood(foodX, foodY);


        moveArray.clear();


        // todo check if the tail's in the way
        // if not, dumb path. otherwise, a*.


        /* Dumb path (this eats the tail, it's just a square path) */
        int distX = foodLocation[0] - headX;
        int distY = foodLocation[1] - headY;
        if (distX > 0) {
            for (int i = 0; i < distX; i++) {
                moveArray.add(0);
            }
        }
        else if (distX < 0) {
            for (int i = 0; i < Math.abs(distX); i++) {
                moveArray.add(2);
            }
        }
        if (distY > 0) {
            for (int i = 0; i < distY; i++) {
                moveArray.add(1);
            }
        }
        else if (distY < 0) {
            for (int i = 0; i < Math.abs(distY); i++) {
                moveArray.add(3);
            }
        }


        return moveArray;
    }
}

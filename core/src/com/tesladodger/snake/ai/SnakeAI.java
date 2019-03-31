package com.tesladodger.snake.ai;

import java.util.ArrayList;
import java.util.List;


/*
 *  Finds the closest food, which just means finding if crossing the edge is
 * faster. After that it calls the A* algorithm.
 *
 *  The end of the tail list (effectively the current snake head) is the start
 * and the food is the goal. The rest of the tail list are obstacles.
 *           ________  2
 *          |********|             Finds the distance between the snake head
 *          |*****f**|            (represented by the s) and the places it can
 *          |********|            get the food from (f). The coordinates of the
 *   _______|********|_______     closest are passed to the A* algorithm.
 *  |************************|     Every quadrant has a number to distinguish
 *  |*****f*******f*******f**|    them better in the code. The center is 0.
 * 3|************************| 1   The quadrants are 40*30.
 *  |*********s**************|
 *   ‾‾‾‾‾‾‾‾‾‾|********|‾‾‾‾‾‾‾‾‾‾
 *          |*****f**|
 *          |********|
 *          |********|
 *           ‾‾‾‾‾‾‾‾‾‾‾
 *              4
 *
 *  My naive goal was 150. It reached 153 before this commit. It has a lot of
 * improvements on top of the A* alg, mainly in the way obstacles are defined
 * (it still can get trapped, still don't know how to fix that).
 */


public final class SnakeAI {
    private int[] foodLocation = new int[2];
    private List<Integer> moveArray = new ArrayList<Integer>();
    private int headX;
    private int headY;
    private AStar aStar = new AStar();


    private int manhattanDist(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }


    private int[] getClosestFood(int foodX, int foodY){
        foodLocation[0] = foodX;
        foodLocation[1] = foodY;

        int dist = manhattanDist(foodX, foodY, headX, headY);           // 0

        int temp = manhattanDist(foodX + 40, foodY, headX, headY);  // 1
        if (temp < dist) {
            foodLocation[0] += 40;
            dist = temp;
        }

        temp = manhattanDist(foodX, foodY + 30, headX, headY);      // 2
        if (temp < dist) {
            foodLocation[0] = foodX;
            foodLocation[1] = foodY + 30;
            dist = temp;
        }

        temp = manhattanDist(foodX - 40, foodY, headX, headY);      // 3
        if (temp < dist) {
            foodLocation[0] = foodX - 40;
            foodLocation[1] = foodY;
            dist = temp;
        }

        temp = manhattanDist(foodX, foodY - 30, headX, headY);      // 4
        if (temp < dist) {
            foodLocation[0] = foodX;
            foodLocation[1] = foodY - 30;
        }

        return foodLocation;
    }


    public List<Integer> getMoves(int foodX, int foodY, List<Integer> snake) {
        // Change the pixel positions to spots on the grid.
        foodX = foodX / 16;
        foodY = foodY / 16;
        headX = snake.get(snake.size() - 2) / 16;
        headY = snake.get(snake.size() - 1) / 16;


        // Find where the closest food is.
        foodLocation = getClosestFood(foodX, foodY);


        moveArray.clear();


        moveArray = aStar.algorithm(foodLocation, snake);


        return moveArray;
    }
}

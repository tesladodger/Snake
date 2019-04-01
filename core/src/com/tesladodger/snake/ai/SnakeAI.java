package com.tesladodger.snake.ai;

import java.util.ArrayList;
import java.util.List;


/*
 *  Finds the closest food, which just means finding if crossing the edge is
 * faster. After that it calls the A* algorithm.
 *
 *  The end of the tail list (effectively the current snake head) is the start
 * and the food is the goal. The rest of the tail list are obstacles.
 *           ________2
 *          |********|             Finds the distance between the snake head
 *          |*****f**|            (represented by the s) and the places it can
 *          |********|            get the food from (f). The coordinates of the
 *   _______|********|_______     closest are passed to the A* algorithm.
 *  |************************|     Every quadrant has a number to distinguish
 *  |*****f*******f*******f**|    them better in the code. The center is 0.
 * 3|************************|1    The quadrants are 40*30.
 *  |*********s**************|
 *          |********|             This needs to be improved. The closest food
 *          |*****f**|            doesn't always mean the shortest path, and
 *          |********|            might not have a solution.
 *          |********|
 *              4
 *
 *  My naive goal was 150. It currently has a high score of 181. It has a lot
 * of improvements on top of the A* alg, mainly in the way obstacles are
 * defined (it still can get trapped, don't know how to fix that).
 *
 *  Need to add some king of zigzag, in case the food is trapped by the snake,
 * just to buy some time.
 */


public final class SnakeAI {
    private int[] foodLocation = new int[2];
    @SuppressWarnings("Convert2Diamond")
    private List<Integer> moveArray = new ArrayList<Integer>();
    private int headX;
    private int headY;
    private AStar aStar = new AStar();


    private static int manhattanDist(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }


    private int[] getClosestFood(int foodX, int foodY){
        foodLocation[0] = foodX;
        foodLocation[1] = foodY;

        // 0
        int dist = manhattanDist(foodX, foodY, headX, headY);

        // 1
        int temp = manhattanDist(foodX + 40, foodY, headX, headY);
        if (temp < dist) {
            foodLocation[0] += 40;
            dist = temp;
        }

        // 2
        temp = manhattanDist(foodX, foodY + 30, headX, headY);
        if (temp < dist) {
            foodLocation[0] = foodX;
            foodLocation[1] = foodY + 30;
            dist = temp;
        }

        // 3
        temp = manhattanDist(foodX - 40, foodY, headX, headY);
        if (temp < dist) {
            foodLocation[0] = foodX - 40;
            foodLocation[1] = foodY;
            dist = temp;
        }

        // 4
        temp = manhattanDist(foodX, foodY - 30, headX, headY);
        if (temp < dist) {
            foodLocation[0] = foodX;
            foodLocation[1] = foodY - 30;
        }

        return foodLocation;
    }


    public final List<Integer> getMoves(int foodX, int foodY, List<Integer> snake) {
        // Change the pixel positions to spots on the grid.
        foodX = foodX / 16;
        foodY = foodY / 16;
        headX = snake.get(snake.size() - 2) / 16;
        headY = snake.get(snake.size() - 1) / 16;

        // Find where the closest food is.
        foodLocation = getClosestFood(foodX, foodY);

        moveArray.clear();

        moveArray = aStar.algorithm(foodLocation, snake);

        // todo zigzag if the array is empty

        return moveArray;
    }
}

package com.tesladodger.snake.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/*  Thanks, wikipedia:                                                         *
 * https://en.wikipedia.org/wiki/A*_search_algorithm                           *
 *  This question and answer:                                                  *
 * https://gamedev.stackexchange.com  question 167824                          *
 *  And Coding Train:                                                          *
 * https://youtu.be/aKYlikFAV4k                                                *
 *                                                                             *
 *  I'm using a Hash Map to get to the nodes. It's not actually faster because *
 * the grid is very small, but I'm just happier not having to create all those *
 * nodes every time the algorithm gets called.                                 *
 *                                                                             *
 *  What I'm doing is searching for the shortest path to the food, around some *
 * obstacles, the tail.                                                        *
 *  To allow it to use the walls to it's advantage I used an 'extended' grid:  *
 *          8               2               7                                  *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *                           *
 *   * * * * *(f)* *|* * * * *(f)* *|* * * * *(f)* *                           *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *                           *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *                           *
 *   -----------------------------------------------                           *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *                           *
 * 3 * * * * *(f)* *|* * * * *(f)* *|* * * * *(f)* * 1                         *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *                           *
 *   * * * * * * * *|*(s)* * * * * *|* * * * * * * *                           *
 *   -----------------------------------------------                           *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *                           *
 *   * * * * *(f)* *|* * * * *(f)* *|* * * * *(f)* *                           *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *                           *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *                           *
 *          5               4               6                                  *
 *                                                                             *
 *  The center part (0) is where the start of the algorithm is, represented by *
 * an s. I just set the food location in the same spot for every part of the   *
 * grid. Setting the obstacles is a little trickier, I explain bellow.         *
 *  This algorithm is modified to search for the food with the shortest path,  *
 * which is done in two ways:                                                  *
 *  - Instead of setting a single node as the goal, I use a boolean in the node*
 * to tell the algorithm that particular node is a goal. This way I can set as *
 * many goals as I want.                                                       *
 *  - The heuristic has to be changed. Instead of the shortest distance to the *
 * goal, it is the shortest distance to any goal. So I just calculate the      *
 * distance to every goal and return the minimum value.                        *
 *  Using the Manhattan distance is the obvious choice for a square grid with  *
 * no diagonal movement.                                                       */


public final class AStar {
    @SuppressWarnings("Convert2Diamond")
    private Deque<Integer> moveQueue = new ArrayDeque<Integer>();
    @SuppressWarnings("Convert2Diamond")
    private Deque<Integer> path = new ArrayDeque<Integer>();

    // Initialize a Hash Map with nodeID as the key and the Node as the value.
    @SuppressWarnings("Convert2Diamond")
    private Map<Integer, Node> gridMap = new HashMap<Integer, Node>();

    private int[] food = new int[2];


    private Deque<Integer> reconstructPath(int current) {
        /*  Go trough the cameFrom nodes, adding them to the path list. Adding *
         * them to the beginning of the queue corrects the order (i.e. from the*
         * head to the food).                                                  */
        path.clear();

        int temp = current;
        path.add(temp);
        while (gridMap.get(temp).cameFrom != null) {
            path.addFirst(gridMap.get(temp).cameFrom);
            temp = gridMap.get(temp).cameFrom;
        }

        return path;
    }


    private Deque<Integer> convertToMoves() {
        // Turn the path of nodes into actual moves.
        int temp;

        // Because I'm shrinking the path deque, I need to save the size.
        int max = path.size() - 1;
        for (int i = 0; i < max; i++) {
            temp = path.removeFirst();
            if (gridMap.get(path.peekFirst()).x > gridMap.get(temp).x) {
                moveQueue.addLast(0);
            }
            else if (gridMap.get(path.peekFirst()).x < gridMap.get(temp).x) {
                moveQueue.addLast(2);
            }
            else if (gridMap.get(path.peekFirst()).y > gridMap.get(temp).y) {
                moveQueue.addLast(1);
            }
            else if (gridMap.get(path.peekFirst()).y < gridMap.get(temp).y) {
                moveQueue.addLast(3);
            }
        }
        
        return moveQueue;
    }


    private int manhattanDist(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }


    private int calculateHeuristic(int startX, int startY, int goalX, int goalY) {
        /*  This method returns the distance to the closest goal, for a given  *
         * node, i.e. the heuristic of that node.                              */
        int[] distances = new int[9];
        distances[0] = manhattanDist(startX, startY, goalX+40, goalY+30);
        distances[1] = manhattanDist(startX, startY, goalX+80, goalY+30);
        distances[2] = manhattanDist(startX, startY, goalX+40, goalY+60);
        distances[3] = manhattanDist(startX, startY, goalX,    goalY+30);
        distances[4] = manhattanDist(startX, startY, goalX+40, goalY);
        distances[5] = manhattanDist(startX, startY, goalX,    goalY);
        distances[6] = manhattanDist(startX, startY, goalX+80, goalY);
        distances[7] = manhattanDist(startX, startY, goalX+80, goalY+60);
        distances[8] = manhattanDist(startX, startY, goalX,    goalY+60);

        int heuristic = distances[0];
        for (int i = 1; i < distances.length; i++) {
            if (distances[i] < heuristic) {
                heuristic = distances[i];
            }
        }
        return heuristic;
    }


    // Main algorithm function. _______________________________________________ //
    public Deque<Integer> algorithm(int foodX, int foodY, List<Integer> snake) {


        // Populate the grid with nodes and add their ID to the HashMap.
        int ID = 0;
        for (int j = 0; j < 90; j++) {
            for (int i = 0; i < 120; i++) {
                Node node = new Node(ID, i, j);
                gridMap.put(node.nodeID, node);
                ID++;
            }
        }


        // Set the goals.
        food[0] = foodX / 16;
        food[1] = foodY / 16;
        gridMap.get( (food[0] + 40) + 120 * (food[1] + 30) ).isGoal = true;  // 0
        gridMap.get( (food[0] + 80) + 120 * (food[1] + 30) ).isGoal = true;  // 1
        gridMap.get( (food[0] + 40) + 120 * (food[1] + 60) ).isGoal = true;  // 2
        gridMap.get( (food[0]     ) + 120 * (food[1] + 30) ).isGoal = true;  // 3
        gridMap.get( (food[0] + 40) + 120 * (food[1]     ) ).isGoal = true;  // 4
        gridMap.get( (food[0]     ) + 120 * (food[1]     ) ).isGoal = true;  // 5
        gridMap.get( (food[0] + 80) + 120 * (food[1]     ) ).isGoal = true;  // 6
        gridMap.get( (food[0] + 80) + 120 * (food[1] + 60) ).isGoal = true;  // 7
        gridMap.get( (food[0]     ) + 120 * (food[1] + 60) ).isGoal = true;  // 8


        // Set the obstacles.
        int tempHeadX = snake.get(snake.size()-2)/16;
        int tempHeadY = snake.get(snake.size()-1)/16;
        for (int i = 2; i < snake.size() - 2; i += 2) {
            /* A part of the tail is only an obstacle if it can be reached.    *
            *  For example, if the second to last bit of the tail is more than *
            * one move away it's not actually an obstacle.                     *
            *  This really makes this algorithm work and not get stuck when    *
            * it doesn't have to (it's like predicting the future).            *
            *  I start at 2 because the tip of the tail is never an obstacle.  */
            int bitX = snake.get(i)/16;
            int bitY = snake.get(i+1)/16;
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16   , snake.get(i+1)/16   ) <= i / 2 + 1) {
                gridMap.get( (bitX + 40) + 120 * (bitY + 30) ).isObstacle = true;
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16+40, snake.get(i+1)/16   ) <= i / 2 + 1) {
                gridMap.get( (bitX + 80) + 120 * (bitY + 30) ).isObstacle = true;
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16   , snake.get(i+1)/16+30) <= i / 2 + 1) {
                gridMap.get( (bitX + 40) + 120 * (bitY + 60) ).isObstacle = true;
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16-40, snake.get(i+1)/16   ) <= i / 2 + 1) {
                gridMap.get( (bitX     ) + 120 * (bitY + 30) ).isObstacle = true;
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16   , snake.get(i+1)/16-30) <= i / 2 + 1) {
                gridMap.get( (bitX + 40) + 120 * (bitY     ) ).isObstacle = true;
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16-40, snake.get(i+1)/16-30) <= i / 2 + 1) {
                gridMap.get( (bitX     ) + 120 * (bitY     ) ).isObstacle = true;
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16+40, snake.get(i+1)/16-30) <= i / 2 + 1) {
                gridMap.get( (bitX + 80) + 120 * (bitY     ) ).isObstacle = true;
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16+40, snake.get(i+1)/16+30) <= i / 2 + 1) {
                gridMap.get( (bitX + 80) + 120 * (bitY + 60) ).isObstacle = true;
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16-40, snake.get(i+1)/16+30) <= i / 2 + 1) {
                gridMap.get( (bitX     ) + 120 * (bitY + 60) ).isObstacle = true;
            }
        }


        // ID of the start node.
        int start = (tempHeadX + 40) + 120 * (tempHeadY + 30);


        // Purely heuristic value for the first node.
        gridMap.get(start).g = 0;
        gridMap.get(start).f = calculateHeuristic(gridMap.get(start).x, gridMap.get(start).y, food[0], food[1]);


        // Open and closed sets.
        //noinspection Convert2Diamond
        List<Integer> openSet = new ArrayList<Integer>();
        //noinspection Convert2Diamond
        HashSet<Integer> closedSet = new HashSet<Integer>();


        // Add the ID of the start node to the openSet.
        openSet.add(start);


        // Algorithm loop.
        while (!openSet.isEmpty()) {

            // Find the node in the openSet with the lowest f.
            int indexLowestF = openSet.size() - 1;
            for (int i = openSet.size() - 1; i >= 0; i--) {
                /* Implement LIFO by starting at the end of the list and only  *
                 * searching for a lower f. This is faster, since all paths    *
                 * without obstacles have the same cost in a square grid. Also *
                 * the value isn't changed as much.                            */
                if (gridMap.get(openSet.get(i)).f < gridMap.get(openSet.get(indexLowestF)).f) {
                    indexLowestF = i;
                }
            }
            // Current is the ID of that node.
            int current = openSet.get(indexLowestF);

            if (gridMap.get(current).isGoal) {
                // Done, let's recreate the path.
                path = reconstructPath(current);
                // Get the moves and return them.
                return moveQueue = convertToMoves();
            }

            // Add current to the closedSet, remove it from the openSet.
            closedSet.add(current);
            openSet.remove(indexLowestF);

            // Get the neighbors of current.
            gridMap.get(current).addNeighbors();
            // For every neighbor of current:
            for (int i = 0; i < gridMap.get(current).neighbors.size(); i++) {
                int neighbor = gridMap.get(current).neighbors.get(i);

                // Ignore the neighbors in the closedSet and the obstacles.
                if (closedSet.contains(neighbor) || gridMap.get(neighbor).isObstacle) {
                    continue;
                }

                // The cost of a move in the grid is always 1.
                int tentativeGScore = gridMap.get(current).g + 1;

                // See if it's a new node.
                if (!openSet.contains(neighbor)) {
                    // Add it to the open set.
                    openSet.add(neighbor);
                }
                else if (tentativeGScore >= gridMap.get(neighbor).g) {
                    // If this path is worse, ignore it.
                    continue;
                }

                // If it gets here, this is the best path for this node until now.
                gridMap.get(neighbor).cameFrom = current;
                gridMap.get(neighbor).g = tentativeGScore;
                gridMap.get(neighbor).h = calculateHeuristic(gridMap.get(neighbor).x, gridMap.get(neighbor).y,
                        food[0], food[1]);
                gridMap.get(neighbor).f = gridMap.get(neighbor).g + gridMap.get(neighbor).h;

            }
        } // End of the A* loop.


        // If you get here, there's no solution...
        return moveQueue;
    }

}

package com.tesladodger.snake.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/*  Thanks, wikipedia:                                                                            *
 * https://en.wikipedia.org/wiki/A*_search_algorithm                                              *
 *  This question and answer:                                                                     *
 * https://gamedev.stackexchange.com  question 167824                                             *
 *  And Coding Train:                                                                             *
 * https://youtu.be/aKYlikFAV4k                                                                   *
 *                                                                                                *
 *  I'm using a Hash Map to get to the nodes. It's not actually faster because the grid is very   *
 * small, I just didn't like creating unnecessary Node objects.                                   *
 *  The openSet, closedSet and path all contain the node IDs in the map.                          *
 *                                                                                                *
 *  What the algorithm ultimately does is search for the shortest path to the food, around some   *
 * obstacles, the tail. I wanted to be able to cross the walls, so I create an 'extended' grid:   *
 *          8               2               7              The center part (0) is where the start *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *      is, represented by an s. I just set the *
 *   * * * * *(f)* *|* * * * *(f)* *|* * * * *(f)* *      food location in the same spot for every*
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *      section. Setting the obstacles is a     *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *      little trickier, I explain bellow.      *
 *   -----------------------------------------------       This algorithm is modified to search   *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *      for the food that can be reached with   *
 * 3 * * * * *(f)* *|* * * * *(f)* *|* * * * *(f)* * 1    the shortest path, which is done in two *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *      ways:                                   *
 *   * * * * * * * *|*(s)* * * * * *|* * * * * * * *       - Instead of setting a single node as  *
 *   -----------------------------------------------      the goal, I use a boolean to tell the   *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *      algorithm that that particular node is  *
 *   * * * * *(f)* *|* * * * *(f)* *|* * * * *(f)* *      a goal. This allows me to set as many   *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *      goals as I want.                        *
 *   * * * * * * * *|* * * * * * * *|* * * * * * * *       - The heuristic needs to be changed.   *
 *          5               4               6             Instead of the shortest distance to the *
 *                                                        goal, it is the shortest distance to any*
 * goal. So I just calculate the distance to every goal and return the minimum value.             *
 *  Using the Manhattan distance is the obvious choice for a square grid with no diagonals.       *
 *                                                                                                *
 *  The survive mode happens when the moveQueue returns empty. Instead of searching for food, the *
 * goals are every node neighboring the head:                                                     *
 *  ___________                                                                                   *
 * | 2 | 4 | 7 |   The algorithm favors the negative x and y directions, making a zigzag pattern. *
 * |___|___|___|   If the snake or the food is trapped the makes it avoid the tail, sometimes long*
 * | 1 | h | 6 |  enough so that a solution can be found.                                         *
 * |___|___|___|                                                                                  *
 * | 0 | 3 | 5 |                                                                                  *
 * |___|___|___|                                                                                  */


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
        /*  This method goes through the cameFrom node IDs and reconstructs    *
         * a path. The Deque is being used as a Stack, so that the returned    *
         * sequence is in the correct order, i.e. from head to food.           */
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
        /*  Turn the path of IDs into actual moves, using the x and y values of*
         * the nodes.                                                          */
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


    // Main algorithm function. _________________________________________________________________ //
    public Deque<Integer> algorithm(int foodX, int foodY, List<Integer> snake, boolean survive) {


        // Initialize the nodes and add their ID to the HashMap.
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
        if (survive) {
            // The food is actually the head.
            gridMap.get( (food[0] + 39) + 120 * (food[1] + 29) ).isGoal = true;  // 0
            gridMap.get( (food[0] + 39) + 120 * (food[1] + 30) ).isGoal = true;  // 1
            gridMap.get( (food[0] + 39) + 120 * (food[1] + 31) ).isGoal = true;  // 2
            gridMap.get( (food[0] + 40) + 120 * (food[1] + 29) ).isGoal = true;  // 3
            gridMap.get( (food[0] + 40) + 120 * (food[1] + 31) ).isGoal = true;  // 4
            gridMap.get( (food[0] + 41) + 120 * (food[1] + 29) ).isGoal = true;  // 5
            gridMap.get( (food[0] + 41) + 120 * (food[1] + 30) ).isGoal = true;  // 6
            gridMap.get( (food[0] + 41) + 120 * (food[1] + 31) ).isGoal = true;  // 7
        }
        else {
            gridMap.get((food[0] + 40) + 120 * (food[1] + 30)).isGoal = true;  // 0
            gridMap.get((food[0] + 80) + 120 * (food[1] + 30)).isGoal = true;  // 1
            gridMap.get((food[0] + 40) + 120 * (food[1] + 60)).isGoal = true;  // 2
            gridMap.get((food[0]     ) + 120 * (food[1] + 30)).isGoal = true;  // 3
            gridMap.get((food[0] + 40) + 120 * (food[1]     )).isGoal = true;  // 4
            gridMap.get((food[0]     ) + 120 * (food[1]     )).isGoal = true;  // 5
            gridMap.get((food[0] + 80) + 120 * (food[1]     )).isGoal = true;  // 6
            gridMap.get((food[0] + 80) + 120 * (food[1] + 60)).isGoal = true;  // 7
            gridMap.get((food[0]     ) + 120 * (food[1] + 60)).isGoal = true;  // 8
        }


        // Set the obstacles.
        int tempHeadX = snake.get(snake.size()-2)/16;
        int tempHeadY = snake.get(snake.size()-1)/16;
        for (int i = 0; i < snake.size() - 2; i += 2) {
            /*  A part of the tail is only an obstacle if it can be reached.   *
             *  For example, if the second to last bit of the tail is more than*
             * one move away it's not actually an obstacle.                    *
             *  This really makes this algorithm work and not get stuck when   *
             * it doesn't have to.                                             */
            int bitX = snake.get(i)/16;
            int bitY = snake.get(i+1)/16;
            if (manhattanDist(tempHeadX, tempHeadY, bitX   , bitY   ) <= i / 2 + 1) {
                gridMap.get( (bitX + 40) + 120 * (bitY + 30) ).isObstacle = true;  // 0
            }
            if (manhattanDist(tempHeadX, tempHeadY, bitX+40, bitY   ) <= i / 2 + 1) {
                gridMap.get( (bitX + 80) + 120 * (bitY + 30) ).isObstacle = true;  // 1
            }
            if (manhattanDist(tempHeadX, tempHeadY, bitX   , bitY+30) <= i / 2 + 1) {
                gridMap.get( (bitX + 40) + 120 * (bitY + 60) ).isObstacle = true;  // 2
            }
            if (manhattanDist(tempHeadX, tempHeadY, bitX-40, bitY   ) <= i / 2 + 1) {
                gridMap.get( (bitX     ) + 120 * (bitY + 30) ).isObstacle = true;  // 3
            }
            if (manhattanDist(tempHeadX, tempHeadY, bitX   , bitY-30) <= i / 2 + 1) {
                gridMap.get( (bitX + 40) + 120 * (bitY     ) ).isObstacle = true;  // 4
            }
            if (manhattanDist(tempHeadX, tempHeadY, bitX-40, bitY-30) <= i / 2 + 1) {
                gridMap.get( (bitX     ) + 120 * (bitY     ) ).isObstacle = true;  // 5
            }
            if (manhattanDist(tempHeadX, tempHeadY, bitX+40, bitY-30) <= i / 2 + 1) {
                gridMap.get( (bitX + 80) + 120 * (bitY     ) ).isObstacle = true;  // 6
            }
            if (manhattanDist(tempHeadX, tempHeadY, bitX+40, bitY+30) <= i / 2 + 1) {
                gridMap.get( (bitX + 80) + 120 * (bitY + 60) ).isObstacle = true;  // 7
            }
            if (manhattanDist(tempHeadX, tempHeadY, bitX-40, bitY+30) <= i / 2 + 1) {
                gridMap.get( (bitX     ) + 120 * (bitY + 60) ).isObstacle = true;  // 8
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
                /*  Solve ties in a LIFO sort of way, since all paths without  *
                 * obstacles have the same cost in a grid with no diagonals.   */
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

            // Set the neighbors of current.
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

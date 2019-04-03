package com.tesladodger.snake.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


/*  Thanks, wikipedia:                                                         *
 * https://en.wikipedia.org/wiki/A*_search_algorithm                           *
 *  This question and answer:                                                  *
 * https://gamedev.stackexchange.com  question 167824                          *
 *  And Coding Train:                                                          *
 * https://youtu.be/aKYlikFAV4k                                                *
 *                                                                             *
 *  This isn't optimal in terms of speed. This uses objects and linked lists,  *
 * because that's what I know how to use. I've seen stuff on StackOverflow with*
 * hash maps and references:                                                   *
 * https://codereview.stackexchange.com/questions/38376/a-search-algorithm     *
 *  It's a possible improvement to speed up the method.                        *
 *                                                                             *
 *  What I'm doing is searching for the shortest path to the food, around some *
 * obstacles, the tail.                                                        *
 *  To allow it to use the walls to it's advantage I used an 'extended' grid:  *
 *          |    2  |                                                          *
 *   ************************      The center part is where the snake head is. *
 * 8 *****f*******f*******f** 7   I just set the food translated to each spot, *
 *   ************************     as well as the tail.                         *
 * __************************__    The algorithm searches for the nearest food.*
 *   ************************      This is achieved in two ways:               *
 *   *****f*******f*******f**      Instead of setting a single goal node, I use*
 * 3 ************************ 1   a boolean to tell the algorithm that node is *
 * __*********s**************__   a goal. This way I can set as many goals as I*
 *   ************************     want.                                        *
 *   *****f*******f*******f**      The heuristic also has to be changed to be  *
 *   ************************     the shortest distance to get to a goal. So I *
 *   ************************ 6   just calculate the distance to every goal and*
 *     5    |   4   |             return the minimum.                          *
 *                                 Using the Manhattan distance is the obvious *
 * choice for a square grid with no diagonal movement.                         */


public final class AStar {
    @SuppressWarnings("Convert2Diamond")
    private Deque<Integer> moveQueue = new ArrayDeque<Integer>();
    @SuppressWarnings("Convert2Diamond")
    private Deque<Node> path = new ArrayDeque<Node>();
    // Initialize a 2D array of node objects.
    private Node[][] grid = new Node[120][90];
    private int[] food = new int[2];


    private Deque<Node> reconstructPath(Node current) {
        /*  Go trough the cameFrom nodes, adding them to the path list. Adding *
         * them to the beginning of the queue corrects the order (i.e. from the*
         * head to the food).                                                  */
        path.clear();

        Node temp = current;
        path.add(temp);
        while (temp.cameFrom != null) {
            path.addFirst(temp.cameFrom);
            temp = temp.cameFrom;
        }

        return path;
    }


    private Deque<Integer> convertToMoves() {
        // Turn the path of nodes into actual moves.
        Node temp;

        // Because I'm shrinking the path deque, I need to save the size.
        int max = path.size() - 1;
        for (int i = 0; i < max; i++) {
            temp = path.removeFirst();
            if (path.peekFirst().x > temp.x) {
                moveQueue.addLast(0);
            }
            else if (path.peekFirst().x < temp.x) {
                moveQueue.addLast(2);
            }
            else if (path.peekFirst().y > temp.y) {
                moveQueue.addLast(1);
            }
            else if (path.peekFirst().y < temp.y) {
                moveQueue.addLast(3);
            }
        }

        return moveQueue;
    }


    private int manhattanDist(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }


    private int calculateHeuristic(Node node, int goalX, int goalY) {
        /*  This method returns the distance to the closest goal, for a given  *
         * node, i.e. the heuristic of that node.                              */
        int[] distances = new int[9];
        distances[0] = manhattanDist(node.x, node.y, goalX+40, goalY+30);
        distances[1] = manhattanDist(node.x, node.y, goalX+80, goalY+30);
        distances[2] = manhattanDist(node.x, node.y, goalX+40, goalY+60);
        distances[3] = manhattanDist(node.x, node.y, goalX,    goalY+30);
        distances[4] = manhattanDist(node.x, node.y, goalX+40, goalY);
        distances[5] = manhattanDist(node.x, node.y, goalX,    goalY);
        distances[6] = manhattanDist(node.x, node.y, goalX+80, goalY);
        distances[7] = manhattanDist(node.x, node.y, goalX+80, goalY+60);
        distances[8] = manhattanDist(node.x, node.y, goalX,    goalY+60);

        int heuristic = distances[0];
        for (int i = 1; i < distances.length; i++) {
            if (distances[i] < heuristic) {
                heuristic = distances[i];
            }
        }
        return heuristic;
    }


    // Main algorithm function. _______________________________________________
    public Deque<Integer> algorithm(int foodX, int foodY, List<Integer> snake) {


        // Populate the grid with nodes.
        for (int i = 0; i < 120; i++) {
            for (int j = 0; j < 90; j++) {
                grid[i][j] = new Node(i, j);
            }
        }


        // Set the goals.
        food[0] = foodX / 16;
        food[1] = foodY / 16;
        grid[food[0]+40][food[1]+30].isGoal = true;  // 0
        grid[food[0]+80][food[1]+30].isGoal = true;  // 1
        grid[food[0]+40][food[1]+60].isGoal = true;  // 2
        grid[food[0]   ][food[1]+30].isGoal = true;  // 3
        grid[food[0]+40][food[1]   ].isGoal = true;  // 4
        grid[food[0]   ][food[1]   ].isGoal = true;  // 5
        grid[food[0]+80][food[1]   ].isGoal = true;  // 6
        grid[food[0]+80][food[1]+60].isGoal = true;  // 7
        grid[food[0]   ][food[1]+60].isGoal = true;  // 8


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
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16   , snake.get(i+1)/16   ) <= i / 2 + 1) {
                grid[(snake.get(i) / 16) + 40][(snake.get(i + 1) / 16) + 30].isObstacle = true;    // 0
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16+40, snake.get(i+1)/16   ) <= i / 2 + 1) {
                grid[(snake.get(i) / 16) + 80][(snake.get(i + 1) / 16) + 30].isObstacle = true;    // 1
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16   , snake.get(i+1)/16+30) <= i / 2 + 1) {
                grid[(snake.get(i) / 16) + 40][(snake.get(i + 1) / 16) + 60].isObstacle = true;    // 2
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16-40, snake.get(i+1)/16   ) <= i / 2 + 1) {
                grid[(snake.get(i) / 16)     ][(snake.get(i + 1) / 16) + 30].isObstacle = true;    // 3
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16   , snake.get(i+1)/16-30) <= i / 2 + 1) {
                grid[(snake.get(i) / 16) + 40][(snake.get(i + 1) / 16)     ].isObstacle = true;    // 4
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16-40, snake.get(i+1)/16-30) <= i / 2 + 1) {
                grid[(snake.get(i) / 16)     ][(snake.get(i + 1) / 16)     ].isObstacle = true;    // 5
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16+40, snake.get(i+1)/16-30) <= i / 2 + 1) {
                grid[(snake.get(i) / 16) + 80][(snake.get(i + 1) / 16)     ].isObstacle = true;    // 6
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16+40, snake.get(i+1)/16+30) <= i / 2 + 1) {
                grid[(snake.get(i) / 16) + 80][(snake.get(i + 1) / 16) + 60].isObstacle = true;    // 7
            }
            if (manhattanDist(tempHeadX,tempHeadY, snake.get(i)/16-40, snake.get(i+1)/16+30) <= i / 2 + 1) {
                grid[(snake.get(i) / 16)     ][(snake.get(i + 1) / 16) + 60].isObstacle = true;    // 8
            }
        }


        // Start and goal nodes (translated to the grid).
        Node start = grid[tempHeadX + 40][tempHeadY + 30];


        // Purely heuristic value for the first node.
        start.g = 0;
        start.f = calculateHeuristic(start, food[0], food[1]);


        // Open and closed sets.
        @SuppressWarnings("Convert2Diamond")
        List<Node> openSet = new ArrayList<Node>();
        @SuppressWarnings("Convert2Diamond")
        List<Node> closedSet = new ArrayList<Node>();
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
                if (openSet.get(i).f < openSet.get(indexLowestF).f) {
                    indexLowestF = i;
                }
            }
            // Current is that node.
            Node current = openSet.get(indexLowestF);

            if (current.isGoal) {
                // Done, let's recreate the path.
                path = reconstructPath(current);
                // Get the moves and return them.
                return moveQueue = convertToMoves();
            }

            // Add current to the closedSet, remove it from the openSet.
            closedSet.add(current);
            openSet.remove(indexLowestF);

            // Get the neighbors of current.
            current.addNeighbors(grid);
            // For every neighbor of current:
            for (int i = 0; i < current.neighbors.size(); i++) {
                Node neighbor = current.neighbors.get(i);

                // Ignore the neighbors in the closedSet and the obstacles.
                if (closedSet.contains(neighbor) || neighbor.isObstacle) {
                    continue;
                }

                // The cost of a move in the grid is always 1.
                int tentativeGScore = current.g + 1;

                // See if it's a new node.
                if (!openSet.contains(neighbor)) {
                    // Add it to the open set.
                    openSet.add(neighbor);
                }
                else if (tentativeGScore >= neighbor.g) {
                    // If this path is worse, ignore it.
                    continue;
                }

                // If it gets here, this is the best path for this node until now.
                neighbor.cameFrom = current;
                neighbor.g = tentativeGScore;
                neighbor.h = calculateHeuristic(neighbor, food[0], food[1]);
                neighbor.f = neighbor.g + neighbor.h;

            }
        } // End of the A* loop.


        // If you get here, there's no solution...
        return moveQueue;
    }

}

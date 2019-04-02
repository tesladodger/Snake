package com.tesladodger.snake.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


/*  Thanks, wikipedia:                                                         *
 * https://en.wikipedia.org/wiki/A*_search_algorithm                           *
 *  Thanks, Coding Train:                                                      *
 * https://youtu.be/aKYlikFAV4k                                                *
 *                                                                             *
 *  This isn't optimal in terms of speed. This uses objects and linked lists,  *
 * because that's what I know how to use. I've seen stuff on StackOverflow with*
 * hash maps and references:                                                   *
 * https://codereview.stackexchange.com/questions/38376/a-search-algorithm     *
 *  It's a possible improvement to speed up the method.                        *
 *                                                                             *
 *  I think I can use this algorithm, together with the Node object, in other  *
 * projects, with a bit of cleaning up. That's why the SnakeAI is a separate   *
 * class.                                                                      */


final class AStar {
    @SuppressWarnings("Convert2Diamond")
    private Deque<Integer> moveQueue = new ArrayDeque<Integer>();
    @SuppressWarnings("Convert2Diamond")
    private Deque<Node> path = new ArrayDeque<Node>();
    // Initialize a 2D array of node objects.
    private Node[][] grid = new Node[120][90];


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
        for (int i = 0; i < path.size() - 1; i++) {
            Node temp = path.removeFirst();
            if (path.getFirst().x > temp.x) {
                moveQueue.addLast(0);
            }
            else if (path.getFirst().x < temp.x) {
                moveQueue.addLast(2);
            }
            else if (path.getFirst().y > temp.y) {
                moveQueue.addLast(1);
            }
            else if (path.getFirst().y < temp.y) {
                moveQueue.addLast(3);
            }
        }
        return moveQueue;
    }


    // Main algorithm function.
    Deque<Integer> algorithm(int[] food, List<Integer> snake) {


        // Populate the grid with nodes.
        for (int i = 0; i < 120; i++) {
            for (int j = 0; j < 90; j++) {
                grid[i][j] = new Node(i, j);
            }
        }


        // Set the obstacles.
        int tempHeadX = snake.get(snake.size()-2)/16;
        int tempHeadY = snake.get(snake.size()-1)/16;
        for (int i = 2; i < snake.size() - 2; i += 2) {
            /*  A part of the tail is only an obstacle if it can be reached.   *
            *  For example, if the second to last bit of the tail is more than *
            *  one move away it's not actually an obstacle.                    *
            *   This really makes this algorithm work and not get stuck when   *
            * it doesn't have to.                                              *
            *   I start at 2 because the tip of the tail is never an obstacle. */
            if (      Math.abs( tempHeadX - (snake.get( i )/16) )
                    + Math.abs( tempHeadY - (snake.get(i+1)/16) ) <= i / 2 + 1) {
                grid[(snake.get(i) / 16) + 40][(snake.get(i + 1) / 16) + 30].isObstacle = true;  // 0
            }
            if (      Math.abs( tempHeadX - (snake.get( i )/16 + 40) )
                    + Math.abs( tempHeadY - (snake.get(i+1)/16     ) ) <= i / 2 + 1) {
                grid[(snake.get(i) / 16) + 80][(snake.get(i + 1) / 16) + 30].isObstacle = true;  // 1
            }
            if (      Math.abs( tempHeadX - (snake.get( i )/16     ) )
                    + Math.abs( tempHeadY - (snake.get(i+1)/16 + 30) ) <= i / 2 + 1) {
                grid[(snake.get(i) / 16) + 40][(snake.get(i + 1) / 16) + 60].isObstacle = true;  // 2
            }
            if (      Math.abs( tempHeadX - (snake.get( i )/16 - 40) )
                    + Math.abs( tempHeadY - (snake.get(i+1)/16     ) ) <= i / 2 + 1) {
                grid[(snake.get(i) / 16)     ][(snake.get(i + 1) / 16) + 30].isObstacle = true;  // 3
            }
            if (      Math.abs( tempHeadX - (snake.get(i  )/16     ) )
                    + Math.abs( tempHeadY - (snake.get(i+1)/16 - 30) ) <= i / 2 + 1) {
                grid[(snake.get(i) / 16) + 40][(snake.get(i + 1) / 16)     ].isObstacle = true;  // 4
            }
            grid[(snake.get(i) / 16)     ][(snake.get(i + 1) / 16)     ].isObstacle = true;  // 5
            grid[(snake.get(i) / 16) + 80][(snake.get(i + 1) / 16)     ].isObstacle = true;  // 6
            grid[(snake.get(i) / 16) + 80][(snake.get(i + 1) / 16) + 60].isObstacle = true;  // 7
            grid[(snake.get(i) / 16)     ][(snake.get(i + 1) / 16) + 60].isObstacle = true;  // 8
        }


        // Start and end nodes (translated to the grid).
        Node start = grid[tempHeadX + 40][tempHeadY + 30];
        Node end = grid[food[0] + 40][food[1] + 30];


        // Purely heuristic value for the first node.
        start.g = 0;
        start.f = Math.abs(start.x - end.x) + Math.abs(start.y - end.y);


        // Open and closed sets.
        @SuppressWarnings("Convert2Diamond")
        List<Node> openSet = new ArrayList<Node>();
        @SuppressWarnings("Convert2Diamond")
        List<Node> closedSet = new ArrayList<Node>();
        openSet.add(start);


        // Algorithm loop.
        while (!openSet.isEmpty()) {

            /* Find the node in the openSet with the lowest f, starting from   *
             * index 0.                                                        */
            int indexLowestF = openSet.size() - 1;
            for (int i = openSet.size() - 1; i >= 0; i--) {
                /* Implement LIFO by starting at the end of the list and only  *
                 * searching for a lower f. This is faster, since all paths    *
                 * without obstacles have the same cost in a square grid.      */
                if (openSet.get(i).f < openSet.get(indexLowestF).f) {
                    indexLowestF = i;
                }
            }
            // Current is that node.
            Node current = openSet.get(indexLowestF);

            if (current == end) {
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
                    openSet.add(neighbor);
                }
                else if (tentativeGScore >= neighbor.g) {
                    // If this path is worse, ignore it.
                    continue;
                }

                // If it gets here, this is the best path until now.
                neighbor.cameFrom = current;
                neighbor.g = tentativeGScore;
                // Using the Manhattan distance as the heuristic.
                neighbor.h = Math.abs(neighbor.x - end.x) + Math.abs(neighbor.y - end.y);
                neighbor.f = neighbor.g + neighbor.h;

            }
        } // End of the A* loop.


        // If you get here, there's no solution...
        return moveQueue;
    }

}

package com.tesladodger.snake.ai;

import java.util.ArrayList;
import java.util.List;


final class Node {
    int x;
    int y;
    int f;
    int g;
    int h;

    boolean isObstacle = false;
    boolean isGoal;

    @SuppressWarnings("Convert2Diamond")
    List<Node> neighbors = new ArrayList<Node>();
    Node cameFrom;

    final void addNeighbors(Node[][] grid) {
        if (x < 119) neighbors.add(grid[x + 1][y    ]);
        if (x > 0  ) neighbors.add(grid[x - 1][y    ]);
        if (y < 89 ) neighbors.add(grid[x    ][y + 1]);
        if (y > 0  ) neighbors.add(grid[x    ][y - 1]);
    }

    // Node constructor.
    Node(int i, int j) {
        x = i;
        y = j;
        f = 10800;  // Basically the cost of going trough every node,
        g = 10800;  // simulating the default value of infinity.
        h = 0;
        cameFrom = null;  // Initially this points to nowhere.
    }
}

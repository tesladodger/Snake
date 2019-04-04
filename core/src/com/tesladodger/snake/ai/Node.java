package com.tesladodger.snake.ai;

import java.util.ArrayList;
import java.util.List;


final class Node {
    int x;
    int y;
    int f;
    int g;
    int h;
    int nodeID;

    boolean isObstacle = false;
    boolean isGoal;

    @SuppressWarnings("Convert2Diamond")
    List<Integer> neighbors = new ArrayList<Integer>();
    Integer cameFrom;

    final void addNeighbors() {
        if (x < 119) neighbors.add(nodeID + 1);
        if (x > 0  ) neighbors.add(nodeID - 1);
        if (y < 89 ) neighbors.add(nodeID + 120);
        if (y > 0  ) neighbors.add(nodeID - 120);
    }

    // Node constructor.
    Node(int nodeID, int x, int y) {
        this.nodeID = nodeID;
        this.x = x;
        this.y = y;
        f = 10800;  // Basically the cost of going trough every node,
        g = 10800;  // simulating the default value of infinity.
        h = 0;
        //cameFrom = null;  // Initially this points to nowhere.
    }
}

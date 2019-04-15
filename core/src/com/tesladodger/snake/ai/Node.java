package com.tesladodger.snake.ai;

import java.util.ArrayList;
import java.util.List;


final class Node {
    int x;  // Grid position X.
    int y;  // Grid position Y.
    int f;  // g + h
    int g;  // Distance from start.
    int h;  // Heuristic distance to goal.

    int nodeID;

    boolean isObstacle;
    boolean isGoal;

    @SuppressWarnings("Convert2Diamond")
    List<Integer> neighbors = new ArrayList<Integer>();
    Integer cameFrom;

    /**
     * Method to add the IDs of this node's neighbors to the array.
     */
    final void addNeighbors() {
        if (x < 119) neighbors.add(nodeID + 1);    // Right
        if (x > 0  ) neighbors.add(nodeID - 1);    // Left
        if (y < 89 ) neighbors.add(nodeID + 120);  // Top
        if (y > 0  ) neighbors.add(nodeID - 120);  // Bottom
    }

    /**
     * Constructor.
     * @param nodeID every node is assigned an ID when constructed;
     * @param x coordinate;
     * @param y coordinate;
     */
    Node(int nodeID, int x, int y) {
        this.nodeID = nodeID;
        this.x = x;
        this.y = y;
        f = 10800;  // Basically the cost of going trough every node,
        g = 10800;  // simulating the default value of infinity.
        h = 0;
    }

}

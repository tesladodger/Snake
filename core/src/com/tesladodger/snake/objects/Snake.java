package com.tesladodger.snake.objects;

import com.badlogic.gdx.Gdx;

import java.util.List;


public final class Snake {
    public static final int side = Gdx.graphics.getHeight() / 30;
    public int x;
    public int y;
    public int moveX;
    public int moveY;
    private int prevLastTipX;
    private int prevLastTipY;
    public List<Integer> tail;
    public boolean justAte;


    // Constructor function
    public Snake(int direction, int magnitude, int ranX, int ranY) {  // Start with a random direction
        if (direction == 0) {
            this.moveX = magnitude * side;
        } else {
            this.moveY = magnitude * side;
        }
        x = ranX * side;
        y = ranY * side;
    }


    public final void move() {
        x = x + moveX;
        y = y + moveY;

        if (justAte){     // If it just ate,
            tail.add(x);  // Just add the new head to the end of the list.
            tail.add(y);
            justAte = false;
        }
        else {
            prevLastTipX = tail.get(0);
            prevLastTipY = tail.get(1);
            for (int i = 0; i < tail.size() - 2; i++) {
                tail.set(i, tail.get(i + 2)); // Otherwise move every coordinate two places to the front,
            }
            tail.set(tail.size() - 2, x);     // and overwrite the new head to the end.
            tail.set(tail.size() - 1, y);
        }
    }


    // When it passes a wall it has to go back before being sent to the other side
    // in order to not have two moves in one turn.
    public final void goBack() {
        for (int i = tail.size() - 1; i >= 2; i--) {
            tail.set(i, tail.get(i - 2));
        }
        tail.set(0, prevLastTipX);
        tail.set(1, prevLastTipY);
    }

    public final boolean checkAteTail() {
        for (int i = 0; i < tail.size() - 2; i += 2) {
            if (tail.get(i).equals(tail.get(tail.size() - 2)) && tail.get(i + 1).equals(tail.get(tail.size() - 1)) ) {
                return true;
            }
        }
        return false;
    }

    public final void restart() {
        tail.clear();
        tail.add(x);
        tail.add(y);
    }

}

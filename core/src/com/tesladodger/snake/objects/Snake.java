package com.tesladodger.snake.objects;

import com.badlogic.gdx.Gdx;

import java.util.List;


public final class Snake {
    public final int side = Gdx.graphics.getHeight() / 30;
    public int x;
    public int y;
    public int moveX;
    public int moveY;
    private int prevLastTipX;
    private int prevLastTipY;
    public List<Integer> tail;
    public boolean justAte;


    // Constructor function.
    public Snake(int direction, int magnitude, int ranX, int ranY) {
        if (direction == 0) {
            this.moveX = magnitude * side;
        } else {
            this.moveY = magnitude * side;
        }
        x = ranX * side;
        y = ranY * side;
    }


    public final void move() {
        x += moveX;
        y += moveY;

        if (justAte) {
            // If it ate, just add the new head to the list.
            tail.add(x);
            tail.add(y);
            justAte = false;
        }
        else {
            // Save the tip coordinates, in case it has to go back.
            prevLastTipX = tail.get(0);
            prevLastTipY = tail.get(1);

            // Move every coordinate two places to the front, overwriting the tip.
            for (int i = 0; i < tail.size() - 2; i++) {
                tail.set(i, tail.get(i + 2));
            }

            // Set the end to the coordinates of the new head.
            tail.set(tail.size() - 2, x);
            tail.set(tail.size() - 1, y);
        }
    }



    private void goBack() {
        /*  When it passes a wall it has to go back before being sent to the other *
         * side, in order to not have two moves in one turn and not be out of      *
         * bounds for the AI grid.                                                 */
        for (int i = tail.size() - 1; i >= 2; i--) {
            tail.set(i, tail.get(i - 2));
        }
        tail.set(0, prevLastTipX);
        tail.set(1, prevLastTipY);
    }


    public final void checkBounds() {
        if (x >= 640) {
            // Go to previous position.
            goBack();
            // Set the head in the right place.
            x = -16;
            move();
        }
        else if (x < -1) {
            goBack();
            x = 640;
            move();
        }
        else if (y >= 480) {
            goBack();
            y = -16;
            move();
        }
        else if (y < 0) {
            goBack();
            y = 480;
            move();
        }
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

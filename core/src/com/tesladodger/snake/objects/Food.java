package com.tesladodger.snake.objects;

import com.badlogic.gdx.Gdx;

public final class Food {
    public static final int side = Gdx.graphics.getHeight() / 30;
    public int x;
    public int y;

    public final int getLocation(int ran) {
        return ran * side;
    }
}

package com.vivian.breakoutgame;

import android.graphics.RectF;

public class Paddle {
    private RectF rect;
    private float length;
    private float height;
    private float x;
    private float y;
    private float paddleSpeed;

    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int paddleMoving = STOPPED;

    public Paddle(int screenX, int screenY){
        length = 130;
        height = 20;

        x = screenX / 2 - length / 2;
        y = screenY - 20;

        rect = new RectF(x, y,x + length,y + height);
        paddleSpeed = 350;
    }

    public RectF getRect(){
        return rect;
    }

    public void setMovementState(int state){
        paddleMoving = state;
    }

    public void update(long fps, int screenX){
        if (paddleMoving == LEFT){
            x = x - paddleSpeed / fps;
            if (x <= 0){
                x = 0;
            }
        }

        if (paddleMoving == RIGHT){
            x = x + paddleSpeed / fps;
            if ((x + length) >= screenX){
                x = screenX - length;
            }
        }

        rect.left = x;
        rect.right = x + length;
    }

    public void reset(int screenX, int screenY){
        x = screenX / 2 - length / 2;
        y = screenY - 20;

        rect.left = x;
        rect.top = y;
        rect.right = x + length;
        rect.bottom = y + height;
    }
}

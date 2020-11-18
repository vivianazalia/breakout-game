package com.vivian.breakoutgame;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BreakoutGame extends Activity {
    BreakoutView breakoutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        breakoutView = new BreakoutView(this);
        setContentView(breakoutView);
    }

    public class BreakoutView extends SurfaceView implements Runnable{
        Thread gameThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playing;
        boolean paused = true;
        Canvas canvas;
        Paint paint;
        long fps;
        private long timeThisFrame;

        int screenX, screenY;

        Paddle paddle;
        Ball ball;

        Brick[] bricks = new Brick[200];
        int numBricks = 0;

        int score = 0;
        int lives = 3;

        public BreakoutView(Context context) {
            super(context);

            ourHolder = getHolder();
            paint = new Paint();

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            screenX = size.x;
            screenY = size.y;

            paddle = new Paddle(screenX, screenY);
            ball = new Ball(screenX, screenY);

            createBricksAndRestart();
        }

        public void createBricksAndRestart(){
            ball.reset(screenX, screenY);
            paddle.reset(screenX, screenY);

            int brickWidth = screenX / 8;
            int brickHeight = screenY / 10;

            numBricks = 0;

            for (int column = 0; column < 8; column++){
                for (int row = 0; row < 3; row++){
                    bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                    numBricks++;
                }
            }

            score = 0;
            lives = 3;
        }

        @Override
        public void run() {
            while (playing){
                long startFrameTime = System.currentTimeMillis();

                if (!paused){
                    update();
                }

                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1){
                    fps = 1000/timeThisFrame;
                }
            }
        }

        public void update(){
            paddle.update(fps, screenX);
            ball.update(fps);

            for (int i = 0; i < numBricks; i++){
                if (bricks[i].getVisibility()){
                    if (RectF.intersects(bricks[i].getRect(), ball.getRect())){
                        bricks[i].setInvisible();
                        ball.reverseYVelocity();
                        score = score + 10;
                    }
                }
            }

            if (RectF.intersects(ball.getRect(), paddle.getRect())){
                ball.setRandomXVelocity();
                ball.reverseYVelocity();
                ball.clearObstacleY(paddle.getRect().top - 2);
            }

            if (ball.getRect().bottom > screenY){
                ball.reverseYVelocity();
                ball.clearObstacleY(screenY - 2);

                lives--;

                if (lives == 0){
                    paused = true;
                }
            }

            if (ball.getRect().top < 0){
                ball.reverseYVelocity();
                ball.clearObstacleY(12);
            }

            if (ball.getRect().left < 0){
                ball.reverseXVelocity();
                ball.clearObstacleX(2);
            }

            if (ball.getRect().right > screenX - 10){
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - 22);
            }

            if (score == numBricks * 10){
                paused = true;
            }

            if ((lives == 0 | score == numBricks * 10) & !paused){
                createBricksAndRestart();
            }
        }

        public void draw(){
            if (ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255,26,128,182));
                paint.setColor(Color.argb(255,255,255,255));

                //Draw paddle
                canvas.drawRect(paddle.getRect(), paint);

                //Draw ball
                canvas.drawRect(ball.getRect(), paint);

                //Draw brick
                paint.setColor(Color.argb(255, 245, 66, 66));

                for (int i = 0; i < numBricks; i++){
                    if (bricks[i].getVisibility()){
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                //Draw HUD
                paint.setColor(Color.argb(255, 255, 255, 255));

                paint.setTextSize(40);
                canvas.drawText("Score: " + score + "   Lives: " + lives, 10,50, paint);

                if (score == numBricks * 10){
                    paint.setTextSize(90);
                    canvas.drawText("YOU HAVE WON!", 30, screenY / 2, paint);
                }

                if (lives == 0){
                    paint.setTextSize(90);
                    canvas.drawText("YOU HAVE LOST!", 30, screenY / 2, paint);
                }

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void pause(){
            playing = false;

            try {
                gameThread.join();
            } catch (InterruptedException e){
                Log.e("Error", "joining thread");
            }
        }

        public void resume(){
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                    paused = false;
                    if (event.getX() > screenX / 2){
                        paddle.setMovementState(paddle.RIGHT);
                    } else {
                        paddle.setMovementState(paddle.LEFT);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    paddle.setMovementState(paddle.STOPPED);
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        breakoutView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        breakoutView.pause();
    }
}
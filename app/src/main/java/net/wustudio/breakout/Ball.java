package net.wustudio.breakout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import java.util.ArrayList;
import java.util.Random;

public class Ball extends ShapeDrawable {
    // ball dimensions
    private int left;
    private int right;
    private int top;
    private int bottom;
    private int radius;

    // ball speed
    private int velocityX;
    private int velocityY;

    // timer when ball hits screen bottom
    private final int resetBallTimer = 1000;

    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private boolean paddleCollision;
    private boolean blockCollision;
    private Rect mPaddle;
    private Rect ballRect;


    public Ball(Context context) {
        super(new OvalShape());
        this.getPaint().setColor(Color.CYAN);
    }

    public void initCoords(int width, int height) {
        Random rnd = new Random(); // starting x velocity direction

        paddleCollision = false;
        blockCollision = false;
        SCREEN_WIDTH = width;
        SCREEN_HEIGHT = height;

        radius = SCREEN_WIDTH / 72;
        velocityX = radius;
        velocityY = radius * 2;

        // ball coordinates
        left = (SCREEN_WIDTH / 2) - radius;
        right = (SCREEN_WIDTH / 2) + radius;
        top = (SCREEN_HEIGHT / 2) - radius;
        bottom = (SCREEN_HEIGHT / 2) + radius;

        int startingXDirection = rnd.nextInt(2); // random beginning direction
        if (startingXDirection > 0) {
            velocityX = -velocityX;
        }
    }

    public void drawBall(Canvas canvas) {
        this.setBounds(left, top, right, bottom);
        this.draw(canvas);
    }

    public int moveBall() {
        int bottomHit = 0;

        if (blockCollision) {
            velocityY = -velocityY;
            blockCollision = false; // reset
        }

        // paddle collision  (擾亂速度)
        if (paddleCollision && velocityY > 0) {
            int paddleSplit = (mPaddle.right - mPaddle.left) / 4;
            int ballCenter = ballRect.centerX();
            if (ballCenter < mPaddle.left + paddleSplit) {
                velocityX = -(radius * 3);
            } else if (ballCenter < mPaddle.left + (paddleSplit * 2)) {
                velocityX = -(radius * 2);
            } else if (ballCenter < mPaddle.centerX() + paddleSplit) {
                velocityX = radius * 2;
            } else {
                velocityX = radius * 3;
            }
            velocityY = -velocityY;
        }

        // side walls collision
        if (this.getBounds().right >= SCREEN_WIDTH) {
            velocityX = -velocityX;
        } else if (this.getBounds().left <= 0) {
            this.setBounds(0, top, radius * 2, bottom);
            velocityX = -velocityX;
        }

        // screen top/bottom collisions
        if (this.getBounds().top <= 0) {
            velocityY = -velocityY;
        } else if (this.getBounds().top > SCREEN_HEIGHT) {
            bottomHit = 1; // lose a turn
            try {
                Thread.sleep(resetBallTimer);
                initCoords(SCREEN_WIDTH, SCREEN_HEIGHT); // reset ball
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // move ball
        left += velocityX;
        right += velocityX;
        top += velocityY;
        bottom += velocityY;
        return bottomHit;
    }

    public boolean checkPaddleCollision(Paddle paddle) {
        mPaddle = paddle.getBounds();
        ballRect = this.getBounds();

        Rect ballBox = this.getBounds();
        //追加便宜
        ballBox.left += (radius * 2);
        ballBox.right += (radius * 2);
        ballBox.top += (radius * 2);
        ballBox.bottom += (radius * 2);

        paddleCollision = ballBox.intersect(mPaddle);
        return paddleCollision;
    }

    public int checkBlocksCollision(ArrayList<Block> blocks) {
        int points = 0;
        Rect ballBox = this.getBounds();
        //放大偏移
        ballBox.left += velocityX;
        ballBox.right += velocityX;
        ballBox.top += velocityY;
        ballBox.bottom += velocityY;

        for (int i = blocks.size() - 1; i >= 0; i--) {  //注意必須反過來刪
            Block block = blocks.get(i);
            Rect blockRect = block.getBounds();
            if (blockRect.intersect(ballBox)) {
                blockCollision = true;
                points = getPoints(block.getColor());
                blocks.remove(i);
                break;
            }
        }
        return points;
    }

    private int getPoints(int color) {
        int points = 0;
        if (color == Color.LTGRAY)
            points = 100;
        else if (color == Color.MAGENTA)
            points = 200;
        else if (color == Color.GREEN)
            points = 300;
        else if (color == Color.YELLOW)
            points = 400;
        else if (color == Color.RED)
            points = 500;

        return points;
    }

    public int getVelocityY() {
        return velocityY;
    }
}

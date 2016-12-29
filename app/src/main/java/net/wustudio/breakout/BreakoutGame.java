package net.wustudio.breakout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class BreakoutGame extends SurfaceView implements Runnable {

    private int levelCompleted = 0;
    private int PLAYER_TURNS_NUM = 3;
    private final int frameRate = 33;
    private final int startTimer = 66;
    private boolean touched = false;
    private float offsetX;
    private SurfaceHolder holder;
    private Thread gameThread = null;
    private boolean running = false;
    private Canvas canvas;
    private int waitCount = 0;
    private Ball ball;
    private Paddle paddle;
    private ArrayList<Block> blocksList;

    //畫筆
    private Paint getReadyPaint;
    private Paint scorePaint;
    private Paint livesPaint;

    private int points = 0;
    private int lives;

    private enum eAction {
        READY,
        INGAME,
        OVER,
    }

    private eAction action = eAction.READY;

    public BreakoutGame(Context context) {
        super(context);
        lives = PLAYER_TURNS_NUM;

        holder = getHolder();
        ball = new Ball(this.getContext());
        paddle = new Paddle();
        blocksList = new ArrayList<Block>();

        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(25);

        livesPaint = new Paint();
        livesPaint.setTextAlign(Paint.Align.RIGHT);
        livesPaint.setColor(Color.WHITE);
        livesPaint.setTextSize(25);

        getReadyPaint = new Paint();
        getReadyPaint.setTextAlign(Paint.Align.CENTER);
        getReadyPaint.setColor(Color.WHITE);
        getReadyPaint.setTextSize(45);
    }

    public void run() {
        while (running) {
            try {
                Thread.sleep(frameRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!holder.getSurface().isValid()) {
                continue;
            }

            canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);

            if (blocksList.size() == 0) {
                levelCompleted++;
                initObjects(canvas);
                if (levelCompleted > 1) {  //表示通關
                    lives++;
                }
                //重置，強迫系統倒數開始
                waitCount = 0;
            }
            //更新等待時間
            waitCount++;

            //檢察是否為使用者動作
            if (touched) {
                paddle.movePaddle((int) offsetX);
            }
            //更新畫面
            drawToCanvas(canvas);
            //啟動遊戲引擎
            engine(canvas);
            //繪製UI
            canvas.drawText("得分:" + points, 0, 25, scorePaint);
            canvas.drawText("生命:" + lives, canvas.getWidth(), 25, livesPaint);

            holder.unlockCanvasAndPost(canvas); // 解除反鎖
        }
    }

    //region //init something
    private void initObjects(Canvas canvas) {
        touched = false; // reset paddle location
        ball.initCoords(canvas.getWidth(), canvas.getHeight());
        paddle.initCoords(canvas.getWidth(), canvas.getHeight());
        initBlocks(canvas);
    }

    private void initBlocks(Canvas canvas) {
        int blockHeight = canvas.getWidth() / 36;
        int spacing = canvas.getWidth() / 144;
        int topOffset = canvas.getHeight() / 10;
        int blockWidth = (canvas.getWidth() / 10) - spacing;

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int y_coordinate = (i * (blockHeight + spacing)) + topOffset;
                int x_coordinate = j * (blockWidth + spacing);

                Rect r = new Rect();
                r.set(x_coordinate, y_coordinate, x_coordinate + blockWidth,
                        y_coordinate + blockHeight);

                int color;

                if (i < 2)
                    color = Color.RED;
                else if (i < 4)
                    color = Color.YELLOW;
                else if (i < 6)
                    color = Color.GREEN;
                else if (i < 8)
                    color = Color.MAGENTA;
                else
                    color = Color.LTGRAY;

                Block block = new Block(r, color);

                blocksList.add(block);
            }
        }
    }
    //endregion

    //region //draw something
    private void drawToCanvas(Canvas canvas) {
        drawBlocks(canvas);
        paddle.drawPaddle(canvas);
        ball.drawBall(canvas);
    }

    private void drawBlocks(Canvas canvas) {
        for (int i = 0; i < blocksList.size(); i++) {
            blocksList.get(i).drawBlock(canvas);
        }
    }
//endregion

    private void engine(Canvas canvas) {

        switch (action) {
            case READY:
                getReadyPaint.setColor(Color.WHITE);
                canvas.drawText("請準備", canvas.getWidth() / 2, (canvas.getHeight() / 2) - (ball.getBounds().height()), getReadyPaint);
                //判斷轉換模式
                if (waitCount > startTimer && lives > 0) {
                    action = eAction.INGAME;
                }
                break;
            case INGAME:
                lives -= ball.moveBall();  //移動球 (如果死掉，會回傳1)
                ball.checkPaddleCollision(paddle); //檢察是否碰撞到　球拍Paddle
                points += ball.checkBlocksCollision(blocksList); //碰撞檢察 (如果撞到block 會回傳分數)

                if (lives < 0) {
                    levelCompleted = 0;
                    points = 0;
                    lives = PLAYER_TURNS_NUM;
                    blocksList.clear();
                    action = eAction.OVER;
                }
                break;
            case OVER:
                getReadyPaint.setColor(Color.RED);
                canvas.drawText("GAME OVER!!!", canvas.getWidth() / 2, (canvas.getHeight() / 2) - (ball.getBounds().height()) - 50, getReadyPaint);
                //TODO: 重新開始的機制
                break;
            default:
                //不可能run
                break;
        }
    }

    public void pause() {
        running = false;
        while (true) {
            try {
                gameThread.join();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        gameThread = null;
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            offsetX = event.getX();
            touched = true;
        }
        return touched;
    }
}

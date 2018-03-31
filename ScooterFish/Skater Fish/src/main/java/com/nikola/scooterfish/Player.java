package com.nikola.scooterfish;

import android.graphics.Bitmap;
import android.graphics.Canvas;


/**
 * Created by Nikola on 6/23/2016.
 */
public class Player extends GameObject {
    private Bitmap spritesheet;
    private int score;
    private boolean up;
    private boolean playing;
    private Animation animation = new Animation();
    private long startTime;
    private static final int dyConst = 0;

    public Player(Bitmap res, int w, int h, int numFrames) {
        x = 100;
        y = GamePanel.HEIGHT/2;
        //dy is the speed of downward and upward movement
        dy = 0;
        score = 0;
        height = h;
        width = w;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, i*width, 0, width, height);
        }

        animation.setFrames(image);
        animation.setDelay(10);
        startTime = System.nanoTime();

    }

    public void setUp(boolean b) {
        up = b;
    }

    public void update() {
        long elapsed = (System.nanoTime() - startTime)/1000000;
        if(elapsed > 100) {
            score++;
            startTime = System.nanoTime();
        }

        if(up) {
            dy -= 1;
        }
        else {
            dy += 1;
        }
        if(dy > 14) dy = 14;
        if(dy < -14) dy = -14;

        //the fish shouldn't move past the top border
        if(y > GamePanel.HEIGHT/3){
            y += dy * 2;
        }
        //if the fish touches the top border it shouldn't move further and when up becomes false it should start falling
        else {
            if(!up) {
                dy = 3;
                y += dy * 2;
            }
        }
        //if the fish touches the bot border it shouldn't move further down
        if(y > GamePanel.HEIGHT - 40) {
            if(up) {
                dy = -2;
                y += dy * 2;
            }
            else {
               y = GamePanel.HEIGHT - 40;
            }
        }

    }

    public void draw(Canvas canvas) {
            canvas.drawBitmap(animation.getImage(), x, y, null);
    }

    public int getScore() {return score;}
    public boolean getPlaying() {return playing;}
    public void setPlaying(boolean b) {playing = b;}
    public void resetDY() {dy = 0;}
    public void resetScore() {score = 0;}

}

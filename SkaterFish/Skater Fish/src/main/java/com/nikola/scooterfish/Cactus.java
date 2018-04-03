package com.nikola.scooterfish;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;


public class Cactus extends GameObject {
    private Animation animation = new Animation();
    private Bitmap spritesheet;

    public Cactus(Bitmap res, int x, int y, int w, int h, int s, int numFrames) {
        super.x = x;
        super.y = y;
        width = w;
        height = h;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for(int i = 0; i<image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, 0, i*height, width, height);
        }
        //there is only 1 image for now so there are no more frames to show (no need to use delay)
        animation.setFrames(image);

    }

    public void update() {
        x -= 5;
        animation.update();
    }

    public void draw(Canvas canvas) {
        try {
            canvas.drawBitmap(animation.getImage(), x, y, null);
        } catch(Exception e) {}
    }
}

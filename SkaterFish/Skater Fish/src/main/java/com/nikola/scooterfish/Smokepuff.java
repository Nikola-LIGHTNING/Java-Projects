package com.nikola.scooterfish;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Smokepuff extends GameObject {
    public int radius; //radius
    public Smokepuff(int x, int y) {
        radius = 5;
        super.x = x;
        super.y = y;
    }

    public void update() {
        x -= 10;
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x - radius, y - radius, radius, paint);
        canvas.drawCircle(x - radius + 2, y - radius - 2, radius, paint);
        canvas.drawCircle(x - radius + 4, y - radius + 1, radius, paint);
    }
}

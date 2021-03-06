package com.nikola.scooterfish;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public static final int WIDTH = 860;
    public static final int HEIGHT = 500;
    public static final int MOVESPEED = -5;
    private MainThread thread;
    private long smokeStartTime;
    private long missileStartTime;
    private long cactusStartTime;
    private Background bg;
    private Player player;
    private ArrayList<Cactus> cactuses;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private Random rand = new Random();
    private boolean newGameCreated;

    private Explosion explosion;
    private long startReset;
    private boolean started;
    private boolean reset;
//    //variable which decides if we are going to draw the skating fish or not
    private boolean dissapear = false;
    private long best = 0;

    public GamePanel(Context context) {
        super(context);

        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);


        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while(retry && counter < 1000)
        {
            counter++;
            try{thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            }catch(InterruptedException e){e.printStackTrace();}
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.background_final));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.skating_fish), 60, 40, 2);
        smoke = new ArrayList<Smokepuff>();
        missiles = new ArrayList<Missile>();
        cactuses = new ArrayList<Cactus>();
        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();
        cactusStartTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);
        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            if(!player.getPlaying() && newGameCreated && reset) {
                player.setPlaying(true);
                player.setUp(true);
            }
            if(player.getPlaying()){
                if(!started) started = true;
                reset = false;
                player.setUp(true);
            }
            return true;
        }

        if(event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update() {
        if (player.getPlaying()) {

            bg.update();
            player.update();

            //add missiles on timer
            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            if (missileElapsed > (2000 - player.getScore() / 4)) {
                //first missile always goes in the middle
                if (missiles.size() == 0) {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.
                            missiles_nobg), WIDTH + 10, HEIGHT / 2, 45, 13, player.getScore(), 3));
                }
                //range of missile spawning points
                //(int)(rand.nextDouble()*HEIGHT)
                else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.
                            missiles_nobg), WIDTH + 10, (int) (rand.nextDouble() * (HEIGHT / 3) + HEIGHT / 3.7), 45, 13, player.getScore(), 3));

                }
                missileStartTime = System.nanoTime();
            }

            //loop through every missile to check for collision and remove if needed
            for (int i = 0; i < missiles.size(); i++) {
                //update missile
                missiles.get(i).update();
                if (collision(missiles.get(i), player)) {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //removes missiles which are way off the screen
                if (missiles.get(i).getX() < -100) {
                    missiles.remove(i);
                    break;
                }
            }
            //add cactuses on timer
            long cactusElapsed = (System.nanoTime() - cactusStartTime) / 1000000;
            if (cactusElapsed > (2000 - player.getScore() / 4)) {
                cactuses.add(new Cactus(BitmapFactory.decodeResource(getResources(), R.drawable.cactus), WIDTH + 10, (int) (rand.nextDouble() * HEIGHT + HEIGHT / 3), 50, 70, player.getScore(), 1));
                cactusStartTime = System.nanoTime();
            }
            // loop through every cactus to check for collision and remove if needed
            for (int i = 0; i < cactuses.size(); i++) {
                //update cactus
                cactuses.get(i).update();
                if (collision(cactuses.get(i), player)) {
                    cactuses.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //removes cactuses which are way off the screen
                if (cactuses.get(i).getX() < -100) {
                    cactuses.remove(i);
                    break;
                }
            }
            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime) / 1000000;

            if (elapsed > 120) {
                smoke.add(new Smokepuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }

            for (int i = 0; i < smoke.size(); i++) {
                smoke.get(i).update();
                if (smoke.get(i).getX() < -10) {
                    smoke.remove(i);
                }
            }
        } else {
            player.resetDY();
            if (!reset) {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion_2), player.getX(), player.getY(), 100, 100, 23);
            }
           explosion.update();

           long resetElapsed = (System.nanoTime()-startReset)/1000000;
            if (resetElapsed > 2500 && !newGameCreated) {
                newGame();
            }
        }
    }

    public boolean collision(GameObject a, GameObject b) {
        if(Rect.intersects(a.getRectangle(), b.getRectangle())) {
            return true;
        }
        return false;
    }


    @Override
    public void draw(Canvas canvas) {

        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);
        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if(!dissapear) {
                player.draw(canvas);
            }
//            player.draw(canvas);
            //draw smoke puffs
            for(Smokepuff sp : smoke) {
                sp.draw(canvas);
            }
            //draw missiles
            for(Missile m:missiles) {
                m.draw(canvas);
            }
            //draw cactuses
            for(Cactus c: cactuses) {
                c.draw(canvas);
            }
            //draw explosion
            if(dissapear && started) {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
        //poneje reve, che iska canvas super draw e napisan sledniq kod
        if(3 == 4) {
            super.draw(canvas);
        }
    }

    public void newGame() {
        dissapear = false;

        missiles.clear();
        cactuses.clear();
        smoke.clear();

        if(player.getScore()*3 > best) {
            best = player.getScore()*3;
        }

        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT/2);

        newGameCreated = true;
    }

    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore()*3), 10, 40, paint);
        canvas.drawText("BEST: " + best, WIDTH - 200, 40, paint);

        if(!player.getPlaying() && newGameCreated && reset) {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2 - 50, HEIGHT/2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2 - 50, HEIGHT/2 + 20, paint1);
            canvas.drawText("RELEASE GO TO GO DOWN", WIDTH/2 - 50, HEIGHT/2 + 40, paint1);
        }

    }

}
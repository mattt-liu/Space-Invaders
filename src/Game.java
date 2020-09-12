package src;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.*;

public class Game extends JPanel {

    private ArrayList<ArrayList<Rectangle>> shieldRects = new ArrayList<ArrayList<Rectangle>>(); // particles of the shields
    private ArrayList<BossAlien> boss = new ArrayList<BossAlien>();		// boss alien position
    private ArrayList<Bullet> bullets = new ArrayList<Bullet>();		// bullets fired by user
    private ArrayList<Bullet> alien_bullets = new ArrayList<Bullet>(); 	// bullets fired by aliens
    private ArrayList<Rectangle> rects = new ArrayList<Rectangle>(); 	// alien position holders
    private ArrayList<Rectangle> shield = new ArrayList<Rectangle>(); 	// shields the ship hides behind
    private ArrayList<Shield> shieldHP = new ArrayList<Shield>(); 		// shields' health

    private boolean over = false;
    private boolean[] alienDie;
    private boolean[] keys;

    private int score = 0;

    private Random rand = new Random();

    private Ship ship;

    private int score1 = 50; // y postions of aliens which corresponds to their score value
    private int score2 = 150;
    private int score3 = 250;

    private BufferedImage alien1;
    private BufferedImage alien2;
    private BufferedImage alien3;
    private BufferedImage alien4;
    private BufferedImage alien5;
    private BufferedImage alien6;
    private BufferedImage alienDieImg;
    private BufferedImage bossalien;
    private BufferedImage shipImg;
    private BufferedImage shipDieImg;

    public Game() {

        keys = new boolean[KeyEvent.KEY_LAST + 1];

        ship = new Ship(180, SpaceInvaders.frameHeight - 100);

        // build aliens
        for (int i = 0; i < 11; i ++) {
            for (int j = 0; j < 5; j ++) {
                Rectangle r = new Rectangle(i * (Alien.w+10) + 20, j * (Alien.h+25) + 100, Alien.w, Alien.h);
                rects.add(r);
            }
        }
        alienDie = new boolean[rects.size()];
        for (int i = 0; i < alienDie.length; i ++) {
            alienDie[i] = false;
        }

        // buield shields
        for (int i = 0; i < 4; i ++) {
            Rectangle r = new Rectangle(i * (70 + 100) + 170, SpaceInvaders.frameHeight - 200, 70, 50);
            shield.add(r);
            Shield s = new Shield();
            shieldHP.add(s);
        }
        for (int i = 0; i < 4; i ++) {
            ArrayList<Rectangle> a = new ArrayList<Rectangle>();
            for (int x = 0; x < 14; x ++) {
                for (int y = 0; y < 8; y ++) {
                    a.add(new Rectangle(i * (70 + 100) + 170 + x*5, SpaceInvaders.frameHeight - 200 + y*5, 5, 5));
                }
            }
            for (int x = 0; x < 4; x ++) {
                for (int y = 8; y < 12; y ++) {
                    a.add(new Rectangle(i * (70 + 100) + 170 + x*5, SpaceInvaders.frameHeight - 200 + y*5, 5, 5));
                }
            }
            for (int x = 10; x < 14; x ++) {
                for (int y = 8; y < 12; y ++) {
                    a.add(new Rectangle(i * (70 + 100) + 170 + x*5, SpaceInvaders.frameHeight - 200 + y*5, 5, 5));
                }
            }
            shieldRects.add(a);
        }

        // load images
        try {
            alien1 = ImageIO.read(new File("alien1.png"));
            alien2 = ImageIO.read(new File("alien2.png"));
            alien3 = ImageIO.read(new File("alien3.png"));
            alien4 = ImageIO.read(new File("alien4.png"));
            alien5 = ImageIO.read(new File("alien5.png"));
            alien6 = ImageIO.read(new File("alien6.png"));
            alienDieImg = ImageIO.read(new File("aliendie.png"));
            bossalien = ImageIO.read(new File("bossalien.png"));
            shipImg = ImageIO.read(new File("ship.png"));
            shipDieImg = ImageIO.read(new File("shipdie.png"));
        }
        catch (IOException e) {}
    }

    public void refresh() {

        // ---------- move ship ----------
        if (!ship.die) {
            if (keys[KeyEvent.VK_RIGHT]) { ship.move(6); }
            if (keys[KeyEvent.VK_LEFT])  { ship.move(-6); }
        }

        // ---------- move aliens ----------
        moveAliens();
        resetAlien();

        // ---------- move boss alien ----------
        moveBoss();

        // ---------- shoot bullets ----------
        shoot();

        // ---------- shoot alien bullets ----------
        alienShoot();

        // ---------- bullets hit things ----------
        if (bullets.size() > 0) {
            hitAlien();
        }
        if (bullets.size() > 0) {
            hitShield();
        }

        // ---------- alien bullets hit things ----------
        if (alien_bullets.size() > 0) {
            alienHitShield();
        }
        if (alien_bullets.size() > 0) {
            hitShip();
        }
    }

    public void alienHitShield() {

        for (int z = alien_bullets.size() - 1; z >= 0; z --) {
            for (int i = 0; i < shield.size(); i ++) {
                Rectangle b = new Rectangle(alien_bullets.get(z).x, alien_bullets.get(z).y, Bullet.w, Bullet.h);
                Rectangle r = shield.get(i);
                for (int j = shieldRects.get(i).size() - 1; j >= 0; j --) {
                    // checks which of the 4 shields is being hit
                    if (b.intersects(shieldRects.get(i).get(j))) {
                        // remove random particles from the shield
                        shieldRects.get(i).remove(j);
                        for (int k = 0; k < 25; k ++) {
                            // create a random index
                            int Xindex = 10 * (rand.nextInt(7) - 3);// in the range (-3, 4) * 10
                            int Yindex = -1 * rand.nextInt(4);		// in the range (0, -3)
                            try {
                                Rectangle toBeRemoved = shieldRects.get(i).get(j+Xindex+Yindex);
                                int dist = (int)(Math.hypot(toBeRemoved.x - b.x, toBeRemoved.y - b.y - 5));
                                if (dist <= 15) { // dist is <= 15 to create a circular crater effect
                                    shieldRects.get(i).remove(j + Xindex + Yindex);
                                }
                            }
                            catch (IndexOutOfBoundsException e) {}
                        }
                        alien_bullets.remove(z);
                        break;
                    }
                }
                if (alien_bullets.size() <= z) { break; }
            }
        }
    }

    public void alienShoot() {

        ArrayList<Rectangle> bottom = new ArrayList<Rectangle>(); // tracks bottom most aliens' rects
        // find bottom most aliens
        for (int i = 0; i < rects.size(); i ++) {
            if (i == rects.size()-1) { // add the last item in AL
                bottom.add(rects.get(i));
            }
            else {
                Rectangle r1 = rects.get(i);
                Rectangle r2 = rects.get(i + 1);
                /* since each rect goes in vertical order,
                 * if the next one is higher up, it means
                 * a new column has started and therefore
                 * the current one is the bottom most rect
                 */
                if (r1.y > r2.y) { bottom.add( rects.get(i) ); }
            }
        }
        // move bullets
        for (int i = alien_bullets.size()-1; i >= 0; i --) {
            if (alien_bullets.get(i).y > SpaceInvaders.frameHeight) { alien_bullets.remove(i); } // remove bullets at the bottom
            else { alien_bullets.get(i).move(Bullet.SPEED - 4); } // alien's bullets move slower
        }
        // create bullets
        if (Alien.reload >= 2 * 66) { // 3 sec reload * 66 fps
            for (int i = 0; i < bottom.size(); i ++) {
                Rectangle r = bottom.get(i);
                if (alien_bullets.size() < 6 && rand.nextInt(2) == 0) { // 1 in 2 chance of shooting when less than 6 bullets on screen
                    if (Math.abs(ship.x - r.x) <= 100) {				// shoot a bullet if near the ship
                        Bullet b = new Bullet(r.x + r.width/2, r.y + r.height/2);
                        alien_bullets.add(b);
                    }
                }
            }
            Alien.reload = 0;
        }
        else {Alien.reload ++; }
    }

    public void hitAlien() {

        Rectangle b = new Rectangle(bullets.get(0).x, bullets.get(0).y, bullets.get(0).w, bullets.get(0).h);
        for (int i = 0; i < rects.size(); i ++) {
            if (rects.get(i).intersects(b)) {
                rects.remove(i);
                alienDie[i] = true;

                // increase score
                /* score is determined by how far the alien is
                 * the further the alien is from the ship,
                 * the higher the score
                 */
                if (bullets.get(0).y >= score3) { score += 10; }
                else if (bullets.get(0).y >= score2) { score += 20; }
                else if (bullets.get(0).y >= score1) { score += 40; }

                bullets.remove(0);
                break;
            }
        }
    }

    public void hitShield() {

        for (int i = 0; i < shield.size(); i ++) {

            Rectangle b = new Rectangle(bullets.get(0).x, bullets.get(0).y, bullets.get(0).w, bullets.get(0).h);
            Rectangle r = shield.get(i);

            for (int j = shieldRects.get(i).size() - 1; j >= 0; j --) {

                // checks which of the 4 shields is being hit
                if (b.intersects(shieldRects.get(i).get(j))) {

                    // remove random particles from the shield
                    shieldRects.get(i).remove(j);
                    for (int k = 0; k < 50; k ++) {
                        // create a random index
                        int Xindex = 10 * (rand.nextInt(7) - 3);// in the range (-3, 4) * 10
                        int Yindex = -1 * rand.nextInt(4);		// in the range (0, -3)

                        try {

                            Rectangle toBeRemoved = shieldRects.get(i).get(j+Xindex+Yindex);
                            int dist = (int)(Math.hypot(toBeRemoved.x - b.x, toBeRemoved.y - b.y - 5));

                            if (dist <= 15) { // dist is <= 15 to create a circular crater effect
                                shieldRects.get(i).remove(j + Xindex + Yindex);
                            }
                        }
                        catch (IndexOutOfBoundsException e) {}
                    }
                    bullets.remove(0);
                    break;
                }
            }
            if (bullets.size() < 1) { break; }
        }
    }

    public void hitShip() {

        for (int i = alien_bullets.size() - 1; i >= 0; i --) {

            Rectangle s = new Rectangle(ship.x, ship.y, Ship.w, Ship.h);
            Rectangle b = new Rectangle(alien_bullets.get(i).x, alien_bullets.get(i).y, Bullet.w, Bullet.h);

            if (s.intersects(b)) {
                alien_bullets.remove(i);
                Ship.lives --;
                if (Ship.lives <= 0) {
                    over = true;
                }
                ship.die = true;
                break;
            }
        }

    }

    public void moveAliens() {

        // game ends when aliens are too close
        for (Rectangle r : rects) {
            if (r.y >= ship.y - 50) {
                over = true;
                break;
            }
        }

        if (rects.size() > 0) {

            if (rects.get(rects.size()-1).x + rects.get(rects.size()-1).width >= SpaceInvaders.limitR && Alien.direction == Alien.RIGHT) {
                // if past the right limit face left
                Alien.direction = Alien.LEFT;
                // shift aliens down
                for (int i = 0; i < rects.size(); i ++) {
                    rects.get(i).y += rects.get(i).height; }
                // shift score line down
                score1 += rects.get(0).height;
                score2 += rects.get(0).height;
                score3 += rects.get(0).height; }
            if (rects.get(0).x <= SpaceInvaders.limitL && Alien.direction == Alien.LEFT) {
                // if past the left limit face right
                Alien.direction = Alien.RIGHT;
                // shift aliens down
                for (int i = 0; i < rects.size(); i ++) {
                    rects.get(i).y += rects.get(i).height; }
                // shift score line down
                score1 += rects.get(0).height;
                score2 += rects.get(0).height;
                score3 += rects.get(0).height; }
        }

        if (Alien.direction == Alien.RIGHT) {
            if (rects.size() > 20) {			// if more than 20 aliens move slow
                if (Alien.move >= 40) {			// every 40 frames the aliens move
                    Alien.move = 0;				// reset the counter
                    for (int i = 0; i < rects.size(); i ++) {
                        rects.get(i).x += 10;// move each rect
                    } }
                else { Alien.move ++; } }
            else {
                for (int i = 0; i < rects.size(); i ++) {
                    if (rects.size() <= 1) { rects.get(i).x += 5; }
                    else if (rects.size() <= 2) { rects.get(i).x += 4; }
                    else if (rects.size() <= 4) { rects.get(i).x += 3; }
                    else if (rects.size() <= 10) { rects.get(i).x += 2; }
                    else if (rects.size() <= 20) { rects.get(i).x += 1; }
                } }
        }

        if (Alien.direction == Alien.LEFT) {
            if (rects.size() > 20) {			// if more than 20 aliens move slow
                if (Alien.move >= 40) {			// every 40 frames the aliens move
                    Alien.move = 0;				// reset the counter
                    for (int i = 0; i < rects.size(); i ++) {
                        rects.get(i).x -= 10;// move each rect
                    } }
                else { Alien.move ++; } }
            else {								// if 4 or less, move aliens quicker
                for (int i = 0; i < rects.size(); i ++) {
                    if (rects.size() <= 1) { rects.get(i).x -= 5; }
                    else if (rects.size() <= 2) { rects.get(i).x -= 4; }
                    else if (rects.size() <= 4) { rects.get(i).x -= 3; }
                    else if (rects.size() <= 10) { rects.get(i).x -= 2; }
                    else if (rects.size() <= 20) { rects.get(i).x -= 1; }
                } }
        }
    }

    public void moveBoss() {

        // create new boss
        if (rand.nextInt(300) == 0 && boss.size() < 1) {
            if (BossAlien.direction == BossAlien.RIGHT) {
                boss.add(new BossAlien(0 - BossAlien.w, 50));
            }
            if (BossAlien.direction == BossAlien.LEFT) {
                boss.add(new BossAlien(SpaceInvaders.limitR, 50));
            }
        }
        for (int i = 0; i < boss.size(); i ++) {
            BossAlien b = boss.get(i);
            // moving
            if (b.direction == b.RIGHT && b.x > SpaceInvaders.limitR + b.w) {
                b.direction = b.LEFT;
                boss.remove(i);
                break;
            }
            else if (b.direction == b.LEFT && b.x < SpaceInvaders.limitL - 2*b.w) {
                b.direction = b.RIGHT;
                boss.remove(i);
                break;
            }
            else {
                b.move();
            }
            // gets shot
            if (bullets.size() > 0) {
                Rectangle rect = new Rectangle(b.x, b.y, b.w, b.h);
                Rectangle bull = new Rectangle(bullets.get(0).x, bullets.get(0).y, bullets.get(0).w, bullets.get(0).h);
                if (rect.intersects(bull)) {
                    score += b.score;
                    bullets.remove(0);
                    boss.remove(0);
                    BossAlien.direction *= -1;
                }
            }
        }
    }

    public void resetAlien() {

        if (rects.size() <= 0) {
            for (int i = 0; i < 11; i ++) {
                for (int j = 0; j < 5; j ++) {
                    Rectangle r = new Rectangle(i * (Alien.w+10) + 20, j * (Alien.h+25) + 100, Alien.w, Alien.h);
                    rects.add(r);
                }
            }
            score1 = 50; // y postions of aliens which corresponds to their score value
            score2 = 150;
            score3 = 250;
            Ship.lives ++;
        }
    }

    public void shoot() {

        // move bullets
        for (int i = 0; i < bullets.size(); i ++) {
            bullets.get(i).move(-Bullet.SPEED);
            if (bullets.get(i).y <= 0 + Bullet.h/2) { bullets.remove(i); } // remove bullets at the top
        }
        // create a bullet if none exist
        if (keys[KeyEvent.VK_SPACE] && bullets.size() == 0) {
            Bullet b = new Bullet(ship.x + (Ship.w/2), ship.y - (Bullet.h/2));
            bullets.add(b);
        }

    }

    @Override
    public void paintComponent(Graphics g) {

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        spaceship(g);

        // draw bullets
        g.setColor(Color.WHITE);
        for (int i = 0; i < bullets.size(); i ++) {
            int bx = bullets.get(i).x;
            int by = bullets.get(i).y;
            int bw = bullets.get(i).w;
            int bh = bullets.get(i).h;
            g.fillRect(bx, by, bw, bh); }
        for (int i = 0; i < alien_bullets.size(); i ++) {
            int bx = alien_bullets.get(i).x;
            int by = alien_bullets.get(i).y;
            int bw = Bullet.w;
            int bh = Bullet.h;
            g.fillRect(bx, by, bw, bh);
        }

        // draw aliens
        if (Animations.alienMove >= 40) {
            for (Rectangle r : rects) {
                if (r.y >= score3) { g.drawImage(alien1, r.x, r.y, null); }
                else if (r.y >= score2) { g.drawImage(alien3, r.x, r.y, null); }
                else if (r.y >= score1) { g.drawImage(alien5, r.x+7, r.y, null); } }
        }
        else if (Animations.alienMove >= 0) {
            for (Rectangle r : rects) {
                if (r.y >= score3) { g.drawImage(alien2, r.x, r.y, null); }
                else if (r.y >= score2) { g.drawImage(alien4, r.x, r.y, null); }
                else if (r.y >= score1) { g.drawImage(alien6, r.x+7, r.y, null); } }
        }
        if (Animations.alienMove >= 80) { Animations.alienMove = 0; }
        else { Animations.alienMove ++; }

        // draw boss alien
        for (int i = 0; i < boss.size(); i ++) {
            BossAlien b = boss.get(i);
            g.drawImage(bossalien, b.x, b.y, null);
        }

        // draw shields
        g.setColor(Color.GREEN);
        for (int i = 0; i < shieldRects.size(); i ++) {
            for (int j = 0; j < shieldRects.get(i).size(); j ++) {
                Rectangle r = shieldRects.get(i).get(j);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        }

        // draw text
        g.setColor(Color.WHITE);
        g.drawString("SCORE", 20, 30);
        g.drawString("" + score, 85, 30);

        // game over screen
        if (over) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.drawString("SCORE", 425, 330);
            g.drawString("" + score, 500, 330);

            g.setColor(Color.RED);
            g.setFont(new Font("Comic Sans MS",Font.PLAIN,48));
            g.drawString("GAME OVER", 350, 275);
        }
    }

    public void spaceship(Graphics g) {
        /* draws spaceship and its lives
         */

        // dying animation
        if (ship.die == true) {

            if (Animations.shipDie > 48) {
                ship.die = false;
                Animations.shipDie = 0;
                ship = new Ship(180, SpaceInvaders.frameHeight - 100); // ship returns to starting position
                Alien.reload = 0; // prevents ship from immediately getting shot
            }
            else {
                g.drawImage(shipDieImg, ship.x, ship.y, null);
                Animations.shipDie ++;
            }
        }

        // normal ship
        else {
            g.setColor(new Color(0, 255, 165));
            g.drawImage(shipImg, ship.x + 2, ship.y, null);
        }

        // ship lives
        g.setColor(Color.WHITE);
        g.drawString("LIVES", Ship.lives * -1 * (Ship.w+10) + SpaceInvaders.frameWidth - 30 - Ship.w, 30);
        for (int i = 0; i < Ship.lives; i ++) {
            g.drawImage(shipImg, i * -1 * (Ship.w+10) + SpaceInvaders.frameWidth - 30 - Ship.w, 8, null);
        }
    }

    public void setKey(int i, boolean b) {
        /* changes the array called 'keys'
         */

        keys[i] = b;
    }
}

class Alien {

    public static final int w = 50;
    public static final int h = 25;
    public static final int RIGHT = 1;
    public static final int LEFT = -1;
    public static int direction = RIGHT;
    public static int move = 0;
    public static int reload = 0;
}

class Animations {

    public static int alienDie = 0;
    public static int alienMove = 0;
    public static int shipDie = 0;
}

class BossAlien {

    public static final int w = 50;
    public static final int h = 25;
    public static final int RIGHT = 1;
    public static final int LEFT = -1;
    public static final int score = 200;
    public static int direction = RIGHT;

    public int x, y;

    public BossAlien(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move() {
        x += direction * 2;
    }
}

class Bullet {

    public int x, y;
    public static final int w = 2;
    public static final int h = 10;
    public static final int SPEED = 9;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public void move(int d) {
        y += d;
    }
}

class Shield {

    int health;

    public Shield() {
        health = 10;
    }

    public void damage() {
        if (health <= 1) {
            health = 0;
        }
        else {
            health --;
        }
    }
}

class Ship {

    public int x, y;
    public boolean die = false;
    public static int lives = 3;
    public static final int w = 50;
    public static final int h = 25;

    public Ship(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(int d) {
        x += d;
        if (x <= SpaceInvaders.limitL) { x = SpaceInvaders.limitL; } // doesn't go past left
        if (x + w >= SpaceInvaders.limitR) { x = SpaceInvaders.limitR - w; } // doesn't go past right
    }
}

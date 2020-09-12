package src;


// SpaceInvaders.java
// Matthew Liu

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

//import javax.swing.*;

public class SpaceInvaders extends JFrame implements ActionListener, KeyListener {

    javax.swing.Timer myTimer;
    Game game;

    static final int frameWidth = 1000, frameHeight = 700;
    static final int limitL = 20, limitR = frameWidth - 35;

    public SpaceInvaders() {

        //super("Space Invaders");
        setSize(frameWidth, frameHeight);

        myTimer = new javax.swing.Timer(15, this); // runs at 66.7 fps
        myTimer.start();

        game = new Game();

        add(game);
        addKeyListener(this);

        //setDefaultCloseOperation(EXIT_ON_CLOSE);
        //setResizable(false);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (game != null) {
            game.refresh();
            game.repaint();
        }
    }

    public static void main(String[] args) {
        new SpaceInvaders();
    }

    public void keyPressed(KeyEvent e) {
        game.setKey(e.getKeyCode(), true);
    }

    public void keyReleased(KeyEvent e) {
        game.setKey(e.getKeyCode(), false);
    }

    public void keyTyped(KeyEvent e) {
    }
}
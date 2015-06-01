package com.ui;

import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

class Graph extends JPanel implements Runnable {
    public Graph(){
        this.setOpaque(false);
        this.setPreferredSize(new Dimension(500,500));
    }
    public void run(){

    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;
        final int width  = this.getWidth();
        final int height = this.getHeight();
        //g2d.setPaint(new )
        g2d.fillRect(0, width, 0, height);
    }
}

package com.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BackgroundPanel extends JPanel {
    private final BufferedImage background;

    public BackgroundPanel(BufferedImage image) {
        background = image;
        this.setPreferredSize(new Dimension(background.getWidth(),
                background.getHeight()));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int iw = background.getWidth();
        int ih = background.getHeight();
        if (iw < 0 || ih < 0) {
            return;
        }

        for (int x = 0; x < getWidth(); x += iw) {
            for (int y = 0; y < getHeight(); y += ih) {
                g.drawImage(background, x, y, iw, ih, this);
            }
        }
    }
}

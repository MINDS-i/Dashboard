package com.ui;

import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

public class BackgroundPanel extends JPanel{
  private BufferedImage background;
  public BackgroundPanel(BufferedImage image){
    background = image;
    this.setPreferredSize(new Dimension(background.getWidth(this),
                            background.getHeight(this)));
  }
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    int iw = background.getWidth(this);
    int ih = background.getHeight(this);
    if (iw < 0 || ih < 0) return;

    for (int x = 0; x < getWidth(); x += iw) {
      for (int y = 0; y < getHeight(); y += ih) {
        g.drawImage(background, x, y, iw, ih, this);
      }
    }
  }
}

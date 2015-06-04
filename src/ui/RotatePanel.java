package com.ui;

import com.ui.TelemetryListener;
import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

public class RotatePanel extends JPanel implements TelemetryListener{
  private double theta;
  private BufferedImage image;
  private BufferedImage background;
  private BufferedImage overlay;

  public RotatePanel(
      BufferedImage tiltImage,
      BufferedImage backgroundImage,
      BufferedImage glare) {
    image = tiltImage;
    background = backgroundImage;
    overlay = glare;
    this.setPreferredSize(new Dimension(background.getWidth(this),
                          background.getHeight(this)));
    setOpaque(false);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.drawImage(background, 0, 0, null);
    g2d.translate(this.getWidth() / 2, this.getHeight() / 2);
    g2d.rotate(theta);
    g2d.translate(-image.getWidth(null) / 2, -image.getHeight(null) / 2);
    g2d.drawImage(image, 0, 0, null);
    g2d.translate(image.getWidth(null)/2, image.getHeight(null)/2);
    g2d.rotate(-theta);
    g2d.translate(-this.getWidth()/2, -this.getHeight()/2);
    g2d.drawImage(overlay, 0, 0, null);
  }

  public void update(double angle){
    theta = (angle*Math.PI)/180;
    repaint();
  }
}

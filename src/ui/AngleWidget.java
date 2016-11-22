package com.ui;

import com.telemetry.TelemetryListener;
import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import com.Context;

public class AngleWidget extends JPanel implements TelemetryListener {
    /**
     * Create a new AngleWidget instance that displays the values in a given
     * telemetry index interpreted as angles by applying them to an image in
     * a dial
     * @param ctx         Dashboard context instance
     * @param telemetryID Telemetry index with angles in degrees
     * @param indicator   The image to rotate according to the telemetry data
     */
    public static AngleWidget createDial(
      Context ctx,
      int telemetryID,
      BufferedImage indicator){
        AngleWidget aw = new AngleWidget( indicator,
                            ctx.theme.gaugeBackground,
                            ctx.theme.gaugeGlare);
        ctx.telemetry.registerListener(telemetryID, aw);
        return aw;
    }

    private double theta;
    private BufferedImage image;
    private BufferedImage background;
    private BufferedImage overlay;

    private AngleWidget(
      BufferedImage tiltImage,
      BufferedImage backgroundImage,
      BufferedImage foregroundImage) {
        image = tiltImage;
        background = backgroundImage;
        overlay = foregroundImage;
        this.setPreferredSize(new Dimension(background.getWidth(),
                                            background.getHeight()) );
        setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        final int bgXC = background.getWidth()  / 2;
        final int bgYC = background.getHeight() / 2;
        final int imageXC = image.getWidth()  / 2;
        final int imageYC = image.getHeight() / 2;

        g2d.drawImage(background, 0, 0, null);
        //
        g2d.translate(bgXC, bgYC);
        g2d.rotate(theta);
        g2d.translate(-imageXC, -imageYC);
        g2d.drawImage(image, 0, 0, null);
        //
        g2d.translate(imageXC, imageYC);
        g2d.rotate(-theta);
        g2d.translate(-bgXC, -bgYC);
        g2d.drawImage(overlay, 0, 0, null);
        //
        g2d.dispose();
    }

    public void update(double angle) {
        theta = (angle*Math.PI)/180;
        repaint();
    }
}

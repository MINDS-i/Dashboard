package com.ui.widgets;

import com.Context;
import com.telemetry.TelemetryListener;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AngleWidget extends JPanel implements TelemetryListener {

    //Constants
    private static final double ANGLE_OFFSET_LIMIT = 10.0;
    private final BufferedImage image;
    private final BufferedImage background;
    private final BufferedImage overlay;
    //Vars
    private double theta;
    private double prevAngle;

    /**
     * Prive class constructor. Instantiated from static createDial function
     * call.
     *
     * @param tiltImage       - The image effected by angle rotation
     * @param backgroundImage - the background image of the dial widget
     * @param foregroundImage - the foreground image of the dial widget
     */
    private AngleWidget(BufferedImage tiltImage, BufferedImage backgroundImage,
                        BufferedImage foregroundImage) {
        image = tiltImage;
        background = backgroundImage;
        overlay = foregroundImage;
        prevAngle = 0.0;

        this.setPreferredSize(
                new Dimension(background.getWidth(), background.getHeight()));
        this.setMaximumSize(
                new Dimension(background.getWidth(), background.getHeight()));
        setOpaque(false);
    }

    /**
     * Create a new AngleWidget instance that displays the values in a given
     * telemetry index interpreted as angles by applying them to an image in
     * a dial
     *
     * @param ctx         Dashboard context instance
     * @param telemetryID Telemetry index with angles in degrees
     * @param indicator   The image to rotate according to the telemetry data
     */
    public static AngleWidget createDial(Context ctx, int telemetryID,
                                         BufferedImage indicator) {

        AngleWidget aw = new AngleWidget(
                indicator, ctx.theme.gaugeBackground, ctx.theme.gaugeGlare);
        ctx.telemetry.registerListener(telemetryID, aw);
        return aw;
    }

    /**
     * Paints the widget image at the current angle offset determined
     * by incoming telemetry angle values.
     *
     * @param g - The graphics reference.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        final int bgXC = background.getWidth() / 2;
        final int bgYC = background.getHeight() / 2;
        final int imageXC = image.getWidth() / 2;
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

    /**
     * Updates the angle offset of the widgets main angle image. If the change
     * in angle exceeds ANGLE_OFFSET_LIMIT then the change is considered to be
     * an outlier and unlikely to be realistic. This change is then ignored,
     * and no update occurs.
     *
     * @param angle
     */
    public void update(double angle) {
        //TODO - CP - Determine a method of smoothing out one or more outlier
        //reading to smooth out data hiccups. Some kind of rolling average could
        //work, but what happens if we get multiple wrong values in a row. how
        //do we determine that the change is real...how many samples have to
        //remain in range before it is considered a normal change? If we wait
        //too long to collect samples it will delay the widget update and look
        //bad

        //Known
        //Update rate is ~110ms

        //Need to know:
        //how many outliers in a row can we get/what is reasonable to account for?
        //How big should the average be? Don't want to take too long to update.
        //How do we account for different widget types or does it matter?
        //	(there is no indication currently)
        //What offset should we consider an outlier? is this universal
        //	across widget types?

        if (Math.abs(prevAngle - angle) > ANGLE_OFFSET_LIMIT) {
            System.out.println(
                    "AngleWidget - Maximum change between angles too great. Ignoring Outlier");
            prevAngle = angle;
            return;
        }

        theta = (angle * Math.PI) / 180;
        prevAngle = angle;
        repaint();
    }
}

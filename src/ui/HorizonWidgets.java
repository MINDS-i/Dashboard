package com.ui;

import com.telemetry.TelemetryListener;
import com.ui.ninePatch.*;
import com.ui.ArtificialHorizon;
import com.Context;

import java.io.Reader;
import java.io.FileReader;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.FontMetrics;

import javax.xml.stream.*;
import java.text.ParseException;

public class HorizonWidgets{
    public interface SetupCallback{
        void setup(ArtificialHorizon ah);
    }
    /**
     * Create a horizon widget jpanel in a `size` by `size` square
     *   Calls `sc.setup` with the internal ArtificialHorizon instance
     *   so it can be linked to the data input side/configured as needed
     */
    public static JPanel makeHorizonWidget(Context ctx, int size, SetupCallback sc){
        /**
         * The drawable margins are actually a property of the nine patch
         *   resource's images, but the patches currently don't have a way
         *   of storing that information so this will make it look correctly
         *   until a more general way is implemented.
         */
        JPanel container = new JPanel(){
            private NinePatch np = ctx.theme.screenPatch;
            private final int LEFT_MARGIN   = 13;
            private final int RIGHT_MARGIN  = 13;
            private final int TOP_MARGIN    = 9;
            private final int BOTTOM_MARGIN = 46;
            private Rectangle drawRect = new Rectangle(
                LEFT_MARGIN,
                TOP_MARGIN,
                size-RIGHT_MARGIN-LEFT_MARGIN,
                size-BOTTOM_MARGIN-TOP_MARGIN
                );
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setClip(drawRect);
                g2d.translate(-size/2+drawRect.getCenterX(),
                              -size/2+drawRect.getCenterY());
                super.paint(g2d);
                g2d.dispose();

                np.paintIn(g, getWidth(), getHeight());
            }
        };

        ArtificialHorizon horizon = new ArtificialHorizon(
            ()->{container.repaint();}
        );
        sc.setup(horizon);

        container.add(horizon);
        container.setPreferredSize(new Dimension(size,size));
        horizon.setPreferredSize(new Dimension(size,size));
        container.setOpaque(false);
        return container;
    }

    /**
     * Create a new popup window with a large artificial horizon in it.
     *   It is set to DISPOSE_ON_CLOSE by default.
     *   Calls `sc.setup` with the internal ArtificialHorizon instance
     *   so it can be linked to the data input side/configured as needed
     */
    public static JFrame makeHorizonWindow(Context ctx, SetupCallback sc){
        ArtificialHorizon ah = new ArtificialHorizon();
        sc.setup(ah);

        JFrame f = new JFrame("Artificial Horizon");
        f.add(ah);
        f.pack();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setSize(800, 800);

        return f;
    }
}

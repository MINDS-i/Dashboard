package com.ui;

import com.ui.TelemetryListener;
import com.ui.ninePatch.*;
import com.ui.ArtificialHorizon;
import com.Context;

import java.io.Reader;
import java.io.FileReader;
import java.util.*;
import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.FontMetrics;

import javax.xml.stream.*;
import java.text.ParseException;

public class HorizonWidgets{
    public static JPanel makeHorizonWidget(Context ctx, int size){
        JPanel container = new JPanel(){
            /**
             * The drawable margins are actually a property of the nine patch
             *   resource's images, but the patches currently don't have a way
             *   of storing that information so this will make it look correctly
             *   until a more general way is implemented.
             */
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
        //JPanel container = new JPanel();
        JPanel horizon = new ArtificialHorizon();
        container.add(horizon);
        container.setPreferredSize(new Dimension(size,size));
        horizon.setPreferredSize(new Dimension(size,size));
        container.setOpaque(false);
        return container;
    }
}

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
    public static JPanel makeHorizonWidget(Context ctx){
        /*JPanel container = new JPanel(){
            private NinePatch np = ctx.theme.screenPatch;
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                np.paintIn(g, getWidth(), getHeight());
            }
        };*/
        JPanel container = new JPanel();
        JPanel horizon = new ArtificialHorizon();
        container.add(horizon);
        container.setPreferredSize(new Dimension(140,140));
        horizon.setPreferredSize(new Dimension(140,140));
        container.setOpaque(false);
        return container;
    }
}

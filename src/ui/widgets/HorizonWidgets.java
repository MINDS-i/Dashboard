package com.ui.widgets;

import com.Context;
import com.ui.ArtificialHorizon;
import com.ui.ninePatch.TransparentPanel;

import javax.swing.*;
import java.awt.*;

public class HorizonWidgets {
    /**
     * Create a horizon widget jpanel in a `size` by `size` square
     * Calls `sc.setup` with the internal ArtificialHorizon instance
     * so it can be linked to the data input side/configured as needed
     */
    public static JPanel makeHorizonWidget(Context ctx, int size, SetupCallback sc) {
        JPanel container = new TransparentPanel(ctx, size);

        ArtificialHorizon horizon = new ArtificialHorizon(
                () -> {
                    container.repaint();
                }
        );
        sc.setup(horizon);

        container.add(horizon);
        horizon.setPreferredSize(new Dimension(size, size));
        return container;
    }

    /**
     * Create a new popup window with a large artificial horizon in it.
     * It is set to DISPOSE_ON_CLOSE by default.
     * Calls `sc.setup` with the internal ArtificialHorizon instance
     * so it can be linked to the data input side/configured as needed
     */
    public static JFrame makeHorizonWindow(Context ctx, SetupCallback sc) {
        ArtificialHorizon ah = new ArtificialHorizon();
        sc.setup(ah);

        JFrame f = new JFrame("Artificial Horizon");
        f.add(ah);
        f.pack();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setSize(800, 800);

        return f;
    }

    public interface SetupCallback {
        void setup(ArtificialHorizon ah);
    }
}

package com.ui.ninePatch;

import javax.swing.*;
import java.awt.*;

public class NinePatchPanel extends JPanel {
    private final NinePatch np;

    public NinePatchPanel(NinePatch np) {
        super();
        this.np = np;
        setOpaque(false);
        setMinimumSize(np.minimumSize());
    }

    @Override
    public void paintComponent(Graphics g) {
        np.paintIn(g, getWidth(), getHeight());
    }
}
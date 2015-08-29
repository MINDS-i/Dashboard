package com.ui.ninePatch;

import com.ui.ninePatch.NinePatch;
import javax.swing.*;
import java.awt.*;

public class NinePatchPanel extends JPanel{
    private NinePatch np;
    public NinePatchPanel(NinePatch np){
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

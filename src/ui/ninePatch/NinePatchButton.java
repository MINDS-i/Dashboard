package com.ui.ninePatch;

import com.ui.ninePatch.NinePatch;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class NinePatchButton extends JButton{
    private NinePatch normal;
    /**
     * Add a second NinePatch for the hovered and/or pressed version of the button
     */
    public NinePatchButton(NinePatch normal, Action action){
        super(action);
        this.normal = normal;
        setOpaque(false);
        setBorderPainted(false);
    }
    public NinePatchButton(NinePatch normal, String label){
        super(label);
        this.normal = normal;
        setOpaque(false);
        setBorderPainted(false);
    }
    @Override
    public void paintComponent(Graphics g) {
        normal.paintIn(g, getWidth(), getHeight());
        g.setFont(getFont());
        int stringLen = (int) g.getFontMetrics().getStringBounds(getText(), g).getWidth();
        int start = getWidth()/2 - stringLen/2;
        g.drawString(getText(), start, getHeight()/2);
    }
}

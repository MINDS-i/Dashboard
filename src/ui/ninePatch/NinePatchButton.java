package com.ui.ninePatch;

import javax.swing.*;
import java.awt.*;

public class NinePatchButton extends JButton {
    private final NinePatch normal;
    private NinePatch pressed;
    private NinePatch hover;

    public NinePatchButton(NinePatch normal) {
        super();
        this.normal = normal;
        configure();
    }

    public NinePatchButton(NinePatch normal, Action action) {
        super(action);
        this.normal = normal;
        configure();
    }

    public NinePatchButton(NinePatch normal, String label) {
        super(label);
        this.normal = normal;
        configure();
    }

    private void configure() {
        setOpaque(false);
        setBorderPainted(false);
        setMinimumSize(normal.minimumSize());
    }

    public void setHoverPatch(NinePatch hover) {
        this.hover = hover;
    }

    public void setPressedPatch(NinePatch pressed) {
        this.pressed = pressed;
    }

    @Override
    public void paintComponent(Graphics g) {
        ButtonModel model = getModel();
        NinePatch np = normal;
        if (model.isPressed() && pressed != null) {
            np = pressed;
        }
        else if (model.isRollover() && hover != null) {
            np = hover;
        }

        np.paintIn(g, getWidth(), getHeight());
        g.setFont(getFont());
        int stringWidth = (int) g.getFontMetrics().getStringBounds(getText(), g).getWidth();
        int stringHeight = (int) g.getFontMetrics().getStringBounds(getText(), g).getHeight();
        int startX = getWidth() / 2 - stringWidth / 2;
        int startY = getHeight() / 2 + stringHeight / 2;
        g.drawString(getText(), startX, startY);
    }
}

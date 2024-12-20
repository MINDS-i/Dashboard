package com.layer;

import com.map.command.CommandManager;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * LayerManager distributes mouse events and chances to draw on a set
 * of Layer objects sorted with largest "getZ" an "top"
 * <p>
 * Mouse clicks and presses are passed down the layers until a layer claims the
 * event by return true in its handler. Subsequent events associated with a
 * claimed event are passed directly to the claimant.
 */

public class LayerManager extends MouseAdapter {
    private final List<Layer> layers = new ArrayList<>();
    //mouse adapter code
    Layer active = null;

    public void add(Layer l) {
        layers.add(l);
        //sort collection by z index
        layers.sort((a, b) -> a.getZ() - b.getZ());
    }

    public void draw(Graphics g) {
        Graphics gn = g.create();
        for (Layer l : layers) {
            l.paint(gn);
        }

        if (CommandManager.getInstance().getGeofence().getIsEnabled()) {
            CommandManager.getInstance().getGeofence().paintFence(gn);
        }

        gn.dispose();
    }

    public void mouseClicked(MouseEvent e) {
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer here = layers.get(i);
            if (here.onClick(e)) {
                return;
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        active = null;
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer here = layers.get(i);
            if (here.onPress(e)) {
                active = here;
                return;
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (active != null) {
            active.onDrag(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (active != null) {
            active.onRelease(e);
        }
    }
}

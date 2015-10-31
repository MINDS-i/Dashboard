package com.layer;

import java.util.*;
import java.awt.geom.Point2D;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.*;

public class LayerManager extends MouseAdapter{
    private List<Layer> layers = new ArrayList<Layer>();

    public void add(Layer l){
        layers.add(l);
        //sort collection by z index
        Collections.sort(layers, new Comparator<Layer>(){
            @Override
            public int compare(Layer a, Layer b){
                return a.getZ() - b.getZ();
            }
        });
    }

    public void draw(Graphics g){
        Graphics gn = g.create();
        for(Layer l : layers){
            l.paint(gn);
        }
        gn.dispose();
    }

    //mouse adapter code
    Layer active = null;
    public void mouseClicked(MouseEvent e) {
        for(int i = layers.size()-1; i >= 0; i--){
            Layer here = layers.get(i);
            if(here.onClick(e)) return;
        }
    }
    public void mousePressed(MouseEvent e) {
        active = null;
        for(int i = layers.size()-1; i >= 0; i--){
            Layer here = layers.get(i);
            if(here.onPress(e)){
                active = here;
                return;
            }
        }
    }
    public void mouseDragged(MouseEvent e) {
        if(active != null){
            active.onDrag(e);
        }
    }
    public void mouseReleased(MouseEvent e) {
        if(active != null){
            active.onRelease(e);
        }
    }
}

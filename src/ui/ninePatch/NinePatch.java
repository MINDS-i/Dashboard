package com.ui.ninePatch;

import java.awt.*;
import java.awt.FontMetrics;
import java.awt.image.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.geom.*;
import java.io.*;

public class NinePatch{
    //image arrays counterclockwise from top left
    private final BufferedImage center;
    private enum Walls{ TOP, LEFT, RIGHT, BOTTOM }
    private enum Corner{ TOP_LEFT, TOP_RIGHT, LOWER_LEFT, LOWER_RIGHT }
    private final EnumMap<Walls, BufferedImage> walls
                    = new EnumMap<Walls, BufferedImage>(Walls.class);
    private final EnumMap<Corner, BufferedImage> joint
                    = new EnumMap<Corner, BufferedImage>(Corner.class);
    private final int edgeWidth;
    private final int edgeHeight;

    /**
     * Test with small, even and odd sised corners
     * test with non-square corners
     * fix edge rotation
     */


    static BufferedImage copyRotated(BufferedImage bi, double rotation){
        //paramaterize rotation
        double rot = Math.toRadians(rotation);
        int newWidth  = (int) Math.abs(bi.getWidth()*Math.cos(rot) ) +
                        (int) Math.abs(bi.getHeight()*Math.sin(rot))  ;
        int newHeight = (int) Math.abs(bi.getWidth()*Math.sin(rot) ) +
                        (int) Math.abs(bi.getHeight()*Math.cos(rot))  ;
        //make compatible, rotated canvas
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = cm.createCompatibleWritableRaster(newWidth, newHeight);
        BufferedImage ni = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        //define transform to do rotation;
        Graphics2D g = ni.createGraphics();
        AffineTransform tx = new AffineTransform();
        tx.translate(newWidth/2.0d, newHeight/2.0d); //move from 0,0 to new image center
        tx.rotate(rot); //perform rotation
        tx.translate(-bi.getWidth()/2.0d, -bi.getHeight()/2.0d); //move image to 0,0 for rotation
        //draw the rotation into the new image
        g.drawImage(bi, tx, null);

        return ni;
    }

    private NinePatch(){
        edgeWidth  = 0;
        edgeHeight = 0;
        center = null;
    }
    public NinePatch(BufferedImage center, BufferedImage wall, BufferedImage corner){
        this.center = center;

        this.walls.put(Walls.TOP, copyRotated(wall, 0));
        this.walls.put(Walls.LEFT, copyRotated(wall, -90));
        this.walls.put(Walls.RIGHT, copyRotated(wall, 90));
        this.walls.put(Walls.BOTTOM, copyRotated(wall, 180));

        this.joint.put(Corner.TOP_LEFT, copyRotated(corner, 0));
        this.joint.put(Corner.TOP_RIGHT, copyRotated(corner, -90));
        this.joint.put(Corner.LOWER_LEFT, copyRotated(corner, 90));
        this.joint.put(Corner.LOWER_RIGHT, copyRotated(corner, 180));

        edgeWidth = corner.getWidth();
        edgeHeight = corner.getHeight();
    }
    public NinePatch(BufferedImage center, BufferedImage[] walls, BufferedImage[] corners){
        this.center = center;
        this.walls.put(Walls.TOP, walls[0]);
        this.walls.put(Walls.LEFT, walls[1]);
        this.walls.put(Walls.RIGHT, walls[2]);
        this.walls.put(Walls.BOTTOM, walls[3]);

        this.joint.put(Corner.TOP_LEFT, corners[0]);
        this.joint.put(Corner.TOP_RIGHT, corners[1]);
        this.joint.put(Corner.LOWER_LEFT, corners[2]);
        this.joint.put(Corner.LOWER_RIGHT, corners[3]);

        edgeWidth = corners[0].getWidth();
        edgeHeight = corners[0].getHeight();
    }

    public BufferedImage getAt(int width, int height){
        return null;
    }

    public void paintIn(Graphics g, int width, int height){
        System.out.println("Painting a 9-patch");
        Graphics2D g2d = (Graphics2D) g;

        final int right = width - edgeWidth;
        final int base  = height - edgeHeight;

        g.drawImage(joint.get(Corner.TOP_LEFT)   ,     0,    0,null);
        g.drawImage(joint.get(Corner.TOP_RIGHT)  , right,    0,null);
        g.drawImage(joint.get(Corner.LOWER_LEFT) ,     0, base,null);
        g.drawImage(joint.get(Corner.LOWER_RIGHT), right, base,null);

        for(int x = edgeWidth; x<right; x++){
            g.drawImage(walls.get(Walls.TOP),    x,    0, null);
            g.drawImage(walls.get(Walls.BOTTOM), x, base, null);
        }

        for(int y = edgeHeight; y<base; y++){
            g.drawImage(walls.get(Walls.LEFT),      0, y, null);
            g.drawImage(walls.get(Walls.RIGHT), right, y, null);
        }

        for(int x = edgeWidth; x<right; x++){
            for(int y = edgeHeight; y<base; y++){
                g.drawImage(center, x, y, null);
            }
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("9-Patch Test");

        NinePatch test = new NinePatch();
        try{
            test = new NinePatch(ImageIO.read(new File("./data/nP/center.png")),
                                 ImageIO.read(new File("./data/nP/edge.png"  )),
                                 ImageIO.read(new File("./data/nP/corner.png")) );
        } catch (Exception e){
            e.printStackTrace();
        }

        final NinePatch drawnPatch = test;

        JPanel draw9patch = new JPanel(){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                drawnPatch.paintIn(g, getWidth(), getHeight());
            }
        };

        f.add(draw9patch);
        f.pack();
        f.setVisible(true);

        while(true){
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

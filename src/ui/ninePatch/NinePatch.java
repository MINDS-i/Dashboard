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
    private final int edgeWidth, edgeHeight;
    private final int horzWallThickness,   vertWallThickness;
    private final int horzCenterThickness, vertCenterThickness;
    /**
     * Test with small, even and odd sised corners
     * test with non-square corners
     * throw illegal size exceptions if any constraint is violated
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
        horzWallThickness = 0;
        vertWallThickness = 0;
        horzCenterThickness = 0;
        vertCenterThickness = 0;
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
        horzWallThickness = wall.getWidth();
        vertWallThickness = wall.getWidth();
        horzCenterThickness = center.getWidth();
        vertCenterThickness = center.getHeight();
    }
    public NinePatch(BufferedImage center,
                     BufferedImage topWall,
                     BufferedImage sideWall,
                     BufferedImage corner){
       this.center = center;

        this.walls.put(Walls.LEFT,  copyRotated(sideWall,0));
        this.walls.put(Walls.RIGHT, copyRotated(sideWall, 180));

        this.walls.put(Walls.TOP,    copyRotated(topWall, 0));
        this.walls.put(Walls.BOTTOM, copyRotated(topWall, 180));

        this.joint.put(Corner.TOP_LEFT, copyRotated(corner, 0));
        this.joint.put(Corner.TOP_RIGHT, copyRotated(corner, -90));
        this.joint.put(Corner.LOWER_LEFT, copyRotated(corner, 90));
        this.joint.put(Corner.LOWER_RIGHT, copyRotated(corner, 180));

        edgeWidth  = corner.getWidth();
        edgeHeight = corner.getHeight();
        horzWallThickness = topWall.getWidth();
        vertWallThickness = sideWall.getHeight();
        horzCenterThickness = center.getWidth();
        vertCenterThickness = center.getHeight();
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
        horzWallThickness = walls[0].getWidth();
        vertWallThickness = walls[1].getHeight();
        horzCenterThickness = center.getWidth();
        vertCenterThickness = center.getHeight();
    }
    public BufferedImage getAt(int width, int height){
        BufferedImage me = new BufferedImage(width, height);
        Graphics2D g2d = me.crateGraphics();
        paintIn(g2d, width, height);
        return me;
    }
    public void paintIn(Graphics g, int width, int height){
        System.out.println("Painting a 9-patch");
        Graphics2D g2d = (Graphics2D) g;

        final int right = width - edgeWidth;
        final int base  = height - edgeHeight;

        for(int x = edgeWidth; x<right; x+=horzCenterThickness){
            for(int y = edgeHeight; y<base; y+=vertCenterThickness){
                g.drawImage(center, x, y, null);
            }
        }

        for(int x = edgeWidth; x<right; x+=horzWallThickness){
            g.drawImage(walls.get(Walls.TOP),    x,    0, null);
            g.drawImage(walls.get(Walls.BOTTOM), x, base, null);
        }

        for(int y = edgeHeight; y<base; y+=vertWallThickness){
            g.drawImage(walls.get(Walls.LEFT),      0, y, null);
            g.drawImage(walls.get(Walls.RIGHT), right, y, null);
        }

        g.drawImage(joint.get(Corner.TOP_LEFT)   ,     0,    0,null);
        g.drawImage(joint.get(Corner.TOP_RIGHT)  , right,    0,null);
        g.drawImage(joint.get(Corner.LOWER_LEFT) ,     0, base,null);
        g.drawImage(joint.get(Corner.LOWER_RIGHT), right, base,null);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("9-Patch Test");

        NinePatch test3 = new NinePatch();
        NinePatch test4 = new NinePatch();
        NinePatch test9 = new NinePatch();
        try{
            BufferedImage c1            = ImageIO.read(new File("./data/nP/c1.png"));
            BufferedImage c2            = ImageIO.read(new File("./data/nP/c2.png"));
            BufferedImage c3            = ImageIO.read(new File("./data/nP/c3.png"));
            BufferedImage c4            = ImageIO.read(new File("./data/nP/c4.png"));
            BufferedImage center        = ImageIO.read(new File("./data/nP/center.png"));
            BufferedImage corner        = ImageIO.read(new File("./data/nP/corner.png"));
            BufferedImage edge          = ImageIO.read(new File("./data/nP/edge.png"));
            BufferedImage horzGreenGrad = ImageIO.read(new File("./data/nP/horzGreenGrad.png"));
            BufferedImage spiral        = ImageIO.read(new File("./data/nP/spiral.png"));
            BufferedImage vertGreenGrad = ImageIO.read(new File("./data/nP/vertGreenGrad.png"));

            BufferedImage[] walls = new BufferedImage[]{
                vertGreenGrad, horzGreenGrad, horzGreenGrad, vertGreenGrad
            };
            BufferedImage[] corners = new BufferedImage[]{
                c1, c2, c3, c4
            };

            test3 = new NinePatch(center, edge, corner);
            test4 = new NinePatch(spiral, vertGreenGrad, horzGreenGrad, c1);
            test9 = new NinePatch(spiral, walls, corners);
        } catch (Exception e){
            e.printStackTrace();
        }

        class NinePatchPanel extends JPanel{
            private NinePatch np;
            NinePatchPanel(NinePatch np){
                this.np = np;
                this.setPreferredSize(new Dimension(200, 200));
            }
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                np.paintIn(g, getWidth(), getHeight());
            }
        }

        JPanel layoutPanel = new JPanel();
        layoutPanel.add(new NinePatchPanel(test3));
        layoutPanel.add(new NinePatchPanel(test4));
        layoutPanel.add(new NinePatchPanel(test9));
        f.add(layoutPanel);
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

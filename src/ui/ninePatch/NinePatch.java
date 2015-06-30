package com.ui.ninePatch;

import java.awt.*;
import java.awt.FontMetrics;
import java.awt.image.*;
//import java.awt.image.AffineTransformOp
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.geom.*;
import java.io.*;

import com.ui.BackgroundPanel;
import com.ui.ninePatch.NinePatchPanel;

public class NinePatch{
    private final BufferedImage center;
    private enum Walls{ TOP, LEFT, RIGHT, BOTTOM }
    private enum Corner{ TOP_LEFT, TOP_RIGHT, LOWER_LEFT, LOWER_RIGHT }
    private final EnumMap<Walls, BufferedImage> walls
                    = new EnumMap<Walls, BufferedImage>(Walls.class);
    private final EnumMap<Corner, BufferedImage> joint
                    = new EnumMap<Corner, BufferedImage>(Corner.class);

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
        g.dispose();

        return ni;
    }

    enum Flip { IDENTITY, HORIZONTAL, VERTICAL, BOTH };
    static BufferedImage copyFlipped(BufferedImage bi, Flip dir){
        AffineTransform tx = new AffineTransform();
        switch(dir){
            case IDENTITY:
                tx = new AffineTransform();
                break;
            case HORIZONTAL:
                tx = AffineTransform.getScaleInstance(-1, 1);
                tx.translate(-bi.getWidth(), 0);
                break;
            case VERTICAL:
                tx = AffineTransform.getScaleInstance(1, -1);
                tx.translate(0, -bi.getHeight());
                break;
            case BOTH:
                tx = AffineTransform.getScaleInstance(-1, -1);
                tx.translate(-bi.getWidth(), -bi.getHeight());
                break;
        }
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(bi, null);
    }

    private NinePatch(){
        center = null;
    }
    public NinePatch(BufferedImage center, BufferedImage wall, BufferedImage corner){
        this.center = center;

        this.walls.put(Walls.TOP, copyFlipped(wall, Flip.IDENTITY));
        this.walls.put(Walls.LEFT, copyRotated(wall, -90));
        this.walls.put(Walls.RIGHT, copyRotated(wall, 90));
        this.walls.put(Walls.BOTTOM, copyFlipped(wall, Flip.VERTICAL));

        this.joint.put(Corner.TOP_LEFT, copyFlipped(corner, Flip.IDENTITY));
        this.joint.put(Corner.TOP_RIGHT, copyFlipped(corner, Flip.HORIZONTAL));
        this.joint.put(Corner.LOWER_LEFT, copyFlipped(corner, Flip.VERTICAL));
        this.joint.put(Corner.LOWER_RIGHT, copyFlipped(corner, Flip.BOTH));
    }
    public NinePatch(BufferedImage center,
                     BufferedImage topWall,
                     BufferedImage sideWall,
                     BufferedImage corner){
        this.center = center;

        this.walls.put(Walls.LEFT,  copyFlipped(sideWall, Flip.IDENTITY));
        this.walls.put(Walls.RIGHT, copyFlipped(sideWall, Flip.BOTH));

        this.walls.put(Walls.TOP,    copyFlipped(topWall, Flip.IDENTITY));
        this.walls.put(Walls.BOTTOM, copyFlipped(topWall, Flip.BOTH));

        this.joint.put(Corner.TOP_LEFT, copyFlipped(corner, Flip.IDENTITY));
        this.joint.put(Corner.TOP_RIGHT, copyFlipped(corner, Flip.HORIZONTAL));
        this.joint.put(Corner.LOWER_LEFT, copyFlipped(corner, Flip.VERTICAL));
        this.joint.put(Corner.LOWER_RIGHT, copyFlipped(corner, Flip.BOTH));
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
    }
    public BufferedImage getImage(int width, int height){
        BufferedImage me = new BufferedImage(width, height, center.getType());
        Graphics2D g2d = me.createGraphics();
        paintIn(g2d, width, height);
        g2d.dispose();
        return me;
    }
    public void paintIn(Graphics g, int width, int height){
        Graphics2D g2d = (Graphics2D) g;

        final int cornerWidth    = joint.get(Corner.TOP_LEFT).getWidth();
        final int cornerHeight   = joint.get(Corner.TOP_LEFT).getHeight();
        final int vertWallHeight = walls.get(Walls.TOP).getHeight();
        final int vertWallWidth  = walls.get(Walls.TOP).getWidth();
        final int horzWallHeight = walls.get(Walls.LEFT).getHeight();
        final int horzWallWidth  = walls.get(Walls.LEFT).getWidth();
        final int vertCenterThickness = center.getHeight();
        final int horzCenterThickness = center.getWidth();

        final int right = width  - cornerWidth;
        final int base  = height - cornerHeight;
        final int bottomDrawPoint = height - vertWallHeight;
        final int rightDrawPoint  = width  - horzWallWidth;

        class Texture{
            private Graphics2D g;
            Texture(Graphics2D g2d, BufferedImage bi){
                g = (Graphics2D) g2d.create();
                g.setPaint(new TexturePaint(bi,
                                new Rectangle2D.Float(0f, 0f,
                                    (float)bi.getWidth(), (float)bi.getHeight())));
            }
            void draw(int x1, int y1, int x2, int y2){
                g.fillRect(x1, y1, x2-x1, y2-y1);
            }
            void dispose() { g.dispose(); }
        }


        Texture centerFill = new Texture(g2d,center);
        //main center rectangle through left/right wall holes
        centerFill.draw(cornerWidth, vertWallHeight, right, bottomDrawPoint);
        //fill eclipsed body section on the left
        centerFill.draw(horzWallWidth, cornerHeight, cornerWidth, base);
        //fill eclipsed body section on the right
        centerFill.draw(right, cornerHeight, rightDrawPoint, base);
        centerFill.dispose();

/*        Texture topWall = new Texture(g2d, walls.get(Walls.TOP));
        topWall.draw(cornerWidth, 0, right, vertWallHeight);
        topWall.dispose();

        Texture bottomWall = new Texture(g2d, walls.get(Walls.BOTTOM));
        bottomWall.draw(cornerWidth, bottomDrawPoint, right, bottomDrawPoint+vertWallHeight);
        bottomWall.dispose();*/

        //(new Texture(g2d, walls.get(Walls.TOP))).draw(cornerWidth, 0, right, horzWallHeight);
        //(new Texture(g2d, walls.get(Walls.TOP))).draw(cornerWidth, 0, right, horzWallHeight);
        //(new Texture(g2d, walls.get(Walls.TOP))).draw(cornerWidth, 0, right, horzWallHeight);

        //draw horizontal walls

        for(int x = cornerWidth; x<right; x+=horzWallHeight){
            g.drawImage(walls.get(Walls.TOP),    x,               0, null);
            g.drawImage(walls.get(Walls.BOTTOM), x, bottomDrawPoint, null);
        }

        //draw vertical walls
        for(int y = cornerHeight; y<base; y+=vertWallWidth){
            g.drawImage(walls.get(Walls.LEFT),               0, y, null);
            g.drawImage(walls.get(Walls.RIGHT), rightDrawPoint, y, null);
        }

        //draw four corners
        final BufferedImage tl = joint.get(Corner.TOP_LEFT);
        g.drawImage(tl, 0, 0,null);
        final BufferedImage tr = joint.get(Corner.TOP_RIGHT);
        g.drawImage(tr , width-tr.getWidth(),    0,null);
        final BufferedImage ll = joint.get(Corner.LOWER_LEFT);
        g.drawImage(ll, 0, height-ll.getHeight(), null);
        final BufferedImage lr = joint.get(Corner.LOWER_RIGHT);
        g.drawImage(lr, width-lr.getWidth(), height-lr.getHeight(), null);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("9-Patch Test");

        NinePatch test3 = new NinePatch();
        NinePatch test4 = new NinePatch();
        NinePatch test9 = new NinePatch();
        NinePatch testOdd = new NinePatch();
        NinePatch testButton = new NinePatch();
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

            BufferedImage oddCorner = ImageIO.read(new File("./data/nP/oddSize/corner.png"));
            BufferedImage oddHorz   = ImageIO.read(new File("./data/nP/oddSize/horzWall.png"));
            BufferedImage oddVert   = ImageIO.read(new File("./data/nP/oddSize/vertWall.png"));
            BufferedImage oddCenter = ImageIO.read(new File("./data/nP/oddSize/whiteCenter.png"));

            BufferedImage[] walls = new BufferedImage[]{
                vertGreenGrad, horzGreenGrad, horzGreenGrad, vertGreenGrad
            };
            BufferedImage[] corners = new BufferedImage[]{
                c1, c2, c3, c4
            };

/*            BufferedImage buttonCenter = ImageIO.read(new File("./data/nP/blueButton/center.png"));
            BufferedImage[] buttonWalls = new BufferedImage[]{
                ImageIO.read(new File("./data/nP/blueButton/top.png")),
                ImageIO.read(new File("./data/nP/blueButton/left.png")),
                ImageIO.read(new File("./data/nP/blueButton/right.png")),
                ImageIO.read(new File("./data/nP/blueButton/bottom.png")) };
            BufferedImage[] buttonJoints = new BufferedImage[]{
                ImageIO.read(new File("./data/nP/blueButton/topLeft.png")),
                ImageIO.read(new File("./data/nP/blueButton/topRight.png")),
                ImageIO.read(new File("./data/nP/blueButton/lowerLeft.png")),
                ImageIO.read(new File("./data/nP/blueButton/lowerRight.png")) };*/

            BufferedImage buttonCenter = ImageIO.read(new File("./data/nP/display/Middle.png"));
            BufferedImage[] buttonWalls = new BufferedImage[]{
                ImageIO.read(new File("./data/nP/display/TopBorder.png")),
                ImageIO.read(new File("./data/nP/display/LeftBorder.png")),
                ImageIO.read(new File("./data/nP/display/RightBorder.png")),
                ImageIO.read(new File("./data/nP/display/BottomBorder.png")) };
            BufferedImage[] buttonJoints = new BufferedImage[]{
                ImageIO.read(new File("./data/nP/display/TopLeft.png")),
                ImageIO.read(new File("./data/nP/display/TopRight.png")),
                ImageIO.read(new File("./data/nP/display/BottomLeft.png")),
                ImageIO.read(new File("./data/nP/display/BottomRight.png")) };

            test3 = new NinePatch(center, edge, corner);
            test4 = new NinePatch(spiral, vertGreenGrad, horzGreenGrad, c1);
            test9 = new NinePatch(spiral, walls, corners);
            testOdd = new NinePatch(oddCenter, oddVert, oddHorz, oddCorner);
            testButton = new NinePatch(buttonCenter, buttonWalls, buttonJoints);
        } catch (Exception e){
            e.printStackTrace();
        }

        JPanel layoutPanel = new JPanel();
        JComponent[] panels = new JComponent[]{
            new NinePatchPanel(test3)
            ,new NinePatchPanel(test4)
            ,new NinePatchPanel(test9)
            ,new BackgroundPanel(test9.getImage(200,200))
            ,new NinePatchPanel(testOdd)
            ,new NinePatchButton(testButton, "Hello")
        };

        for(JComponent panel: panels){
            panel.setPreferredSize(new Dimension(400, 400));
            layoutPanel.add(panel);
        }

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
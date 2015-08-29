package com.ui.ninePatch;

import java.awt.*;
import java.awt.FontMetrics;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

import com.ui.BackgroundPanel;
import com.ui.ninePatch.NinePatchPanel;

public class NinePatch{
    private final BufferedImage middle;
    private final BufferedImage topBorder;
    private final BufferedImage leftBorder;
    private final BufferedImage rightBorder;
    private final BufferedImage bottomBorder;
    private final BufferedImage topLeft;
    private final BufferedImage topRight;
    private final BufferedImage bottomLeft;
    private final BufferedImage bottomRight;

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
        middle       = null;
        topBorder    = null;
        leftBorder   = null;
        rightBorder  = null;
        bottomBorder = null;
        topLeft      = null;
        topRight     = null;
        bottomLeft   = null;
        bottomRight  = null;
    }
    public NinePatch(BufferedImage center, BufferedImage wall, BufferedImage corner){
        middle       = center;
        topBorder    = copyFlipped(wall, Flip.IDENTITY);
        leftBorder   = copyRotated(wall, -90);
        rightBorder  = copyRotated(wall, 90);
        bottomBorder = copyFlipped(wall, Flip.VERTICAL);
        topLeft      = copyFlipped(corner, Flip.IDENTITY);
        topRight     = copyFlipped(corner, Flip.HORIZONTAL);
        bottomLeft   = copyFlipped(corner, Flip.VERTICAL);
        bottomRight  = copyFlipped(corner, Flip.BOTH);
    }
    public NinePatch(BufferedImage center,
                     BufferedImage topWall,
                     BufferedImage sideWall,
                     BufferedImage corner){
        middle       = center;
        leftBorder   = copyFlipped(sideWall, Flip.IDENTITY);
        rightBorder  = copyFlipped(sideWall, Flip.BOTH);
        topBorder    = copyFlipped(topWall, Flip.IDENTITY);
        bottomBorder = copyFlipped(topWall, Flip.BOTH);
        topLeft      = copyFlipped(corner, Flip.IDENTITY);
        topRight     = copyFlipped(corner, Flip.HORIZONTAL);
        bottomLeft   = copyFlipped(corner, Flip.VERTICAL);
        bottomRight  = copyFlipped(corner, Flip.BOTH);
    }
    public NinePatch(BufferedImage center, BufferedImage[] walls, BufferedImage[] corners){
        middle       = center;
        topBorder    = walls[0];
        leftBorder   = walls[1];
        rightBorder  = walls[2];
        bottomBorder = walls[3];
        topLeft      = corners[0];
        topRight     = corners[1];
        bottomLeft   = corners[2];
        bottomRight  = corners[3];
    }

    public static NinePatch loadFrom(Path dir) throws IOException {
        BufferedImage center = ImageIO.read(dir.resolve("Middle.png").toFile());
        BufferedImage[] walls = new BufferedImage[]{
            ImageIO.read(dir.resolve("TopBorder.png").toFile()),
            ImageIO.read(dir.resolve("LeftBorder.png").toFile()),
            ImageIO.read(dir.resolve("RightBorder.png").toFile()),
            ImageIO.read(dir.resolve("BottomBorder.png").toFile()) };
        BufferedImage[] joints = new BufferedImage[]{
            ImageIO.read(dir.resolve("TopLeft.png").toFile()),
            ImageIO.read(dir.resolve("TopRight.png").toFile()),
            ImageIO.read(dir.resolve("BottomLeft.png").toFile()),
            ImageIO.read(dir.resolve("BottomRight.png").toFile()) };
        return new NinePatch(center, walls, joints);
    }

    public Dimension minimumSize(){
        //widest left corner + widest right corner
        int width = Math.max(topLeft.getWidth(), bottomLeft.getWidth()) +
                    Math.max(topRight.getWidth(), bottomRight.getWidth());
        //tallest top corner + tallest bottom corner
        int height = Math.max(topLeft.getHeight(), topRight.getHeight()) +
                     Math.max(bottomLeft.getHeight(), bottomRight.getHeight());
        return new Dimension(width, height);
    }

    public BufferedImage getImage(int width, int height){
        BufferedImage me = new BufferedImage(width, height, middle.getType());
        Graphics2D g2d = me.createGraphics();
        paintIn(g2d, width, height);
        g2d.dispose();
        return me;
    }

    public void paintIn(Graphics g, int width, int height){
        Graphics2D g2d = (Graphics2D) g;
        //fill between walls
        paintTexture(g2d,middle,
                     leftBorder.getWidth(), topBorder.getHeight(),
                     width-rightBorder.getWidth(), height-bottomBorder.getHeight() );

        //draw four corners
        g.drawImage(topLeft, 0, 0,null);
        g.drawImage(topRight , width-topRight.getWidth(),    0,null);
        g.drawImage(bottomLeft, 0, height-bottomLeft.getHeight(), null);
        g.drawImage(bottomRight, width-bottomRight.getWidth(), height-bottomRight.getHeight(), null);

        //draw walls
        paintTexture(g2d, leftBorder,
                     0, topLeft.getHeight(),
                     leftBorder.getWidth(), height-bottomLeft.getHeight());

        paintTexture(g2d, rightBorder,
                     width-rightBorder.getWidth(), topRight.getHeight(),
                     width, height-bottomRight.getHeight());

        paintTexture(g2d, topBorder,
                     topLeft.getWidth(), 0,
                     width-topRight.getWidth(), topBorder.getHeight());

        paintTexture(g2d, bottomBorder,
                     bottomLeft.getWidth(), height-bottomBorder.getHeight(),
                     width-bottomRight.getWidth(), height);
    }

    private void paintTexture(Graphics2D g, BufferedImage bi,
                             int x1, int y1, int x2, int y2  ){
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x1, y1);
        g2d.setPaint(new TexturePaint(bi,
                        new Rectangle2D.Float(0f, 0f,
                            (float)bi.getWidth(), (float)bi.getHeight())));
        g2d.fillRect(0,0,x2-x1,y2-y1);
        g2d.dispose();
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

            test3 = new NinePatch(center, edge, corner);
            test4 = new NinePatch(spiral, vertGreenGrad, horzGreenGrad, c1);
            test9 = new NinePatch(spiral, walls, corners);
            testOdd = new NinePatch(oddCenter, oddVert, oddHorz, oddCorner);
            testButton = NinePatch.loadFrom(Paths.get("./data/nP/screen"));
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

        //f.add(new NinePatchPanel(testButton));
        f.add(layoutPanel);
        f.pack();
        f.setVisible(true);

        while(f.isShowing()){
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

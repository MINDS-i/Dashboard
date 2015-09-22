package com.map;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.*;
import javax.swing.*;

class TileServer implements MapSource{
    private static final int TILE_SIZE = 256;
    private static final int MAX_Z = 18; //max zoom level
    private static final int MAX_X = (1 << MAX_Z) / TILE_SIZE;
    private static final int MAX_Y = (1 << MAX_Z) / TILE_SIZE;
    private static final int CACHE_SIZE = 64;
    //maximum number of concurrent tile load requests
    private static final int CC_REQUEST = 1;

    private final static Image dummyTile = new BufferedImage(1,1,
                                                   BufferedImage.TYPE_INT_ARGB);

    private final String rootURL;
    private Queue<Image> oldTiles = new LinkedList<Image>();
    private Queue<TileTag> requestedTiles = new LinkedList<TileTag>();
    private Map<TileTag, Image> cache = new ConcurrentHashMap<TileTag, Image>(
                                        CACHE_SIZE+1, 1.0f, CC_REQUEST);

    TileServer(String url){
        this.rootURL = url;
    }

    public void clear(){
        cache.clear();
        oldTiles.clear();
        requestedTiles.clear();
        //kill off load threads
    }

    public void paint(Graphics2D gd, Point2D center, int scale, int width, int height){
        Graphics2D g2d = (Graphics2D) gd.create();
        int zoom = 31 - Integer.numberOfLeadingZeros(scale / TILE_SIZE);
        //pixel positions of latitude and longitude center point
        int sLon = (int)((center.getX()+90.0) * (double)scale / 180.0) - (width / 2);
        int sLat = (int)((center.getY()+90.0) * (double)scale / 180.0) - (height/ 2);
        //cow/col index of the top left corner
        int rowB = sLon/TILE_SIZE;
        int colB = sLat/TILE_SIZE;
        //X,Y coordinate that top left corner tile should be displayed at
        int minX = -sLon%TILE_SIZE;
        int minY = -sLat%TILE_SIZE;
        //number of tiles along width/height
        int tcw  = ((width -minX)/TILE_SIZE)+1;
        int tch  = ((height-minY)/TILE_SIZE)+1;

        /*
        System.out.println("minX "+minX+" minY "+minY);
        Image img = pollImage(new TileTag(rowB, colB, zoom));
        g2d.drawImage(img, minX, minY, null);
        */

        for(int row = 0; row < tcw; row++){
            for(int col = 0; col < tch; col++){
                Image img = pollImage(new TileTag(row+rowB, col+colB, zoom));
                g2d.drawImage(img, minX+row*TILE_SIZE, minY + col*TILE_SIZE,null);
            }
        }

        g2d.dispose();
    }

    Image pollImage(TileTag target){
        //return image from cache
        Image tile = cache.get(target);
        if(tile != null) return tile;

        loadTile(target);
        //requestedTiles.add(target);
        //if not cached, return dummy
        //  add URL to request queue
        //  make sure load threads are spun up
        return dummyTile;
    }

    private void loadTile(TileTag target){
        Runnable load = new Runnable(){
            public void run(){
                try {
                    Image img = Toolkit.getDefaultToolkit().getImage(target.getURL());
                    cache.put(target, img);
                    System.out.println("Loaded "+target);
                } catch (Exception e) {
                    System.err.println("failed to load tlie "+target);
                }
            }
        };
        (new Thread(load)).start();
    }

    class TileTag{
        int x,y,z;
        TileTag(int x, int y, int z){
            this.x = x;
            this.y = y;
            this.z = z;
        }
        @Override
        public int hashCode() {
            int result = x;
            result = result*(MAX_Y) + y;
            result = result*(MAX_Z) + z;
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            TileTag other = (TileTag) obj;
            if (x != other.x || y != other.y || z != other.z)
                return false;
            return true;
        }
        URL getURL() {
            StringBuilder sb = new StringBuilder(rootURL);
            sb.append("/");
            sb.append(z);
            sb.append("/");
            sb.append(x);
            sb.append("/");
            sb.append(y);
            sb.append(".png");
            URL res = null;
            try{
                res = new URL(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }
        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder("Tile @ ");
            sb.append("x: ");
            sb.append(x);
            sb.append(" y: ");
            sb.append(y);
            sb.append(" z: ");
            sb.append(z);
            return sb.toString();
        }
    }
}

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
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.*;
import javax.swing.*;

class TileServer implements MapSource {
    //Number of pixels a tile takes up
    private static final int TILE_SIZE = 256;
    //Maximum index any tile can have on Z
    private static final int MAX_Z = 18;
    //Maximum index any tile can have on X
    private static final int MAX_X = (1 << MAX_Z) / TILE_SIZE;
    //Maximum index any tile can have on Y
    private static final int MAX_Y = (1 << MAX_Z) / TILE_SIZE;
    //Maximum number of tiles to keep in the cache at any given moment
    private static final int CACHE_SIZE = 256;
    //maximum number of concurrent tile load requests
    private static final int CC_REQUEST   = 4;
    //How many tiles to remove from the cache whenever a sweep is done
    private static final int CLEAN_NUM    = 16;
    //Ratio of lateral to vertical tile priority
    private static final int Z_PRIORITY   = 6;
    //Number of offscreen tiles around the margins to try and load
    private static final int MARGIN_TILES = 2;
    //Dummy image to render when a tile that has not loaded is requested
    private static final Image dummyTile = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);

    private java.util.List<Component> repaintListeners = new LinkedList<Component>();
    private Map<TileTag, Image> cache = new HashMap<TileTag, Image>(CACHE_SIZE+1, 1.0f);
    private final String rootURL;
    private TileTag centerTag;

    TileServer(String url){
        this.rootURL = url;
    }

    public void clear(){
        if(tileLoader != null) {
            tileLoader.interrupt();
        }
        cache.clear();
    }

    public void paint(Graphics2D gd, Point2D center, int scale, int width, int height){
        Graphics2D g2d = (Graphics2D) gd.create();
        int zoom = 31 - Integer.numberOfLeadingZeros(scale / TILE_SIZE);
        //pixel positions of latitude and longitude center point
        int sLon = (int)((center.getX()+90.0) * (double)scale / 180.0) - (width / 2);
        int sLat = (int)((center.getY()+90.0) * (double)scale / 180.0) - (height/ 2);
        //cow/col Base - index of the top left corner
        int rowB = sLon/TILE_SIZE;
        int colB = sLat/TILE_SIZE;
        //X,Y coordinate that top left corner tile should be displayed at
        int minX = -sLon%TILE_SIZE;
        int minY = -sLat%TILE_SIZE;
        //number of tiles along width/height
        int tcw  = ((width -minX)/TILE_SIZE)+1;
        int tch  = ((height-minY)/TILE_SIZE)+1;

        for(int row = 0; row < tcw; row++){
            for(int col = 0; col < tch; col++){
                Image img = pollImage(new TileTag(row+rowB, col+colB, zoom));
                g2d.drawImage(img, minX+row*TILE_SIZE, minY + col*TILE_SIZE,null);
            }
        }
        g2d.dispose();

        TileTag newCenterTag = new TileTag(rowB + tcw/2, colB + tch/2, zoom);
        if(!newCenterTag.equals(centerTag)){
            centerTag = newCenterTag;
            launchTileLoader(centerTag, tcw, tch);
        }
    }

    Image pollImage(TileTag target){
        //return image from cache
        Image tile = cache.get(target);
        if(tile != null) return tile;

        return dummyTile;
    }

    private Thread tileLoader = null;
    private void launchTileLoader(TileTag center, int width, int height){
        if(tileLoader != null) {
            tileLoader.interrupt();
            //tileLoader.join();
        }
        tileLoader = new TileLoader(centerTag, width, height);
        tileLoader.start();
    }

    /**
     * Remove the CLEAN_NUM furthest tiles from the cache to make space
     */
    private void cleanTiles(){
        TileTag[] loaded = cache.keySet().toArray(new TileTag[]{});
        Arrays.sort(loaded, new TileDistCmp(centerTag, TileDistCmp.FURTHEST));
        for(int i=0; i<CLEAN_NUM; i++){
            cache.remove(loaded[i]);
        }
    }
    /**
     * Add given tile as image to the cache
     */
    private synchronized void addTile(TileTag tag, Image tile){
        if(cache.size() >= CACHE_SIZE){
            cleanTiles();
        }
        cache.put(tag, tile);
    }
    /**
     * Add listener to be repainted if the viewed map ever changes
     */
    public void addRepaintListener(Component c){
        repaintListeners.add(c);
    }
    /**
     * Remove listener to be repainted if the viewed map ever changes
     */
    public void removeRepaintListener(Component c){
        repaintListeners.remove(c);
    }
    /**
     * Notify listeners that the current map view may have changed
     */
    private void contentChanged(){
        for(Component c : repaintListeners){
            c.repaint();
        }
    }
    /**
     * Tile loader thread
     * This will load all the tiles around ref given width and height, as well
     * as all the tiles one level below that, in the order specified by the
     * TileDistCmp comparator
     */
    private class TileLoader extends Thread {
        private AtomicInteger helpers = new AtomicInteger();
        java.util.List<TileTag> toLoad = new ArrayList<TileTag>(64);
        final TileTag ref;
        final int width;
        final int height;
        TileLoader(TileTag ref, int width, int height){
            this.ref = ref;
            this.width = width;
            this.height = height;
        }
        public void run(){
            try{
                int hw = ((width +1)/2) + MARGIN_TILES;
                int hh = ((height+1)/2) + MARGIN_TILES;
                for(int row=-hw; row<hw; row++){
                    for(int col=-hh; col<hh; col++){
                        enqueueAround(new TileTag(ref.x+row, ref.y+col, ref.z));
                    }
                }
                if(interrupted()) return;
                Collections.sort(toLoad, new TileDistCmp(ref, TileDistCmp.CLOSEST));
                for(TileTag t : toLoad) {
                    if(interrupted()) return;
                    load(t);
                }
            } catch (Exception e) {

            }
        }
        void enqueueAround(TileTag tag){
            enqueue(tag);
            enqueue(new TileTag(tag.x*2+0, tag.y*2+0, tag.z+1));
            enqueue(new TileTag(tag.x*2+0, tag.y*2+1, tag.z+1));
            enqueue(new TileTag(tag.x*2+1, tag.y*2+0, tag.z+1));
            enqueue(new TileTag(tag.x*2+1, tag.y*2+1, tag.z+1));
        }
        void enqueue(TileTag tag){
            if(!cache.containsKey(tag)) toLoad.add(tag);
        }
        void load(TileTag tag){
            // if available helper thread, launch it
            // otherwise, block to get image

            System.out.println(this.hashCode()+" is loading "+tag);

            if (!(helpers.get() >= CC_REQUEST)) {
                (new Helper(new LoadTile(tag))).start();
            } else {
                (new LoadTile(tag)).run();
            }
            //(new LoadTile(tag)).run();
        }
        private class Helper extends Thread {
            Helper(Runnable r){
                super(r);
            }
            @Override
            public void run(){
                helpers.incrementAndGet();
                super.run();
                helpers.decrementAndGet();
            }
        }
    }
    /**
     * Runnable for fetching a given TileTag and putting it in the cache
     */
    private class LoadTile implements Runnable{
        final TileTag target;
        LoadTile(TileTag target){
            this.target = target;
        }
        public void run(){
            try {
                Image img = ImageIO.read(target.getURL(rootURL));
                addTile(target, img);
            } catch (Exception e) {
                System.err.println("failed to load "+target);
            } finally {
                contentChanged();
            }
        }
    }
    /**
     * Compares two TileTags based on which one is closer to the given tag
     * Z axis is weighten using Z_PRIORITY
     * This should allow components to select the tags most/least important
     * to the viewer based on their current viewport into the map
     */
    static class TileDistCmp implements Comparator<TileTag>{
        static final int FURTHEST = -1;
        static final int CLOSEST  =  1;
        final TileTag ref;
        final int dir;
        TileDistCmp(TileTag reference, int direction){
            ref = reference;
            dir = direction;
        }
        TileTag onRefZoom(TileTag n){
            int zdiff = ref.z - n.z;
            if (zdiff == 0) {
                return n;
            } else if (zdiff > 0) {
                //n is more zoomed out than ref
                int fact = (1 << zdiff);
                int newX = n.x * fact;
                int newY = n.y * fact;
                return new TileTag(newX, newY, ref.z);
            } else { //zdiff < 0
                //n is more zoomed in than ref
                int fact = (1 << -zdiff);
                int newX = n.x / fact;
                int newY = n.x / fact;
                return new TileTag(newX, newY, ref.z);
            }
        }
        int xyDiff(TileTag a, TileTag b){
            TileTag newA = onRefZoom(a);
            TileTag newB = onRefZoom(b);
            int adiff = Math.abs(ref.x - newA.x) + Math.abs(ref.y - newA.y);
            int bdiff = Math.abs(ref.x - newB.x) + Math.abs(ref.y - newB.y);
            return adiff - bdiff;
        }
        public int compare(TileTag a, TileTag b){
            int xydiff = xyDiff(a, b);
            int azdiff = Math.abs(ref.z - a.z);
            int bzdiff = Math.abs(ref.z - b.z);
            return dir * (xydiff + Z_PRIORITY * (azdiff - bzdiff));
        }
    }
    /**
     * TileTag contains refereces to x,y,z coordinates, defining equals
     * and hash code appropriately, and generating url's for standard
     * web-mercator protocols
     */
    static class TileTag{
        final int x,y,z;
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
        URL getURL(String rootURL) {
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
    /**
     * small set of tests for TileDistCmp and TileTag
     */
    private void testTileDist(){
        TileTag ref = new TileTag(4, 4, 3);
        Comparator<TileTag> cmp = new TileDistCmp(ref, TileDistCmp.CLOSEST);

        System.out.println("Hello from test Tile Dist Test");
        System.out.println(cmp.compare(new TileTag(4,4,3), ref)); // 0
        System.out.println(cmp.compare(new TileTag(0,4,3), ref)); // 4
        System.out.println(cmp.compare(new TileTag(4,0,3), ref)); // 4
        System.out.println(cmp.compare(new TileTag(2,2,2), ref)); // 4
        System.out.println(cmp.compare(new TileTag(8,8,4), ref)); // 4

        TileTag[] test = new TileTag[]{
            new TileTag(5,4,3),
            new TileTag(4,3,3),
            new TileTag(5,8,7),
            new TileTag(4,5,3),
            new TileTag(3,4,3),
            new TileTag(5,8,2),
            new TileTag(4,4,3),
            new TileTag(5,5,3),
            new TileTag(5,8,3)
        };

        Arrays.sort(test, cmp);

        for(TileTag t : test) System.out.println(t);
    }
}

package com.map;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MapSource implementation for web-mercator maps loaded from external
 * tile servers
 */
class TileServer implements MapSource {

    //label tiles on screen for debugging
    private static final boolean TILE_LABEL = false;

    //Number of pixels a tile takes up
    private static final int TILE_SIZE = 256;

    //Minimum index any tile can have on Z
    private static final int MIN_Z = 2;

    //Maximum index any tile can have on Z
    private static final int MAX_Z = 19;

    //Maximum index any tile can have on Y (Used in TileTag Hashcode calculation only)
    private static final int MAX_Y = (1 << MAX_Z) / TILE_SIZE;

    //Maximum number of tiles to keep in the cache at any given moment
    private static final int CACHE_SIZE = 256;

    //maximum number of concurrent tile load requests (Threads)
    private static final int CC_REQUEST = 12;

    //How many tiles to remove from the cache whenever a sweep is done
    private static final int CLEAN_NUM = 16;

    //Ratio of lateral to vertical tile priority
    private static final int Z_PRIORITY = 6;

    //rings of offscreen tiles around the margins to try and load
    private static final int CUR_ZOOM_MARGIN = 2;

    //rings of offscreen tiles on the next zoom to try and load
    private static final int NEXT_ZOOM_MARGIN = -1;

    //maximum percentage to zoom a tile before shrinking the layer below instead
    private static final float ZOOM_CROSSOVER = 1.30f;

    //Dummy image to render when a tile that has not loaded is requested
    private static final Image dummyTile = new BufferedImage(TILE_SIZE, TILE_SIZE,
            BufferedImage.TYPE_INT_ARGB);

    //List of listeners triggered to repaint when a change to the map tiles occurs
    private final java.util.List<Component> repaintListeners = new LinkedList<>();


    //Key value pairings for currently cached map tile data. Contains the following:
    // 		TileTag - XYZ and URL (Standard Web Mercator Protocol)
    // 		Image - that coincides with the tile tag information
    private final Map<TileTag, Image> cache =
            new ConcurrentHashMap<>(CACHE_SIZE + 1, 1.0f);

    //Main URL of the tile server to be used. specific tile addresses are 
    //concatenated to this
    private final String rootURL;

    // Keeps track of the map position on the last draw to trigger new tile loads
    private TileTag centerTag = new TileTag(0, 0, 0);
    /**
     * Instantiates and starts a new TileLoader thread. If one
     * is already running it is interrupted.
     *
     * @param width - The width in map tiles of the map
     * @param height - The Height in tiles of the map.
     */
    private Thread tileLoader = null;

    /**
     * Class Constructor
     *
     * @param url - The root url of the tile server.
     */
    TileServer(String url) {
        this.rootURL = url;
    }

    /**
     * Clears the current TileServer cache, interrupting tile loading threads if
     * necessary. Once the cache is cleared, the center TileTag is also reset to its
     * default/initial value in order to trigger tile loads immediately on the next draw.
     */
    public void clear() {
        if (tileLoader != null) {
            tileLoader.interrupt();
        }

        cache.clear();
        centerTag = new TileTag(0, 0, 0);
    }

    /**
     * Calculates the ratio between crossover tile layers based on the current zoom
     * level and then draws the current scaled layer.
     *
     * @param gd     - The Graphics2d class used for rendering and color management.
     * @param center - Center Point2D position of the tiles in cache to paint.
     * @param scale  - Scale of the current tile layer being drawn.
     * @param width  - Width of the tile set in cache
     * @param height - height of the tile set in cache
     */
    public void paint(Graphics2D gd, Point2D center, int scale, int width, int height) {
        Graphics2D g2d = (Graphics2D) gd.create();

        //ratio between zoom and the tile layer above it
        double zfix = (double) scale / (double) Integer.highestOneBit(scale);
        int zoom = (31 - Integer.numberOfLeadingZeros(scale / TILE_SIZE));

        if (zfix >= ZOOM_CROSSOVER) {
            zoom += 1;
            zfix /= 2.0;
        }

        //effective width/height after zoom correction
        double ewidth = (width / zfix);
        double eheight = (height / zfix);

        g2d.scale(zfix, zfix);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        /*
         * From here on, the graphics object is mapped so that if we draw
         * around the center of an ewidth by eheight screen at effs (effective scale),
         * it will fit in the orginal screen size zoomed correctly
         */

        //pixel positions of top left point
        double sLon = (center.getX() / zfix) - (ewidth / 2.0);
        double sLat = (center.getY() / zfix) - (eheight / 2.0);

        //row/col Base index in the top left corner
        int rowB = (int) (sLon / (double) TILE_SIZE);
        int colB = (int) (sLat / (double) TILE_SIZE);

        //X,Y shifts to keep the view in alignment
        int xalign = (int) -(sLon % TILE_SIZE);
        int yalign = (int) -(sLat % TILE_SIZE);

        //width/height in tiles
        int wit = (((int) ewidth - xalign) / TILE_SIZE) + 1;
        int hit = (((int) eheight - yalign) / TILE_SIZE) + 1;

        for (int row = 0; row < wit; row++) {
            for (int col = 0; col < hit; col++) {
                int x = xalign + (row * TILE_SIZE);
                int y = yalign + (col * TILE_SIZE);
                TileTag tile = new TileTag((row + rowB), (col + colB), zoom);
                Image img = pollImage(tile);
                g2d.drawImage(img, x, y, null);

                if (TILE_LABEL) {
                    g2d.setColor(Color.YELLOW);
                    g2d.drawString(tile.toString(), x, y);
                }
            }
        }
        g2d.dispose();

        // Start loading new tiles if the center tag has changed
        TileTag newCenterTag = new TileTag(rowB + (wit / 2), colB + (hit / 2), zoom);
        if (!newCenterTag.equals(centerTag)) {
            centerTag = newCenterTag;
            launchTileLoader((width / TILE_SIZE) + 1, (height / TILE_SIZE) + 1);
        }
    }

    /**
     * Determines if the given zoom level is within the acceptable range
     * determined by MIN_Z/MAX_Z.
     *
     * @param zoomLevel - The new new zoom level the verify
     * @return - whether or not the zoom level is within range.
     */
    public boolean isValidZoom(int zoomLevel) {
        int zoom = 31 - Integer.numberOfLeadingZeros(zoomLevel / TILE_SIZE);
        return (zoom >= MIN_Z && zoom < MAX_Z);
    }

    /**
     * Retrieves a tile image from the cache. If no tile is available,
     * a standard dummy tile is returned.
     *
     * @param target - the Images corresponding TileTag to index in the cache
     * @return - The cached tile image or a dummy tile.
     */
    Image pollImage(TileTag target) {
        Image tile = cache.get(target);
        if (tile != null) {
            return tile;
        }

        return dummyTile;
    }

    private void launchTileLoader(int width, int height) {

        if (tileLoader != null) {
            tileLoader.interrupt();
        }

        tileLoader = new TileLoader(centerTag, width, height);
        tileLoader.start();
    }

    /**
     * Remove the CLEAN_NUM furthest tiles from the cache to make space
     * for new incoming tiles.
     */
    private void cleanTiles() {
        TileTag[] loaded = cache.keySet().toArray(new TileTag[]{});
        Arrays.sort(loaded, new TileDistCmp(centerTag, TileDistCmp.FURTHEST));
        for (int i = 0; i < CLEAN_NUM; i++) {
            cache.remove(loaded[i]);
        }
    }

    /**
     * Adds the TileTag and corresponding image to the cache
     *
     * @param tag
     * @param tile
     */
    private synchronized void addTile(TileTag tag, Image tile) {
        if (cache.size() >= CACHE_SIZE) {
            cleanTiles();
        }
        cache.put(tag, tile);
    }

    /**
     * Add listener to be repainted if the viewed map ever changes
     *
     * @param c - The target component to be repaint on triggering.
     */
    public void addRepaintListener(Component c) {
        repaintListeners.add(c);
    }

    /**
     * Remove listener to be repainted if the viewed map ever changes
     *
     * @param c - the target component to be removed from the listener list
     */
    public void removeRepaintListener(Component c) {
        repaintListeners.remove(c);
    }

    /**
     * Notify listeners that the current map view may have changed
     */
    private void contentChanged() {
        for (Component c : repaintListeners) {
            c.repaint();
        }
    }

    /**
     * Method for fetching a given TileTag and putting it in the cache
     */
    private void loadTile(TileTag target) {
        try {
            Image img = ImageIO.read(target.getURL(rootURL));
            addTile(target, img);

        }
        catch (Exception e) {
            System.err.println("failed to load " + target);
            e.printStackTrace();
        }
        finally {
            //don't repaint for prefetched tiles
            if (target.z == centerTag.z) {
                contentChanged();
            }
        }
    }

    /**
     * small set of tests for TileDistCmp and TileTag
     */
    private void testTileDist() {
        java.util.List<TileTag> test = new ArrayList<>();

        TileDistCmp cmp = new TileDistCmp(new TileTag(2, 2, 5), TileDistCmp.FURTHEST);

        for (int z = 0; z < 12; z++) {
            System.out.println(z + " dist: " + cmp.compare(
                    new TileTag((1 << 6), (1 << 6), 6),
                    new TileTag((1 << z) + 2, (1 << z) + 2, z)));
        }

        test.add(new TileTag(0, 0, 1));
        test.add(new TileTag(0, 1, 1));
        test.add(new TileTag(1, 0, 1));
        test.add(new TileTag(1, 1, 1));

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                test.add(new TileTag(i, j, 2));
                test.add(new TileTag((2 * i), (2 * j), 3));
                test.add(new TileTag((2 * i), (2 * j) + 1, 3));
                test.add(new TileTag((2 * i) + 1, (2 * j), 3));
                test.add(new TileTag((2 * i) + 1, (2 * j) + 1, 3));
            }
        }

        test.sort(new TileDistCmp(new TileTag(1, 1, 2), TileDistCmp.FURTHEST));

        for (TileTag t : test) {
            System.out.println(t);
        }
    }

    /**
     * Compares two TileTags based on which one is closer to the given tag.
     * Z axis is weighted using Z_PRIORITY.
     * This should allow components to select the tags most/least important
     * to the viewer based on their current viewport into the map
     */
    static class TileDistCmp implements Comparator<TileTag> {
        static final int FURTHEST = -1;
        static final int CLOSEST = 1;
        //final TileTag ref;
        final double refx;
        final double refy;
        final int refz;
        final int dir;

        TileDistCmp(TileTag reference, int direction) {
            dir = direction;
            refz = reference.z;
            refx = reference.x / (double) (1 << reference.z);
            refy = reference.y / (double) (1 << reference.z);
        }

        public int compare(TileTag a, TileTag b) {
            double ax = a.x / (double) (1 << a.z);
            double ay = a.y / (double) (1 << a.z);
            double bx = b.x / (double) (1 << b.z);
            double by = b.y / (double) (1 << b.z);

            double ardist = Math.abs(refx - ax) + Math.abs(refy - ay);
            double brdist = Math.abs(refx - bx) + Math.abs(refy - by);

            double xydiff = (ardist - brdist) * (double) (1 << refz);
            double zdiff = Math.abs(refz - a.z) - Math.abs(refz - b.z);

            return dir * (int) Math.signum(xydiff + Z_PRIORITY * zdiff);
        }
    }

    /**
     * TileTag contains refereces to x,y,z coordinates, defining equals
     * and hash code appropriately, and generating url's for standard
     * web-mercator protocols.
     */
    static class TileTag {
        final int x, y, z;

        TileTag(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = result * (MAX_Y) + y;
            result = result * (MAX_Z) + z;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            TileTag other = (TileTag) obj;
            return x == other.x && y == other.y && z == other.z;
        }

        //check if a tile is within the coordinate bounds
        boolean valid() {
            return z >= MIN_Z && z <= MAX_Z &&
                    x >= 0 && x < (1 << z) &&
                    y >= 0 && y < (1 << z);
        }

        URL getURL(String rootURL) {
            String url = rootURL.replaceAll("\\{[xX]\\}", "" + x)
                    .replaceAll("\\{[yY]\\}", "" + y)
                    .replaceAll("\\{[zZ]\\}", "" + z);
            URL res = null;

            try {
                res = new URL(url);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return res;
        }

        @Override
        public String toString() {
            return "Tile @ " + "x: " +
                    x +
                    " y: " +
                    y +
                    " z: " +
                    z;
        }
    }

    /**
     * Class: TileLoader
     * Tile loader thread .This will load all the tiles around ref given width
     * and height, as well as all the tiles one level below that, in the order
     * specified by the TileDistCmp comparator
     */
    private class TileLoader extends Thread {

        final TileTag ref;
        final int width;
        final int height;

        boolean stop = false;
        int numToLoad;

        AtomicInteger loadIndex = new AtomicInteger(0);
        AtomicInteger finishedHelpers = new AtomicInteger(0);
        java.util.List<TileTag> toLoad = new ArrayList<>(CACHE_SIZE);

        /**
         * Class Constructor
         *
         * @param ref    - the map center reference point.
         * @param width  - Width in tiles of the map
         * @param height - Height in tiles of the map
         */
        TileLoader(TileTag ref, int width, int height) {
            this.ref = ref;
            this.width = width;
            this.height = height;
        }

        public void run() {
            try {
                //enqueue all the tiles on this zoom level by view+margin
                int hw = ((width + 1) / 2) + CUR_ZOOM_MARGIN;
                int hh = ((height + 1) / 2) + CUR_ZOOM_MARGIN;

                for (int row = -hw; row <= hw; row++) {
                    for (int col = -hh; col <= hh; col++) {
                        enqueue(new TileTag(ref.x + row, ref.y + col, ref.z));
                    }
                }

                if (interrupted()) {
                    return;
                }

                //enqueue all the tiles on the next zoom level by view+margin
                hw = ((width + 1) / 2) + NEXT_ZOOM_MARGIN;
                hh = ((height + 1) / 2) + NEXT_ZOOM_MARGIN;

                for (int row = -hw; row <= hw; row++) {
                    for (int col = -hh; col <= hh; col++) {
                        enqueueBeneath(new TileTag(ref.x + row, ref.y + col, ref.z));
                    }
                }

                if (interrupted()) {
                    return;
                }

                //sort queue by distance from current view
                toLoad.sort(new TileDistCmp(ref, TileDistCmp.CLOSEST));

                if (interrupted()) {
                    return;
                }

                //Set up and wait for the loading threads
                numToLoad = Math.min(toLoad.size(), CACHE_SIZE);
                for (int i = 0; i < CC_REQUEST; i++) {
                    (new Helper()).start();
                }

                while (finishedHelpers.get() != CC_REQUEST) {
                    try {
                        Thread.sleep(50);
                    }
                    catch (Exception e) {
                        stop = true;
                    }
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        //enqueue the 4 tiles on the next zoom level, beneath the argument
        void enqueueBeneath(TileTag tag) {
            enqueue(new TileTag((tag.x * 2), (tag.y * 2), tag.z + 1));
            enqueue(new TileTag((tag.x * 2), (tag.y * 2) + 1, tag.z + 1));
            enqueue(new TileTag((tag.x * 2) + 1, (tag.y * 2), tag.z + 1));
            enqueue(new TileTag((tag.x * 2) + 1, (tag.y * 2) + 1, tag.z + 1));
        }

        void enqueue(TileTag tag) {
            if (tag.valid() && !cache.containsKey(tag)) {
                toLoad.add(tag);
            }
        }

        private class Helper extends Thread {

            @Override
            public void run() {

                while (true) {
                    int index = loadIndex.getAndIncrement();

                    if (index >= numToLoad || TileLoader.this.stop) {
                        finishedHelpers.incrementAndGet();
                        break;
                    }

                    TileTag next = toLoad.get(index);
                    loadTile(next);
                }
            }
        }
    }
}

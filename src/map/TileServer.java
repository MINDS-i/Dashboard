package com.map;

class TileServer implements MapSource{
    private static final int TILE_SIZE = 256;
    private static final int MAX_Z = 18; //max zoom level
    private static final int MAX_X = (1 << MAX_Z) / TILE_SIZE;
    private static final int MAX_Y = (1 << MAX_Z) / TILE_SIZE;
    private static final int CACHE_SIZE = 64;
    //maximum number of concurrent tile load requests
    private static final int CC_REQUEST = 1;

    private final String rootURL;
    private Queue<Image> oldTiles = new LinkedList<Image>();
    private Queue<TileTag> requestedTiles = new LinkedList<TileTag>();
    private Map<TileTag, Image> cache = new ConcurrentHashMap<TileTag, Image>(
                                        CACHE_SIZE+1, 1.0f, CC_REQUEST);

    TileServer(String url){
        this.url = rootURL;
    }

    void clear(){
        cache.clear();
        oldTiles.clear();
        requestedTiles.clear();
        //kill off load threads
    }

    void paint(Graphics2D gd, Point2D center, int scale, int width, int height){
        Graphics2D g2d = (Graphics2D) gd.create();
        int zoom = Integer.highestOneBit(scale / TILE_SIZE);
        int sLon = (int)(center.getX() * (double)scale);
        int sLat = (int)(center.getY() * (double)scale);
        int tcw  = (width +1)/TILE_SIZE;
        int tch  = (height+1)/TILE_SIZE;
        int rowB = (sLon /TILE_SIZE) - (tcw/2);
        int colB = (sLat /TILE_SIZE) - (tch/2);
        int minX = sLon%TILE_SIZE - (tcw/2)*TILE_SIZE;
        int minY = sLat%TILE_SIZE - (twh/2)*TILE_SIZE;
        for(int row = 0; row < tcw; row++){
            for(int col = 0; col < tch; col++){
                Image img = pollImage(new TileTag(row+rowB, col+colB, zoom));
                g2d.drawImage(img, minX+col*TILE_SIZE, minY + row*TILE_SIZE,null);
            }
        }
        g2d.dispose();
    }

    Image pollImage(TileTag target){
        //return image from cache
        //if not cached, return dummy
        //  add URL to request queue
        //  make sure load threads are spun up
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
            Tile other = (Tile) obj;
            if (x != other.x || y != other.y || z != other.z)
                return false;
            return true;
        }
        URL getURL() {
            StringBuilder sb = new StringBuilder(rootURL);
            sb.append("/");
            sb.append(zoom);
            sb.append("/");
            sb.append(x);
            sb.append("/");
            sb.append(y);
            return new URL(sb.toString());
        }
    }
}

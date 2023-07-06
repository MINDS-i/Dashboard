package com.map;

import static com.map.WaypointList.*;

import com.map.command.*;
import com.Context;
import com.serial.Serial;
import com.telemetry.TelemetryListener;
import com.layer.Layer;
import com.util.SwathProperties;
import com.map.WaypointType;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.*;

public class RoverPath implements Layer {
	
    private static final Color ACTIVE_LINE_FILL = new Color(1.f, 1.f, 0.f, 1f);
    private static final Color PATH_LINE_FILL   = new Color(0f, 0f, 0f, 1f);
    private static final Color LINE_BORDER      = new Color(0.5f, 0.5f, 0.5f, 0.0f);
    private static final int   LINE_WIDTH       = 10;

    private Context context;
    private CoordinateTransform mapTransform;
    private WaypointList waypoints;
    private Dot rover = new Dot();
    private Component painter;
    private MapPanel map;
    private boolean waypointsDisabled = false;
    
    
    //Movement action vars (Press, Drag, Release)
    private Dot draggedDot = null;
    private int draggedDotIdx = Integer.MAX_VALUE;
    private int downDot = Integer.MAX_VALUE;
    private WaypointCommand moveCommand;
    private OpMode currOpMode;
    
    //Logging support
    protected final Logger serialLog = Logger.getLogger("d.serial");
    
    /**
     * Pre-defined state enums used for deciding the correct user action to
     * perform based on UI context.
     */
    public enum OpMode {
    	STANDARD	(0),
    	SET_HOME	(1),
    	PLACE_SWATH	(2);
    	
    	private final int mode;
    	
    	OpMode(int mode) {
    		this.mode = mode;
    	}
    	
    	public int getValue() {
    		return this.mode;
    	}
    };
    
    RoverPath(Context c, CoordinateTransform cT, WaypointList waypoints,
    		Component painter, MapPanel map) {
        context = c;
        mapTransform = cT;
        this.waypoints = waypoints;
        this.painter = painter;
        this.map = map;
        
        waypointsDisabled = !Boolean.valueOf(
        		context.getResource("waypoints_enabled", "true"));
        
        currOpMode = OpMode.STANDARD;
    }

    @Override
    public int getZ() {
        return 1;
    }

    /**
     * Enable creating and modifying the waypoint list. Defaults to true.
     */
    public void setWaypointsEnabled(boolean value) {
        waypointsDisabled = !value;
    }

    @Override
    public void paint(Graphics g) {
        paintDots(g);
    }

    @Override
    public boolean onClick(MouseEvent e) {
    	WaypointCommand command = null;
    	boolean result = false;
    	
        if(waypointsDisabled) {
        	return false;
        } 
        
        Point2D pixel = toP2D(e.getPoint());
        int underneath = isOverDot(pixel, context.theme.waypointImage);

        // Left click that is NOT over an existing point
        if((e.getButton() == MouseEvent.BUTTON1)
        && (underneath == Integer.MAX_VALUE)) {
        	
            Point2D point = mapTransform.mapPosition(pixel);
            int line = isOverLine(e.getPoint());
            
            // Click is NOT over an existing line
            if (line == Integer.MAX_VALUE) {
            	
            	switch(currOpMode) {
            		case SET_HOME:
            			updateHomeLocation(point);
            			map.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            			currOpMode = OpMode.STANDARD;
            			break;
            		case STANDARD:
            			command = new WaypointCommandAdd(
                    			waypoints, 
                    			new Dot(point, WaypointType.STANDARD), 
                    			waypoints.size());
            			
                    	//Manually adjust for size vs index offset
                        waypoints.setSelected(waypoints.size() - 1);
            			break;
            			
            		case PLACE_SWATH:
            			
            			//If a swath hasn't already been placed, set the command
            			if(!SwathProperties.getInstance().getIsSwathPlaced()) {
                			command = new WaypointCommandAddSwath(
                					waypoints, 
                					new Dot(point, WaypointType.SWATH), 
                					waypoints.size(),
                					context.dash.farmingPanel.getType(),
                					context.dash.farmingPanel.getInversion());
                			
                			waypoints.setSelected(waypoints.size() - 1);
            			}
            			else { //If a swath has been placed, then report error
            				serialLog.warning("RoverPath - Error: "
            						+ "Only one swath may be placed at a time.");
            			}
            			
            			map.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            			currOpMode = OpMode.STANDARD;
            			break;
            			
            		default:
            	}
            } 
            // Click is over an existing line
            else {
            	
            	switch(currOpMode) {
        			case STANDARD:
        				command = new WaypointCommandAdd(
        						waypoints, 
        						new Dot(point, WaypointType.STANDARD), 
        						line);
        			break;
        			
            		case SET_HOME:
            			updateHomeLocation(point);
            			map.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            			currOpMode = OpMode.STANDARD;
            			break;
            		
            		case PLACE_SWATH:
            			serialLog.warning("RoverPath - Error: "
            					+ "Can't insert swath inside an existing path");
            			break;
        			default:
            	}
            }
        } 
        // Right click on top of a point
        else if ((e.getButton() == MouseEvent.BUTTON3) 
        	 &&  (underneath != Integer.MAX_VALUE)) {
        	
        	switch(currOpMode) {
				case SET_HOME:
					serialLog.warning("RoverPath - "
							+ "Can't set home to existing point position.");
        			map.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        			currOpMode = OpMode.STANDARD;
					break;
				case STANDARD:
					
					//If it's an attempt to delete the fence origin, return
					if(waypoints.getSelected() == 0) {
						return false;
					}
					
					command = new WaypointCommandRemove(waypoints, underneath);
					break;
					
				case PLACE_SWATH:
					//Do Nothing
					break;
				default:
        	}
        }
        else {
        	return false;
        }
        
        //Verify the command exists before processing it.
        if(command != null) {
            return CommandManager.getInstance().process(command);
        }
 
        return true;
    }

    @Override
    public boolean onPress(MouseEvent e) {
    	//If we aren't in standard mode, don't allow waypoint movement.
    	if(currOpMode != OpMode.STANDARD) {
    		return false;
    	}
    	
        Point pixel = e.getPoint();
        downDot = isOverDot(pixel, context.theme.waypointImage);
        
        if(downDot != Integer.MAX_VALUE) {
        	waypoints.setSelected(downDot);
            
            if(downDot < 0 || waypointsDisabled) {
                // Disable dragging for non-waypoint line dots
                downDot = Integer.MAX_VALUE;
            } 
            else {
                draggedDot = waypoints.get(downDot).dot();
                //Initialize moveCommand but wait for end location to process.
                moveCommand = new WaypointCommandMove(waypoints, downDot);
            }
            
            return true;
        }
        
        return false;
    }

    @Override
    public void onDrag(MouseEvent e) {
        Point2D pixel = toP2D(e.getPoint());
        if (downDot != Integer.MAX_VALUE) {
            Point2D finalLoc = mapTransform.mapPosition(pixel);
            draggedDotIdx = downDot;
            draggedDot.setLocation(finalLoc);
            painter.repaint();
        }
    }

    @Override
    public void onRelease(MouseEvent e) {
        Point pixel = e.getPoint();
        if(draggedDotIdx != Integer.MAX_VALUE) {
        	draggedDotIdx = Integer.MAX_VALUE;
        	
        	//Dot should now be at end location, so we can grab it and execute.
        	moveCommand.finalize(new Dot(draggedDot));
        	CommandManager.getInstance().process(moveCommand);
        }
    }

    public void paintLine(Graphics g, Point2D pointA, Point2D pointB, Color fill) {
        Graphics2D g2d = (Graphics2D) g.create();
        RenderingHints hints = g2d.getRenderingHints();
        
        hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(hints);
        
        try {
            final double dx = pointA.getX() - pointB.getX();
            final double dy = pointA.getY() - pointB.getY();
            final int length = (int) Math.sqrt(dx * dx + dy * dy);

            g2d.translate((int)pointB.getX(), (int)pointB.getY());
            g2d.rotate(Math.atan2(dy, dx));
            g2d.setPaint(new GradientPaint(0, 0, fill,
                                           0,
                                           -(LINE_WIDTH/2),
                                           LINE_BORDER,
                                           true));
            g2d.fillRect(0, -(LINE_WIDTH / 2), length, LINE_WIDTH);
        } 
        finally {
            g2d.dispose();
        }
    }

    private void paintDots(Graphics g) {
        drawLines(g);
        drawRoverLine(g);
        drawPoints(g);
    }

    /**
     * Returns the screen coordinate (x, y) pixel position of
     * the Lng/Lat point found at the specified index in the
     * waypoint list. 
     * @param index - The waypoint list index
     * @return - Point2D containing the pixel coordinates on the map.
     */
    private Point2D drawnLocation(int index){
        Dot d = null;
        
        if(index == draggedDotIdx) {
            d = draggedDot;
        } 
        else {
            d = waypoints.get(index).dot();
        }
        
        return mapTransform.screenPosition(d.getLocation());
    }

    private Point2D roverLocation(){
        return mapTransform.screenPosition(waypoints.getRover().getLocation());
    }

    private void drawLines(Graphics g) {
    	
        if(waypoints.size() < 2) {
        	return;
        } 

        Point n = null;
        Point l = null;
        for(int i = 0; i < waypoints.size(); i++) {
            n = toPoint(drawnLocation(i));
            
            if(l != null) {
            	paintLine(g, n, l, PATH_LINE_FILL);
            } 
            
            l = n;
        }

        if(waypoints.getIsLooped()) {
            n = toPoint(drawnLocation(0));
            paintLine(g, n, l, PATH_LINE_FILL);
        }
    }

    private void drawPoints(Graphics g) {
        Point tmp;

        for(int i = waypoints.getExtendedIndexStart(); i < waypoints.size(); i++) {
            tmp = toPoint(drawnLocation(i));
            ExtendedWaypoint w = waypoints.get(i);
            BufferedImage img = context.theme.waypointImage;
            
            switch(w.type()) {
                case HOME: 
                	img = context.theme.homeIcon; 
                	break;
                case ROVER:
                	img = context.theme.roverImage;
                	break;
                case SELECTED:
                	img = context.theme.waypointSelected; 
                	break;
            }
            
            drawImg(g, img, tmp);
        }
    }

    private void drawRoverLine(Graphics g) {
        if(waypoints.getTarget() >= waypoints.size()) {
            return;
        }
        
        Point n = toPoint(roverLocation());
        Point l = toPoint(drawnLocation(waypoints.getTarget()));
        paintLine(g, n, l, ACTIVE_LINE_FILL);
    }

    private void drawImg(Graphics g, BufferedImage img, Point2D loc) {
        g.translate(-img.getWidth() / 2, -img.getHeight() / 2);
        g.drawImage( img , (int)loc.getX(), (int)loc.getY(), null);
        g.translate( img.getWidth() / 2, img.getHeight() / 2);
    }

    
    //Mouse Action Handler
    public int isOverDot(Point2D click, BufferedImage image) {
        for(int i = waypoints.getExtendedIndexStart(); i < waypoints.size(); i++) {
            Dot d = waypoints.get(i).dot();
            Point2D loc = mapTransform.screenPosition(d.getLocation());
            if(Math.abs(click.getX()-loc.getX()-1) > image.getWidth() / 2.0) continue;
            if(Math.abs(click.getY()-loc.getY()-1) > image.getHeight() / 2.0) continue;
            return i;
        }
        
        return Integer.MAX_VALUE;
    }

    //Mouse Action Handler
    public int isOverLine(Point p) {
        if(waypoints.size() <= 0) {
        	return Integer.MAX_VALUE;
        }

        int index;
        Point prevPoint;

        if(waypoints.getTarget() == 0) {
            index = 0;
            prevPoint = toPoint(roverLocation());
        } 
        else {
            index = 1;
            prevPoint = toPoint(drawnLocation(0));
        }

        for( ; index < waypoints.size(); index++) {
            Point thisPoint = toPoint(drawnLocation(index));
            if( distFromLine(prevPoint, thisPoint, p) < (LINE_WIDTH/2) ) {
                return index;
            }
            
            prevPoint = thisPoint;
        }

        return Integer.MAX_VALUE;
    }

    public int distFromLine(Point a, Point b, Point idp) {
        float abSlope = (a.y-b.y)/(a.x-b.x+.0000001f);
        float abYCept = a.y - abSlope*a.x;
        float perpSlope = (a.x-b.x)/(b.y-a.y+.0000001f);
        float perpYCept = idp.y - perpSlope*idp.x;
        float interceptX = (abYCept - perpYCept) / (perpSlope-abSlope+.0000001f);
        float interceptY = perpSlope*interceptX + perpYCept;
        
        if( a.x > b.x && a.x > interceptX && interceptX > b.x) {
            return (int) Math.floor(
                       Math.sqrt( (interceptX-idp.x) * (interceptX-idp.x) +
                                  (interceptY-idp.y) * (interceptY-idp.y) ));
        } 
        else if ( b.x > a.x && b.x > interceptX && interceptX > a.x) {
            return (int) Math.floor(
                       Math.sqrt( (interceptX-idp.x) * (interceptX-idp.x) +
                                  (interceptY-idp.y) * (interceptY-idp.y) ));
        } 
        else {
            return Integer.MAX_VALUE;
        }
    }

    public Point2D toP2D(Point p) {
        return new Point2D.Double(p.x, p.y);
    }

    public Point toPoint(Point2D p) {
        return new Point((int) p.getX(), (int) p.getY());
    }
    
    /**
     * Returns the current operational mode for determining
     * how to handle user input.
     * @return - enum describing the current op mode.
     */
    public OpMode getOpMode() {
    	return currOpMode;
    }
    
    /**
     * Sets the current operational mode used for determining
     * how to handle user input.
     * @param mode - The new mode to set.
     */
    public void setOpMode(OpMode mode) {
    	currOpMode = mode;
    }
    
    /**
     * Changes the current home location and updates its
     * persistent storage value to be consistent upon successive
     * restarts.
     * @param location - The updated home location.
     */
    public void updateHomeLocation(Point2D point) {
    	Dot home = waypoints.getHome();
    	home.setLocation(point);
    	waypoints.setHome(new Dot(point, WaypointType.HOME));
    	context.setHomeProp(String.valueOf(home.getLatitude()), 
    			String.valueOf(home.getLongitude()));
    	serialLog.warning("SET HOME - Home point set.");
    }
    
}

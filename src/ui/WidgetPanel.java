package com.ui;

import java.util.*;
import java.util.logging.Logger;

import com.Context;
import com.ui.ninePatch.NinePatchPanel;
import com.ui.widgets.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

/** 
 * @author Chris Park @ Infinetix Corp.
 * Date: 12-8-20
 * Description: Main panel container for holding a group of 
 * dashboard UIWidgets.
 */
public class WidgetPanel extends NinePatchPanel {
	//Constants
    protected static final int 		BORDER_TOP 	= 18;
    protected static final int 		BORDER_BOT 	= 18;
    protected static final int 		BORDER_LFT 	= 13;
    protected static final int 		BORDER_RHT 	= 13;
    protected static final Color 	BG_COLOR	= Color.decode("0xEEEEEE");
    
	protected Context context;
	protected LinkedList<UIWidget> widgets;
	
    //Logging support
    protected final Logger serialLog = Logger.getLogger("d.serial");
    
	/**
	 * Class Constructor
	 * The layoutType provided here should be one of the static ints defined
	 * for the BoxLayout class, such as LINE_AXIS/PAGE_AXIS. (See Java
	 * documentation for the BoxLayout class for details)
	 * @param ctx - The application context
	 * @param layoutType - The BoxLayout type to be used for orientation
	 */
	public WidgetPanel(Context ctx, int layoutType) {
		super(ctx.theme.panelPatch);
		context = ctx;
		widgets = new LinkedList<UIWidget>();
		
		setOpaque(false);
		LayoutManager layout = new BoxLayout(this, layoutType);
		setLayout(layout);
		setBorder(BorderFactory.createEmptyBorder(BORDER_TOP, BORDER_LFT,
        		BORDER_BOT, BORDER_RHT));
		
        //Do nothing to prevent clicks from falling through to map panel
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {}
        });
	}
	
	/**
	 * Adds a widget to the WidgetPanel and its tracked widgets list.
	 * @param widget - The UIWidget to be added to this panel container.
	 * @return - Whether or not the operation was successful.
	 */
	public boolean addWidget(UIWidget widget) {
		add(widget);
		return widgets.add(widget);
	}
	
	/**
	 * Removes a widget from the WidgetPanel and its tracked widgets list.
	 * @param widget - The widget to be removed.
	 * @return - Whether or not the operation was successful.
	 */
	public boolean removeWidget(UIWidget widget) {
		remove(widget);
		widgets.remove(widget);
		return true;
	}
	
}

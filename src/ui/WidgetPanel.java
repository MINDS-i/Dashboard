package com.ui;


import java.util.*;

import com.Context;
import com.ui.ninePatch.NinePatchPanel;

import java.awt.*;
import javax.swing.*;

/** 
 * @author Chris Park @ Infinetix Corp.
 * Date: 12-8-20
 * Description: Main panel container for the right side of the dashboard.
 * Holds a collection of UIWidgets. 
 * 
 * Note: 12-8-20 - Future implementation improvements will aim
 * to provide additional functionality for the manipulation of contained widgets.
 */
public class WidgetPanel extends NinePatchPanel {
	//Constants
	protected static final int BDR_SIZE_TB = 20;
    protected static final int BDR_SIZE_LR = 10;
	
	protected Context context;
	protected LinkedList<UIWidget> widgets;
    
	/**
	 * Class Constructor
	 * @param ctx - The application context
	 */
	public WidgetPanel(Context ctx) {
		super(ctx.theme.panelPatch);
		context = ctx;
		widgets = new LinkedList<UIWidget>();
		
		setOpaque(false);
		LayoutManager layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		setLayout(layout);
		setBorder(BorderFactory.createEmptyBorder(BDR_SIZE_TB, BDR_SIZE_LR,
        		BDR_SIZE_TB, BDR_SIZE_LR));
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

package com.ui.ninePatch;

import com.Context;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * A transparent border panel
 * Child nodes will have to call repaint on this node instead of repainting
 *   themselves directly or else they will draw over the border
 * Child nodes will be panned and clipped correctly so they automatically
 *   draw within the center region
 * At some point patches should carry some metadata so this panel isn't
 *   limited to use with a single specific patch.
 */

public class TransparentPanel extends JPanel {

	//Margin Consts
	private final int LEFT_MARGIN   = 8;
    private final int RIGHT_MARGIN  = 8;
    private final int TOP_MARGIN    = 8;
    private final int BOTTOM_MARGIN = 8;
    
    //Vars
    private Rectangle drawRect;
    private NinePatch np;
    private int size;

    public TransparentPanel(Context ctx, int size){
        this.size = size;
        np = ctx.theme.horizonBorder;
        
        drawRect = new Rectangle(LEFT_MARGIN, TOP_MARGIN,
        		(size - RIGHT_MARGIN - LEFT_MARGIN), 
        		(size - BOTTOM_MARGIN - TOP_MARGIN));
        
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
        setBorder(new EmptyBorder(TOP_MARGIN, LEFT_MARGIN, BOTTOM_MARGIN, RIGHT_MARGIN));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        np.paintIn(g, getWidth(), getHeight());
    }
}

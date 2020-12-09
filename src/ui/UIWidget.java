package com.ui;

import com.Context;

import java.io.Reader;
import java.io.FileReader;

import javax.swing.*;
import javax.swing.border.*;

import javax.xml.stream.*;
import java.text.ParseException;

import java.awt.*;

/**
 * 
 * @author Chris Park @ Infinetix Corp.
 * Date: 11-20-2020
 * Descriptions: Base class from which a UI widget derives its
 * general functionality.
 */
public class UIWidget extends JPanel {
	//Constants
	protected static final float FONT_SIZE 	= 14.0f;
	
	protected Context context;
	protected Border insets;
	
	protected JPanel titlePanel;
	protected JLabel titleLabel;
	
	
	/**
	 * Class constructor
	 * @param ctx - The application context
	 * @param title - the title string for the widget
	 */
	public UIWidget(Context ctx, String title) {
		context = ctx;
		insets =  new EmptyBorder(0, 0, 0, 0);
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setOpaque(false);
		
		Font font = context.theme.text.deriveFont(FONT_SIZE);
		Dimension labelSize= new Dimension(100, 25);
		
		titlePanel = new JPanel();
		titlePanel.setOpaque(true);
		titlePanel.setPreferredSize(labelSize);
		titlePanel.setBorder(BorderFactory.createLineBorder(Color.black));
		titlePanel.setBackground(Color.decode("0xFAAC2D"));
		
		titleLabel = new JLabel(title);
		titleLabel.setOpaque(false);
		titleLabel.setFont(font);
		
		titlePanel.add(titleLabel);
		add(titlePanel);
	}

	public void setInsets (int top, int left, int bottom, int right) {
		insets = new EmptyBorder(top, left, bottom, right);
		this.setBorder(insets);
	}	
	
	public void updateTitle(String title) {
		titleLabel.setText(title);
	}
}

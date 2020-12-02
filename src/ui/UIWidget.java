package com.ui;

import com.Context;

import java.io.Reader;
import java.io.FileReader;

import javax.swing.*;
import javax.swing.border.*;

import javax.xml.stream.*;
import java.text.ParseException;

/**
 * 
 * @author Chris Park @ Infinetix Corp.
 * Date: 11-20-2020
 * Descriptions: Base class from which a UI widget derives its
 * general functionality.
 */
public class UIWidget extends JPanel {
	protected Context context;
	protected Border insets;
	
	/**
	 * Standard class Constructor
	 */
	public UIWidget(Context ctx) {
		context = ctx;
		insets =  new EmptyBorder(0, 0, 0, 0);
	}

	public void setInsets (int top, int left, int bottom, int right) {
		insets = new EmptyBorder(top, left, bottom, right);
		this.setBorder(insets);
	}	
}

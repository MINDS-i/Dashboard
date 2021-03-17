package com.ui;

import javax.swing.*;

/**
 * @author Chris Park @ Infinetix.com
 * Date: 3-5-21
 * Description: A standard JSlider works in integer values only. This
 * custom JSlider class is intended for handling float values.
 */
public class FloatJSlider extends JSlider{
	private int scale;
	
	/**
	 * Default Constructor
	 */
	public FloatJSlider() {
		super(0, 100, 50);
		this.scale = 1;
	}
	
	/**
	 * Class constructor
	 * @param min 	- Minimum value for slider range
	 * @param max 	- Maximum value for slider range
	 * @param value - The initial value of the slider
	 * @param scale - The decimal scale of used for value conversion.
	 */
	public FloatJSlider(int min, int max, int value, int scale) {
		super(min, max, value);
		this.scale = scale;
	}
	
	/**
	 * Returns the float value that corresponds to the sliders current position.
	 * @return - float
	 */
	public float getScaledValue() {
		return ((float)super.getValue() / this.scale);
	}
	
	/**
	 * Sets the scale that determines decimal conversion when retrieving slider
	 * values.
	 * @return
	 */
	public int getScale() {
		return scale;
	}
	
	/**
	 * Sets the current decimal scale for this slider.
	 * @param value
	 */
	public void setScale(int value) {
		scale = value;
	}
	
	public void updateValue() {
		//Update the slider value from an external source here.
	}
}

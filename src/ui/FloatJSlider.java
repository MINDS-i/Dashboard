package com.ui;

import javax.swing.*;

/**
 * @author Chris Park @ Infinetix.com
 * Date: 3-5-21
 * Description: A standard JSlider works in integer values only. This
 * custom JSlider class is intended for handling float values.
 */
public class FloatJSlider extends JSlider{
	private final int scale;
	
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
}

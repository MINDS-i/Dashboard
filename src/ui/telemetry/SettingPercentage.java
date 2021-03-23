package com.ui.telemetry;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 3-22-21
 * Description: Data structure class for maintaining a percentage value related
 * to a telemetry setting. This is used to render a JSlider within a table to
 * control Telemetry settings.
 */
public class SettingPercentage {
	private int percentage;
	
	/**
	 * Class Constructor. Defaults value to 0 percent
	 */
	public SettingPercentage() {
		setPercentage(0);
	}
	
	/**
	 * Class Constructor. Sets percentage to specified
	 * value.
	 * @param value - the value to set the percentage to.
	 */
	public SettingPercentage(int value) {
		setPercentage(value);
	}
	
	/**
	 * Sets the percentage value.
	 * @param value - The new percentage value
	 */
	public void setPercentage(int value) {
		percentage = value;
	}
	
	/**
	 * Sets the percentage value from an existing SettingPercentage object.
	 * @param value - The new percentage value
	 */
	public void setPercentage(Object value) {
		if(value instanceof SettingPercentage) {
			setPercentage(((SettingPercentage) value).getPercentage());
		}
		
	}
	
	/**
	 * Returns the current percentage value
	 * @return - int
	 */
	public int getPercentage() {
		return percentage;
	}
	
	/**
	 * Returns the string representation of the current
	 * percentage value.
	 * @return - String
	 */
	public String toString() {
		return String.valueOf(percentage);
	}
}

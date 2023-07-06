package com.util;


public class SwathProperties {
	private static SwathProperties swathPropertiesInstance = null;
	
	//Swath Pattern dimension properties
	public static final double SWATH_LENGTH_FT = 200;
	public static final double SWATH_WIDTH_FT = 32;
	
	//Swath placement state
	private boolean isSwathPlaced;
	private boolean previousSwathPlacedState;
	
	/**
	 * Const ructor (Private, accedssed by getInstance)
	 */
	private SwathProperties() {
		isSwathPlaced = false;
		previousSwathPlacedState = false;
	}
	
	/**
	 * Returns a singleton instance of this class to be used system wide.
	 * @return
	 */
	public static SwathProperties getInstance() {
		if(swathPropertiesInstance == null) {
			swathPropertiesInstance = new SwathProperties();
		}
		
		return swathPropertiesInstance;
	}
	
	/**
	 * Gets the current placement state of the swath pattern.
	 * @return - boolean
	 */
	public boolean getIsSwathPlaced() {
		return isSwathPlaced;
	}
	
	/**
	 * Sets the current placement state of the swath pattern
	 * @param placed - boolean
	 */
	public void setIsSwathPlaced(boolean placed) {
		isSwathPlaced = placed;
	}
	
	/**
	 * Gets the previous state of swath placement
	 * @return - boolean
	 */
	public boolean getPreviousSwathPlacedState() {
		return previousSwathPlacedState;
	}
	
	/**
	 * Sets the previous state of swath placement
	 * @param placed - boolean
	 */
	public void setPreviousSwathPlacedState(boolean placed) {
		previousSwathPlacedState = placed;
	}
	
}

package com.ui.telemetry;

import javax.swing.*;

public class TableSlider extends JSlider {

	
	public void updateValue(int value) {
		setValue(value);
		invalidate();
	}
}

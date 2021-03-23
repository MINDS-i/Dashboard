package com.ui.telemetry;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

import java.awt.Component;

public class SliderRenderer extends JSlider implements TableCellRenderer {
	public SliderRenderer() {
		super(SwingConstants.HORIZONTAL);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if(value == null) {
			return this;
		}
		
		if(value instanceof SettingPercentage) {
			setValue(((SettingPercentage) value).getPercentage());
		}
		else {
			setValue(0);
		}
		
		return this;
	}
	
}

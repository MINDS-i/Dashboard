package com.ui.telemetry;

import com.Context;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

import java.awt.Component;

/** 
 * @author Chris Park @ Infinetix Corp.
 * Date: 3-22-21
 * Description: A custom renderer that display JSliders in table cells
 * for SettingPercentage (integer) values.
 */
public class SliderRenderer extends JSlider implements TableCellRenderer {
	Context context;
	
	public SliderRenderer(Context context) {
		super(SwingConstants.HORIZONTAL);
		this.context = context;
	}
	
	/**
	 * Handles the standard functionality of the renderer. Determining how
	 * values and positions are visually represented.
	 * @param table - The table this renderer is attached to
	 * @param value - The cell value manipulated by the renderer.
	 * @param isSelected - Whether or not the active cell is selected
	 * @param hasFocus - Whether or not the selected cell has focus.
	 * @param row - The row location of the cell in the table
	 * @param column - The column location of the cell in the table.
	 * @return - Component
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if(value instanceof SettingPercentage) {
			setValue(((SettingPercentage) value).getPercentage());
		}
		else {
			setValue(0);
		}
		
		return this;
	}
}

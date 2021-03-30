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
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if(value instanceof SettingPercentage) {
//			System.err.println("SliderRenderer - In renderer component");
			setValue(((SettingPercentage) value).getPercentage());
		}
		else {
			setValue(0);
		}
		
		return this;
	}
	
}

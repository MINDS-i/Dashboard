package com.ui.telemetry;

import com.remote.Setting;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TelemetryDataFieldPanelRenderer 
	extends TelemetryDataFieldPanel implements TableCellRenderer {
	
	public TelemetryDataFieldPanelRenderer(Setting setting) {
		super(setting);
		setName("Table.cellRenderer");
	}
	
	@Override
	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int col) {
		if(value instanceof Setting) {
			updateValue((Setting) value);
		}
		
		return this;
	}
}

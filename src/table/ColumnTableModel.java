package com.table;

import javax.swing.table.AbstractTableModel;
import java.util.Iterator;
import java.util.List;

public class ColumnTableModel extends AbstractTableModel {
    private final List<TelemetryColumn<?>> columns;

    public ColumnTableModel(List<TelemetryColumn<?>> cList) {
        columns = cList;
    }

    public int getColumnCount() {
        return columns.size();
    }

    public int getRowCount() {
        int rowCount = Integer.MAX_VALUE;

        for (TelemetryColumn<?> column : columns) {
            TelemetryColumn col = (TelemetryColumn) column;
            rowCount = Math.min(col.getRowCount(), rowCount);
        }
        return rowCount;
    }

    public String getColumnName(int col) {
        return columns.get(col).getName();
    }

    public Object getValueAt(int row, int col) {
        return columns.get(col).getValueAt(row);
    }

    public Class getColumnClass(int col) {
        return columns.get(col).getDataClass();
    }

    public boolean isCellEditable(int row, int col) {
        return columns.get(col).isRowEditable(row);
    }

    public void setValueAt(Object value, int row, int col) {
        setValueAtHelper(columns.get(col), value, row);
        fireTableCellUpdated(row, col);
    }

    private <T> void setValueAtHelper(TelemetryColumn<T> tc, Object obj, int row) {
        T val = tc.getDataClass().cast(obj);
        tc.setValueAt(val, row);
    }
}

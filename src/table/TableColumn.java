package com.table;

public interface TableColumn {
    /** Returns the name of this Column */
    public String getName();
    /** Returns the value for the coll on `row` */
    public Object getValueAt(int row);
    /** Returns the number of rows in this column */
    public int getRowCount();
    /** Returns the most specific superclass of all cell values */
    public Class<?> getDataClass();
    /** Returns true if the cell on `row` is editable */
    public boolean isRowEditable(int row);
    /** Sets the value in the coll `row` to `value` */
    public void setValueAt(Object value, int row);
}

package com.table;

public interface TelemetryColumn<T> {
    /**
     * Returns the name of this Column
     */
    String getName();

    /**
     * Returns the value for the coll on `row`
     */
    T getValueAt(int row);

    /**
     * Returns the number of rows in this column
     */
    int getRowCount();

    /**
     * Returns the most specific superclass of all cell values
     */
    Class<T> getDataClass();

    /**
     * Returns true if the cell on `row` is editable
     */
    boolean isRowEditable(int row);

    /**
     * Sets the value in the coll `row` to `value`
     */
    void setValueAt(T value, int row);
}

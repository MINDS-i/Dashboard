package com.table;

import com.Context;
import com.remote.SettingList;
import com.ui.telemetry.SettingPercentage;
import com.ui.telemetry.SliderEditor;
import com.ui.telemetry.SliderRenderer;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author Chris Park @ Infinetix Corp
 * Date: 3-4-21
 * Description: Factory class used to encapsulate the creation of
 * tables used for displaying telemetry data and configurable
 * vehicle settings.
 */
public class TableFactory {

    //Constructor is private here to prevent object instantiation.
    private TableFactory() {
    }

    /**
     * Create a table based on the Enum TableType provided.
     *
     * @param type    - The TableType of the table to create
     * @param context - The application context
     * @return JTable - The generated table
     */
    public static JTable createTable(TableType type, Context context) {
        switch (type) {
            case Telemetry:
                return buildTelemetryTable(context);
            case Settings:
                return buildSettingsTable(context);
            case Sliders:
                return buildSettingsSliderTable(context);
            default:
                System.err.println("TableFactory Error - Unknown table type");
        }

        return null;
    }

    /**
     * Creates a vehicle settings table with column layout specified by
     * the applied table model and sizing settings.
     *
     * @param context - The application context
     * @return - A configured vehicle settings table
     */
    private static JTable buildSettingsTable(Context context) {
        JTable table;
        ColumnTableModel model;
        SettingList settingList = context.settingList;
        ArrayList<TelemetryColumn<?>> columns = new ArrayList<>();

        columns.add(new TelemetryColumn<String>() {
            public String getName() {
                return "Name";
            }

            public String getValueAt(int row) {
                if (row < settingList.size()) {
                    return settingList.get(row).getName();
                }
                return "#" + row;
            }

            public int getRowCount() {
                return settingList.size();
            }

            public Class<String> getDataClass() {
                return String.class;
            }

            public boolean isRowEditable(int row) {
                return false;
            }

            public void setValueAt(String val, int row) {
            }
        });

        columns.add(new TelemetryColumn<String>() {
            public String getName() {
                return "Setting";
            }

            public String getValueAt(int row) {
                float val = settingList.get(row).getVal();
                return " " + val;
            }

            public int getRowCount() {
                return settingList.size();
            }

            public Class<String> getDataClass() {
                return String.class;
            }

            public boolean isRowEditable(int row) {
                return true;
            }

            public void setValueAt(String val, int row) {

                float newVal = Float.parseFloat(val);

                if (settingList.get(row).outsideOfBounds(newVal)) {
                    JFrame mf = new JFrame("Warning");
                    JOptionPane.showMessageDialog(
                            mf, "Caution: new value is outside of allowable range. " +
                                    "The min or max of that range will be used.");

                    //Ensure we are only setting values at maximum or minimum
                    //if they have been exceeded.
                    newVal = ((newVal > settingList.get(row).getMax())
                            ? settingList.get(row).getMax()
                            : settingList.get(row).getMin());
                }
                settingList.pushSetting(row, newVal);
            }
        });

        model = new ColumnTableModel(columns);
        table = new JTable(model);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);

        return table;
    }

    /**
     * Creates a custom table that renders slider controls for use in
     * controlling vehicle telemetry settings values.
     *
     * @param context - The application context;
     * @return - A configured slider table.
     */
    private static JTable buildSettingsSliderTable(Context context) {

        JTable table;
        SettingSliderModel model;


        model = new SettingSliderModel(context.settingList.size());

        table = new JTable(model);
        table.setDefaultRenderer(SettingPercentage.class, new SliderRenderer(context));
        table.setDefaultEditor(SettingPercentage.class, new SliderEditor(context, table));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);

        return table;
    }

    /**
     * Creates a telemtry data table with column layout specified by
     * the applied table model and sizing settings.
     *
     * @param context - The application context
     * @return - A configured telemetry data table.
     */
    private static JTable buildTelemetryTable(Context context) {
        JTable table;
        ColumnTableModel model;

        ArrayList<TelemetryColumn<?>> columns = new ArrayList<>();
        columns.add(new TelemetryColumn<String>() {
            public String getName() {
                return "name";
            }

            public String getValueAt(int row) {
                return context.getTelemetryName(row);
            }

            public int getRowCount() {
                return context.getTelemetryCount();
            }

            public Class<String> getDataClass() {
                return String.class;
            }

            public boolean isRowEditable(int row) {
                return false;
            }

            public void setValueAt(String val, int row) {
            }
        });

        columns.add(new TelemetryColumn<String>() {
            public String getName() {
                return "Value";
            }

            public String getValueAt(int row) {
                return " " + context.getTelemetry(row);
            }

            public int getRowCount() {
                return context.getTelemetryCount();
            }

            public Class<String> getDataClass() {
                return String.class;
            }

            public boolean isRowEditable(int row) {
                return false;
            }

            public void setValueAt(String val, int row) {
            }
        });

        model = new ColumnTableModel(columns);
        table = new JTable(model);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);

        return table;
    }

    //Table type to return from the factory.
    public enum TableType {Telemetry, Settings, Sliders}
}

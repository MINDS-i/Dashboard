package com.graph;

import com.table.ColumnTableModel;
import com.table.TelemetryColumn;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class GraphConfigWindow {
    private static final Dimension SPINNER_SIZE = new Dimension(80, 20);
    private final Graph subject;
    private final JFrame frame;
    private SpinnerNumberModel strokeModel;
    private Graph.DataConfig closeupData;
    private JColorChooser colorPicker;

    public GraphConfigWindow(Graph subject) {
        this.subject = subject;
        frame = new JFrame("Graph Configuration");

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(buildSpinners());
        container.add(buildSourceTable());
        container.add(buildCloseupPanel());

        frame.add(container);
        frame.setResizable(false);
        frame.pack();
    }

    public void show() {
        frame.setVisible(true);
    }

    public void close() {
        frame.dispose();
    }

    JSpinner numberSpinner(SpinnerNumberModel model, ChangeListener cl) {
        JSpinner js = new JSpinner(model);
        js.setPreferredSize(SPINNER_SIZE);
        js.addChangeListener(cl);
        return js;
    }

    private JPanel buildSpinners() {
        JPanel spinnerPanel = new JPanel();

        ViewSpec vs = subject.getViewSpec();
        double curYscale = vs.maxY() - vs.minY();
        double curYcenter = (vs.maxY() + vs.minY()) / 2.0;
        SpinnerNumberModel yscale =
                new SpinnerNumberModel(curYscale, Double.MIN_NORMAL, Double.MAX_VALUE, 1.0);
        SpinnerNumberModel ycenter =
                new SpinnerNumberModel(curYcenter, -Double.MAX_VALUE, Double.MAX_VALUE, 2.0);
        SpinnerNumberModel xscale = new SpinnerNumberModel(1.0, 0.0, 1.0, 0.05);

        ChangeListener cl = (ChangeEvent e) -> {
            subject.setViewSpec(new RTViewSpec(
                    yscale.getNumber().floatValue(),
                    ycenter.getNumber().floatValue(),
                    xscale.getNumber().floatValue()
            ));
        };

        JCheckBox aa = new JCheckBox("AntiAlias", subject.getAntiAliasing());
        aa.addChangeListener((ChangeEvent e) ->
                subject.setAntiAliasing(aa.getModel().isSelected())
        );

        spinnerPanel.add(aa);
        spinnerPanel.add(new JLabel(" Y Scale:"));
        spinnerPanel.add(numberSpinner(yscale, cl));
        spinnerPanel.add(new JLabel(" Y Center:"));
        spinnerPanel.add(numberSpinner(ycenter, cl));
        spinnerPanel.add(new JLabel(" X Scale:"));
        spinnerPanel.add(numberSpinner(xscale, cl));

        return spinnerPanel;
    }

    private JComponent buildSourceTable() {
        List<Graph.DataConfig> sources = subject.getSources();

        ArrayList<TelemetryColumn<?>> cols = new ArrayList<>();
        cols.add(new TelemetryColumn<String>() {
            public String getName() {
                return "#";
            }

            public String getValueAt(int row) {
                return sources.get(row).getName();
            }

            public int getRowCount() {
                return sources.size();
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
        cols.add(new TelemetryColumn<Boolean>() {
            public String getName() {
                return "Graph?";
            }

            public Boolean getValueAt(int row) {
                return sources.get(row).getDrawn();
            }

            public int getRowCount() {
                return sources.size();
            }

            public Class<Boolean> getDataClass() {
                return Boolean.class;
            }

            public boolean isRowEditable(int row) {
                return true;
            }

            public void setValueAt(Boolean val, int row) {
                sources.get(row).setDrawn(val);
            }
        });

        ColumnTableModel colModel = new ColumnTableModel(cols);
        JTable table = new JTable(colModel);
        table.getSelectionModel().addListSelectionListener(event -> setCloseup(sources.get(table.getSelectedRow())));
        JScrollPane pane = new JScrollPane(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(200, 120));
        pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                BorderFactory.createLineBorder(Color.BLACK)));
        return pane;
    }

    //provides a closeup view of the settings associated with a particular DataConfig
    private JPanel buildCloseupPanel() {
        JPanel panel = new JPanel();

        ChangeListener updateListener = e -> updateCloseupPaint();

        strokeModel = new SpinnerNumberModel(1, 1, 100, 1);

        //construct color picker
        colorPicker = new JColorChooser(Color.BLACK);
        colorPicker.setPreviewPanel(new JPanel());
        colorPicker.getSelectionModel().addChangeListener(updateListener);
        panel.add(colorPicker.getChooserPanels()[0]);

        return panel;
    }

    private void setCloseup(Graph.DataConfig dc) {
        closeupData = dc;
        colorPicker.setColor((Color) closeupData.getPaint());
    }

    private void updateCloseupPaint() {
        if (closeupData == null) {
            return;
        }
        closeupData.setPaint(colorPicker.getColor());
    }

}

package com.ui;

import com.ui.Graph;
import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.util.List;
import java.util.ArrayList;

class GraphConfigWindow{
    private Graph  subject; //the graph to configure
    private JFrame frame;
    private int closeupIndex;

    public GraphConfigWindow(Graph subject){
        this.subject = subject;
        frame = new JFrame("Graph Configuration");

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(buildSpinners());
        container.add(buildSourceTable());
        container.add(buildCloseupPanel());

        frame.add(container);
        frame.pack();
        frame.setVisible(true);
    }

    private JPanel buildSpinners(){
        JPanel spinnerPanel = new JPanel();

        //X scale spinner
        SpinnerNumberModel xScaleM = new SpinnerNumberModel(subject.getXScale(),
                                                           0.01, 1.0, 0.1);
        JSpinner xScaleSpinner = new JSpinner(xScaleM);
        xScaleSpinner.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                subject.setXScale(xScaleM.getNumber().doubleValue());
            }
        });

        //Y scale spinner
        SpinnerNumberModel yScaleM = new SpinnerNumberModel(subject.getYScale(),
                                                           0.01, 10000.0, 0.25);
        JSpinner yScaleSpinner = new JSpinner(yScaleM);
        yScaleSpinner.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                subject.setYScale(yScaleM.getNumber().doubleValue());
            }
        });

        //Y center spinner
        SpinnerNumberModel yCenterM = new SpinnerNumberModel(subject.getYCenter(),
                                                            -10000.0, 10000.0, 0.5);
        JSpinner yCenterSpinner = new JSpinner(yCenterM);
        yCenterSpinner.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                subject.setYCenter(yCenterM.getNumber().doubleValue());
            }
        });

        spinnerPanel.add(new JLabel("X Scale:"));
        spinnerPanel.add(xScaleSpinner);
        spinnerPanel.add(new JLabel(" Y Scale:"));
        spinnerPanel.add(yScaleSpinner);
        spinnerPanel.add(new JLabel(" Y Center:"));
        spinnerPanel.add(yCenterSpinner);

        return spinnerPanel;
    }

    private JPanel buildSourceTable(){
        List<Graph.DataConfig> sources = subject.getSources();

        ArrayList<TableColumn> cols = new ArrayList<TableColumn>();
        cols.add( new TableColumn(){
            public String   getName(){ return "#"; }
            public Object   getValueAt(int row){ return row; }
            public int      getRowCount(){ return 10000; }
            public Class    getDataClass(){ return Integer.class; }
            public boolean  isRowEditable(int row){ return false; }
            public void     setValueAt(Object val, int row){
                if(val.getClass() == Integer.class)
                    setCloseup( sources.get((Integer)val) );
            }
        });
        cols.add( new TableColumn() {
            public String   getName(){ return "Graph?"; }
            public Object   getValueAt(int row){ return sources.get(row).getDrawn(); }
            public int      getRowCount(){ return sources.size(); }
            public Class    getDataClass(){ return Boolean.class; }
            public boolean  isRowEditable(int row){ return true; }
            public void     setValueAt(Object val, int row){
                if(val.getClass() == Boolean.class){
                    sources.get(row).setDrawn((Boolean) val);
                }
            }
        });

        ColumnTableModel colModel = new ColumnTableModel(cols);
        JTable table = new JTable(colModel);
        JPanel tablePanel = new JPanel();
        JScrollPane pane = new JScrollPane(table);
        tablePanel.add(pane);

        return tablePanel;
    }

    private SpinnerNumberModel strokeModel;
    private Graph.DataConfig   closeupData;
    private JColorChooser      colorPicker;

    //provides a closeup view of the settings associated with a particular DataConfig
    private JPanel buildCloseupPanel(){
        JPanel panel = new JPanel();

        strokeModel = new SpinnerNumberModel(1, 1, 100, 1);
        JSpinner lineStroke = new JSpinner(strokeModel);
        lineStroke.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                updateCloseupPaint();
            }
        });

        //construct color picker
        colorPicker = new JColorChooser(Color.BLACK);
        colorPicker.setPreviewPanel(new JPanel());

        panel.add(lineStroke);
        panel.add(colorPicker);

        return panel;
    }

    private void setCloseup(Graph.DataConfig dc){
        closeupData = dc;
        //update number model
        //update color picker
    }

    private void updateCloseupPaint(){
        //build paint from stroke and color picker
        //set closeupData appropriatly
    }

}

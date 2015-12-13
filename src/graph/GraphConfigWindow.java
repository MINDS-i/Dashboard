package com.graph;

import com.table.ColumnTableModel;
import com.table.TableColumn;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.colorchooser.AbstractColorChooserPanel;

class GraphConfigWindow{
    private static final Dimension SPINNER_SIZE = new Dimension(80,20);
    private Graph    subject;
    private JFrame   frame;

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
    }

    public void show(){
        frame.setVisible(true);
    }

    public void close(){
        frame.dispose();
    }

    JSpinner numberSpinner(SpinnerNumberModel model, ChangeListener cl){
        JSpinner js = new JSpinner(model);
        js.setPreferredSize(SPINNER_SIZE);
        js.addChangeListener(cl);
        return js;
    }

    private JPanel buildSpinners(){
        JPanel spinnerPanel = new JPanel();

        ViewSpec vs = subject.getViewSpec();
        double curYscale  =  vs.maxY() - vs.minY();
        double curYcenter = (vs.maxY() + vs.minY()) / 2.0;
        SpinnerNumberModel yscale  =
            new SpinnerNumberModel(curYscale, Double.MIN_NORMAL, Double.MAX_VALUE, 1.0);
        SpinnerNumberModel ycenter =
            new SpinnerNumberModel(curYcenter, -Double.MAX_VALUE, Double.MAX_VALUE, 2.0);
        SpinnerNumberModel xscale  = new SpinnerNumberModel(1.0, 0.0, 1.0, 0.05);

        ChangeListener cl = (ChangeEvent e) -> {
            subject.setViewSpec(new RTViewSpec(
                yscale.getNumber().floatValue(),
                ycenter.getNumber().floatValue(),
                xscale.getNumber().floatValue()
                ));
        };

        spinnerPanel.add(new JLabel(" Y Scale:"));
        spinnerPanel.add(numberSpinner(yscale, cl));
        spinnerPanel.add(new JLabel(" Y Center:"));
        spinnerPanel.add(numberSpinner(ycenter, cl));
        spinnerPanel.add(new JLabel("X Scale:"));
        spinnerPanel.add(numberSpinner(xscale, cl));

        return spinnerPanel;
    }

    private JComponent buildSourceTable(){
        List<Graph.DataConfig> sources = subject.getSources();

        ArrayList<TableColumn> cols = new ArrayList<TableColumn>();
        cols.add( new TableColumn(){
            public String  getName(){ return "#"; }
            public Object  getValueAt(int row){ return sources.get(row).getName(); }
            public int     getRowCount(){ return sources.size(); }
            public Class   getDataClass(){ return String.class; }
            public boolean isRowEditable(int row){ return false; }
            public void    setValueAt(Object val, int row){;}
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
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent event) {
                setCloseup(sources.get(table.getSelectedRow()));
            }
        });
        JScrollPane pane = new JScrollPane(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(200, 120));
        pane.setBorder(BorderFactory.createCompoundBorder(
                         BorderFactory.createEmptyBorder(5, 10, 5,10),
                         BorderFactory.createLineBorder(Color.BLACK)  ));
        return pane;
    }

    private SpinnerNumberModel strokeModel;
    private Graph.DataConfig   closeupData;
    private JColorChooser      colorPicker;

    //provides a closeup view of the settings associated with a particular DataConfig
    private JPanel buildCloseupPanel(){
        JPanel panel = new JPanel();

        ChangeListener updateListener = new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                updateCloseupPaint();
            }
        };

        strokeModel = new SpinnerNumberModel(1, 1, 100, 1);

        //construct color picker
        colorPicker = new JColorChooser(Color.BLACK);
        colorPicker.setPreviewPanel(new JPanel());
        colorPicker.getSelectionModel().addChangeListener(updateListener);
        panel.add(colorPicker.getChooserPanels()[0]);

        return panel;
    }

    private void setCloseup(Graph.DataConfig dc){
        closeupData = dc;
        colorPicker.setColor((Color)closeupData.getPaint());
    }

    private void updateCloseupPaint(){
        if(closeupData == null) return;
        closeupData.setPaint( (Paint) colorPicker.getColor() );
    }

}

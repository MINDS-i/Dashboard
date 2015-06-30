package com.ui;

import com.Context;
import com.ui.Graph;
import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public class SystemConfigWindow{
    private Context context;

    public SystemConfigWindow(Context cxt){
        this.context = cxt;
        JFrame frame = new JFrame("Configuration");

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(new JButton("Woot"));


        frame.add(container);
        frame.pack();
        frame.setVisible(true);
    }
}

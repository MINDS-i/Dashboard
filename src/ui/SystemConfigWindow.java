package com.ui;

import com.Context;
import com.ui.Graph;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.*;

public class SystemConfigWindow{
    private Context context;

    public SystemConfigWindow(Context cxt){
        this.context = cxt;
        JFrame frame = new JFrame("Configuration");

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(new JButton(toggleLocale));

        /*
        Change radio config / baud rate?
        */

        frame.add(container);
        frame.pack();
        frame.setVisible(true);
    }
    private Action toggleLocale = new AbstractAction(){
        {
            String text = "Toggle ground/air mode";
            putValue(Action.NAME, text);
        }
        public void actionPerformed(ActionEvent e){
            context.toggleLocale();
            JFrame mf = new JFrame("message");
            JOptionPane.showMessageDialog(mf, "Changes will take effect next launch");
        }
    };
}

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

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton(toggleLocale));
        container.add(buttonPanel);

        JPanel lpanel = new JPanel();
        lpanel.add(new JLabel("---- Radio Configuration ----"));
        container.add(lpanel);

        container.add(new RadioConfigScreen());

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

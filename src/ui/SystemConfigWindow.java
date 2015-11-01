package com.ui;

import com.Context;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.*;
import javax.swing.text.*;

public class SystemConfigWindow{
    private Context context;
    protected static final String COPY_RIGHT_TEXT = "Map Tiles Courtesy of MapQuest" +
        "\nStreet Data from OpenStreetMap\nPortions Courtesy NASA/JPL-Caltech" +
        " and\nU.S. Depart. of Agriculture, Farm Service Agency";

    public SystemConfigWindow(Context cxt){
        this.context = cxt;
        JFrame frame = new JFrame("Configuration");

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton(toggleLocale));
        if(isWindows())
            buttonPanel.add(new JButton(driverExec));
        container.add(buttonPanel);

        JPanel lpanel = new JPanel();
        lpanel.add(new JLabel("---- Radio Configuration ----"));
        container.add(lpanel);

        container.add(new RadioConfigScreen());

        container.add(Box.createRigidArea(new Dimension(0,10)));

        JTextPane copyRights = new JTextPane();
        Font tmp = copyRights.getFont();
        copyRights.setFont( tmp.deriveFont(9f) );
        copyRights.setOpaque(false);
        StyledDocument doc = copyRights.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        copyRights.setText(COPY_RIGHT_TEXT);
        container.add(copyRights);

        frame.add(container);
        frame.pack();
        frame.setVisible(true);
    }
    private boolean isWindows(){
        String OS = System.getProperty("os.name");
        return OS.startsWith("Windows");
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
    private Action driverExec = new AbstractAction(){
        {
            String text = "Launch driver installer";
            putValue(Action.NAME, text);
        }
        public void actionPerformed(ActionEvent e){
            String[] cmd = { "RadioDiversv2.12.06WHQL_Centified.exe" };
            try{
                Process p = Runtime.getRuntime().exec(cmd);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };
}

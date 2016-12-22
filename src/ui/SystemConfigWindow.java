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

public class SystemConfigWindow {
    private Context context;
    protected static final String COPY_RIGHT_TEXT =
        "Map data courtesy of\n" +
        "Esri, DigitalGlobe, Earthstar Geographics, CNES/Airbus DS, GeoEye, USDA FSA, USGS, Getmapping, Aerogrid, IGN, IGP, and the GIS User Community\n"+
        "Maps © www.thunderforest.com, Data © www.osm.org/copyright";

    public SystemConfigWindow(Context cxt) {
        this.context = cxt;
        // make frame and vertical box container
        JFrame frame = new JFrame("Configuration");
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // Add ground/air switch panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton(toggleLocale));
        if(isWindows())
            buttonPanel.add(new JButton(driverExec));
        container.add(buttonPanel);

        // Add radio configuration area
        JPanel lpanel = new JPanel();
        lpanel.add(new JLabel("---- Radio Configuration ----"));
        container.add(lpanel);
        container.add(new RadioConfigScreen());
        container.add(Box.createRigidArea(new Dimension(0,10)));

        // Add version numbers
        String versionString = String.format(
            "MINDS-i Dashboard | Version %s | %s",
            context.getResource("version_id"),
            context.getResource("release_date"));
        JLabel versionPane = new JLabel(versionString);
        versionPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(versionPane);

        // Add copy right notices
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
    private boolean isWindows() {
        String osname = System.getProperty("os.name");
        return osname.toLowerCase().contains("windows");
    }
    private Action toggleLocale = new AbstractAction() {
        {
            String text = "Toggle ground/air mode";
            putValue(Action.NAME, text);
        }
        public void actionPerformed(ActionEvent e) {
            context.toggleLocale();
            JFrame mf = new JFrame("message");
            JOptionPane.showMessageDialog(mf, "Changes will take effect next launch");
        }
    };
    private Action driverExec = new AbstractAction() {
        {
            String text = "Launch driver installer";
            putValue(Action.NAME, text);
        }
        public void actionPerformed(ActionEvent e) {
            String[] cmd = { "RadioDiversv2.12.06WHQL_Centified.exe" };
            try {
                Process p = Runtime.getRuntime().exec(cmd);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };
}

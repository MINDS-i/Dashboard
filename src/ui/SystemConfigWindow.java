package com.ui;

import com.Context;
import com.map.MapPanel;

import javax.swing.*;
import java.awt.*;

public class SystemConfigWindow {
    protected static final String COPY_RIGHT_TEXT =
            "<html><p align=\"center\">" +
            "Map data courtesy of:" +
            "Esri, DigitalGlobe, Earthstar Geographics, CNES/Airbus DS, GeoEye,<br>" +
            "USDA FSA, USGS, Getmapping, Aerogrid, IGN, IGP, and the GIS User Community<br>" +
            "Maps © www.thunderforest.com, Data © www.osm.org/copyright" +
            "</p></html>";
    private final Context context;
    private final JFrame frame;
    private final MapPanel map;
    private final JLabel versionPane;

    public SystemConfigWindow(Context cxt, MapPanel mapPanel) {
        this.context = cxt;
        this.map = mapPanel;

        // make frame and vertical box container
        frame = new JFrame("Configuration");
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // Add ui configuration area
        JPanel uiTitlePanel = new JPanel();
        uiTitlePanel.add(new JLabel("---- UI Configuration ----"));
        container.add(uiTitlePanel);
        container.add(new UIConfigPanel(this.context, map, isWindows()));

        // Add map tile cache config area
        JPanel tileCacheTitlePanel = new JPanel();
        tileCacheTitlePanel.add(new JLabel("---- Map Tile Cache Configuration ----"));
        container.add(tileCacheTitlePanel);
        container.add(new TileCacheConfigPanel(this.context, map));

        // Add radio configuration area
        JPanel lpanel = new JPanel();
        lpanel.add(new JLabel("---- Radio Configuration ----"));
        container.add(lpanel);
        container.add(new RadioConfigScreen());
        container.add(Box.createRigidArea(new Dimension(0, 10)));

        // Add version numbers
        versionPane = new JLabel("Waiting...");
        versionPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(versionPane);

        // Getting the APM version requires making a request to the remote system.
        // We pass the context a callback so that it can make a request, and then we
        // can update the UI when it's complete.
        context.getAPMVersion(this::setVersionString);

        // Add copyright notices
        JLabel copyRights = new JLabel();
        Font tmp = copyRights.getFont();
        copyRights.setFont(tmp.deriveFont(9f));
        copyRights.setOpaque(false);
        copyRights.setAlignmentX(Component.CENTER_ALIGNMENT);
        copyRights.setHorizontalAlignment(SwingConstants.CENTER);
        copyRights.setHorizontalTextPosition(SwingConstants.CENTER);
        copyRights.setText(COPY_RIGHT_TEXT);
        container.add(copyRights);

        frame.add(container);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private String formatVersionString(String apmVersion) {
        return String.format(
                "MINDS-i Dashboard | Dashboard Version %s | APM Version %s | %s",
                context.getResource("version_id"),
                apmVersion,
                context.getResource("release_date"));
    }

    private Void setVersionString(String apmVersion) {
        SwingUtilities.invokeLater(() -> {
            versionPane.setText(formatVersionString(apmVersion));
        });
        return null;
    }

    private boolean isWindows() {
        String osname = System.getProperty("os.name");
        return osname.toLowerCase().contains("windows");
    }

    public void toFront() {
        frame.toFront();
    }

    public boolean getVisible() {
        return frame.isVisible();
    }
}

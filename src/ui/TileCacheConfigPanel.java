package com.ui;

import com.Context;
import com.map.MapPanel;
import com.map.TileLoadingCallback;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.map.TileServer.CACHE_NAME;

public class TileCacheConfigPanel extends JPanel {
    private final Context context;
    private final MapPanel map;
    private static final Logger logger = LoggerFactory.getLogger(TileCacheConfigPanel.class);

    private final JLabel cacheSizeLabel = new JLabel("Cache size: ");

    private class CacheSizeCalculatorThread extends Thread {
        @Override
        public void run() {
            AtomicLong size = new AtomicLong(0);

            try {
                Path folder = Paths.get(System.getProperty("java.io.tmpdir"), CACHE_NAME);
                cacheSizeLabel.setText("Cache size: calculating...");

                Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        size.addAndGet(attrs.size());
                        return FileVisitResult.CONTINUE;
                    }
                });

                cacheSizeLabel.setText(String.format("Cache size: %d MB", size.longValue() / (1024 * 1024)));
            }
            catch (NoSuchFileException e) {
                logger.warn("No cache exists.  That's fine if it was just cleared.");
                cacheSizeLabel.setText("Cache size: 0 MB");
            }
            catch (IOException e) {
                logger.warn("Error calculating cache size", e);
                cacheSizeLabel.setText("Cache size: Unknown");
            }
            catch (Exception e) {
                logger.warn("Interrupted while calculating cache size", e);
            }
        }
    }

    public TileCacheConfigPanel(Context ctx, MapPanel map) {
        this.context = ctx;
        this.map = map;

        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 5, 0, 5);

        // Tile cache controls
        // Map tile cache configuration and control
        JCheckBox enableTileCacheCheckBox = new JCheckBox("Enable Tile Cache");
        enableTileCacheCheckBox.setSelected(ctx.getEnableTileCacheProp());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        enableTileCacheCheckBox.addItemListener(itemEvent -> {
            switch (itemEvent.getStateChange()) {
                case ItemEvent.SELECTED:
                    context.setEnableTileCacheProp(true);
                    break;
                case ItemEvent.DESELECTED:
                    context.setEnableTileCacheProp(false);
                    break;
            }
        });
        this.add(enableTileCacheCheckBox, constraints);

        JButton clearTileCacheButton = new JButton("Clear Tile Cache");
        constraints.gridx = 1;
        clearTileCacheButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                logger.info("Clearing tile cache");
                Path folder = Paths.get(System.getProperty("java.io.tmpdir"), CACHE_NAME);
                try {
                    FileUtils.deleteDirectory(folder.toFile());
                }
                catch (IOException e) {
                    logger.error("Error deleting tile cache", e);
                }
                recalculateCacheSize();
            }
        });
        this.add(clearTileCacheButton, constraints);

        JButton seedTileCacheButton = new JButton("Seed Tile Cache");
        constraints.gridx = 2;
        final TileCacheConfigPanel self = this;
        seedTileCacheButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String message = "Seeding the tile cache will download every " +
                        "map tile within " + MapPanel.SEED_CACHE_RADIUS_KM +
                        " km of the home point.  This operation may take several minutes to " +
                        "complete.  Continue?";
                message = String.format("<html><body><p style='width: 400px'>%s</p></body></html>", message);
                int result = JOptionPane.showConfirmDialog(self,
                        message,
                        "Seed Tile Cache",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    seedTileCache();
                }
            }
        });
        this.add(seedTileCacheButton, constraints);

        constraints.gridx = 3;
        this.add(cacheSizeLabel, constraints);

        recalculateCacheSize();
    }

    private void seedTileCache() {
        Window window = SwingUtilities.getWindowAncestor(this);
        this.map.seedTileCache(new TileLoadingCallback() {
            // The map panel may load multiple sources at once, so consolidate their progress
            // into a single value.
            String lastStr = "";
            final Map<String, Integer> sourceTotals = new HashMap<>();
            final Map<String, Integer> sourceProgress = new HashMap<>();
            final AtomicInteger finishedCount = new AtomicInteger();

            @Override
            public void started(String source, int total) {
                sourceTotals.put(source, total);
            }

            private void updateLabel() {
                AtomicInteger realTotal = new AtomicInteger();
                AtomicInteger realProgress = new AtomicInteger();
                sourceTotals.values().forEach(realTotal::addAndGet);
                sourceProgress.values().forEach(realProgress::addAndGet);

                double doubleTotal = realTotal.doubleValue();

                double pct = doubleTotal > 0.0 ? 100.0 * (realProgress.doubleValue() / doubleTotal) : 0.0;

                String strVal = String.format("%.02f%%", pct);
                if (!strVal.equals(lastStr)) {
                    lastStr = strVal;
                    cacheSizeLabel.setText(String.format("Seeding cache: %s", lastStr));
                }
            }

            @Override
            public void progress(String source, int number) {
                sourceProgress.put(source, number);
                updateLabel();
            }

            @Override
            public void done(String source) {
                int finished = finishedCount.addAndGet(1);
                if (finished == sourceTotals.size()) {
                    recalculateCacheSize();
                }
            }
        });
        if (window != null) {
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    logger.info("Stopping cache thread.");
                    map.stopSeeding();
                }
            });
        }
        else {
            logger.error("Unable to get parent window; cache thread may not stop properly.");
        }
    }

    private void recalculateCacheSize() {
        CacheSizeCalculatorThread thread = new CacheSizeCalculatorThread();
        thread.setDaemon(true);
        thread.start();

        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    thread.interrupt();
                }
            });
        }
    }
}

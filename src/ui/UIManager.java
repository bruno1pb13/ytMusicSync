package ui;

import application.Application;
import com.formdev.flatlaf.FlatLightLaf;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.Separator;
import dorkbox.systemTray.SystemTray;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UIManager {

    private final Application app;
    private MainWindow mainWindow;
    private SystemTray tray;

    public UIManager(Application app) {
        this.app = app;
    }

    public void initialize() {
        // Install FlatLaf and apply Material You customizations before any component is created
        FlatLightLaf.install();
        MaterialTheme.apply();

        SwingUtilities.invokeLater(() -> {
            mainWindow = new MainWindow(app);
            mainWindow.setVisible(true);
            app.restoreAutoSync();
            setupSystemTray();
        });
    }

    private void setupSystemTray() {
        try {
            SystemTray.DEBUG = true;
            tray = SystemTray.get();
        } catch (Exception e) {
            System.err.println("Nao foi possivel iniciar o system tray: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        if (tray == null) {
            System.err.println("SystemTray.get() retornou null");
            return;
        }
        System.out.println("SystemTray iniciado: tipo=" + tray.getType());

        tray.setImage(createTrayImage());
        tray.setTooltip("YT Music Sync");

        dorkbox.systemTray.Menu menu = tray.getMenu();
        menu.add(new MenuItem("Mostrar", e -> SwingUtilities.invokeLater(() -> mainWindow.showWindow())));
        menu.add(new MenuItem("Ocultar", e -> SwingUtilities.invokeLater(() -> mainWindow.hideWindow())));
        menu.add(new Separator());
        menu.add(new MenuItem("Sair", e -> System.exit(0)));

        app.addPropertyChangeListener(evt -> {
            String prop = evt.getPropertyName();
            if ("syncProgress".equals(prop) || "syncInProgress".equals(prop)) {
                tray.setTooltip(buildTrayTooltip());
            }
        });
    }

    private String buildTrayTooltip() {
        if (!app.isSyncInProgress()) {
            return "YT Music Sync";
        }
        String currentVideo = app.getSyncCurrentVideo();
        int current = app.getSyncDownloadCurrent();
        int total = app.getSyncDownloadTotal();
        if (total > 0) {
            return "Sincronizando " + current + "/" + total +
                    (currentVideo != null ? ": " + currentVideo : "");
        }
        return currentVideo != null ? currentVideo : "Sincronizando...";
    }

    private BufferedImage createTrayImage() {
        int size = 64;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(0x6750A4));
        g.fillOval(0, 0, size, size);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 40));
        FontMetrics fm = g.getFontMetrics();
        String note = "\u266B";
        int x = (size - fm.stringWidth(note)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(note, x, y);

        g.dispose();
        return img;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }
}

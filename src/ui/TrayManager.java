package ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import application.Application;

public class TrayManager {

    private final Application app;
    private TrayIcon trayIcon;

    private final Image iconAutoOff; // ícone vermelho
    private final Image iconAutoOn;  // ícone verde

    public TrayManager(Application app) {
        this.app = app;

        // Criar ícones
        iconAutoOff = createIcon(Color.RED);
        iconAutoOn = createIcon(Color.GREEN);

        app.addPropertyChangeListener(evt -> {
            if ("autoSyncRunning".equals(evt.getPropertyName())) {
                boolean running = (boolean) evt.getNewValue();
                updateTrayIcon(running);
            }
        });

    }

    private Image createIcon(Color color) {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillOval(0, 0, 16, 16);
        g.dispose();
        return image;
    }

    public void initTray() {

        if (!SystemTray.isSupported()) {
            System.out.println("System tray não suportado neste sistema.");
            return;
        }

        try {
            PopupMenu menu = new PopupMenu();
            MenuItem syncItem = new MenuItem("Sincronizar agora");
            MenuItem exitItem = new MenuItem("Sair");
            menu.add(syncItem);
            menu.addSeparator();
            menu.add(exitItem);

            // Inicialmente assume que auto sync está desligado
            trayIcon = new TrayIcon(iconAutoOff, "YT Music Sync", menu);
            trayIcon.setImageAutoSize(true);

            SystemTray.getSystemTray().add(trayIcon);

            syncItem.addActionListener(e -> {
                trayIcon.displayMessage("YT Music Sync", "Iniciando sincronização...", TrayIcon.MessageType.INFO);
                new Thread(app::triggerSyncNow).start();
            });

            exitItem.addActionListener(e -> {
                SystemTray.getSystemTray().remove(trayIcon);
                System.exit(0);
            });

        } catch (Exception e) {
            System.err.println("Erro ao inicializar tray: " + e.getMessage());
        }
    }

    /**
     * Atualiza o ícone do tray dependendo se a sincronização automática está ativa ou não.
     */
    public void updateTrayIcon(boolean autoSyncActive) {
        if (trayIcon != null) {
            trayIcon.setImage(autoSyncActive ? iconAutoOn : iconAutoOff);
            trayIcon.setToolTip("YT Music Sync - Auto Sync " + (autoSyncActive ? "Ativo" : "Desligado"));
        }
    }
}

package ui;

import application.Application;

import javax.swing.*;

/**
 * @deprecated Esta classe está obsoleta. Use {@link UIManager} ao invés.
 * Mantida apenas para compatibilidade temporária.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
public class TrayManager {

    private final Application app;
    private MainWindow mainWindow;

    @Deprecated
    public TrayManager(Application app) {
        this.app = app;
        this.mainWindow = new MainWindow(app);
    }

    @Deprecated
    public void initTray() {
        System.out.println("[AVISO] TrayManager está obsoleto. Use UIManager.");

        SwingUtilities.invokeLater(() -> {
            mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainWindow.showWindow();
        });
    }

    @Deprecated
    public void updateTrayIcon(boolean autoSyncActive) {
        // Método mantido por compatibilidade
        // Não faz nada
    }
}

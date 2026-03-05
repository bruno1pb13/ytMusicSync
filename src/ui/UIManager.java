package ui;

import application.Application;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class UIManager {

    private final Application app;
    private MainWindow mainWindow;

    public UIManager(Application app) {
        this.app = app;
    }

    public void initialize() {
        // Install FlatLaf and apply Material You customizations before any component is created
        FlatLightLaf.install();
        MaterialTheme.apply();

        SwingUtilities.invokeLater(() -> {
            mainWindow = new MainWindow(app);
            mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainWindow.setVisible(true);
        });
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }
}

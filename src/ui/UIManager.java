package ui;

import application.Application;

import javax.swing.*;

/**
 * Gerenciador da interface gráfica da aplicação.
 * Segue o Single Responsibility Principle: responsável apenas por inicializar e coordenar a UI.
 */
public class UIManager {

    private final Application app;
    private MainWindow mainWindow;

    public UIManager(Application app) {
        this.app = app;
    }

    /**
     * Inicializa a interface gráfica.
     * Cria e exibe a janela principal.
     */
    public void initialize() {
        SwingUtilities.invokeLater(() -> {
            mainWindow = new MainWindow(app);
            mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainWindow.setVisible(true);
        });
    }

    /**
     * Retorna a janela principal.
     * @return MainWindow instance
     */
    public MainWindow getMainWindow() {
        return mainWindow;
    }
}

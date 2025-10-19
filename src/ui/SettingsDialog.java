package ui;

import application.Application;
import util.Config;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;

/**
 * Diálogo de configurações da aplicação.
 */
public class SettingsDialog extends JDialog {

    private final Application app;
    private final Config config;

    // Campos de configuração
    private JTextField downloadDirField;
    private JTextField intervalField;
    private JTextField ytDlpPathField;
    private JComboBox<String> audioFormatCombo;
    private JComboBox<String> audioQualityCombo;

    public SettingsDialog(JFrame parent, Application app) {
        super(parent, "Configurações", true);
        this.app = app;
        this.config = app.getConfig();
        initUI();
        loadCurrentSettings();
    }

    private void initUI() {
        setSize(600, 450);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        // Layout principal
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // Painel central - Campos de configuração
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Painel inferior - Botões
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createCenterPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Seção: Caminhos
        mainPanel.add(createPathsPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        // Seção: Sincronização
        mainPanel.add(createSyncPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        // Seção: Áudio
        mainPanel.add(createAudioPanel());

        return mainPanel;
    }

    private JPanel createPathsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Caminhos"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Diretório de downloads
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Diretório de Downloads:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        downloadDirField = new JTextField(30);
        panel.add(downloadDirField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton browseDownloadButton = new JButton("Procurar");
        browseDownloadButton.addActionListener(e -> browseDirectory(downloadDirField));
        panel.add(browseDownloadButton, gbc);

        // Caminho do yt-dlp
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Caminho do yt-dlp:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        ytDlpPathField = new JTextField(30);
        panel.add(ytDlpPathField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton browseYtDlpButton = new JButton("Procurar");
        browseYtDlpButton.addActionListener(e -> browseFile(ytDlpPathField));
        panel.add(browseYtDlpButton, gbc);

        return panel;
    }

    private JPanel createSyncPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sincronização"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Intervalo de Verificação (minutos):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        intervalField = new JTextField(10);
        panel.add(intervalField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JLabel hintLabel = new JLabel("(mínimo: 1)");
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 11f));
        hintLabel.setForeground(Color.GRAY);
        panel.add(hintLabel, gbc);

        return panel;
    }

    private JPanel createAudioPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Áudio"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Formato de áudio
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Formato de Áudio:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        audioFormatCombo = new JComboBox<>(new String[]{"mp3", "m4a", "opus"});
        panel.add(audioFormatCombo, gbc);

        // Qualidade de áudio
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Qualidade de Áudio (kbps):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        audioQualityCombo = new JComboBox<>(new String[]{"128", "192", "256", "320"});
        audioQualityCombo.setEditable(true);
        panel.add(audioQualityCombo, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JLabel hintLabel = new JLabel("(64-320)");
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 11f));
        hintLabel.setForeground(Color.GRAY);
        panel.add(hintLabel, gbc);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        // Botões
        JButton saveButton = new JButton("Salvar");
        saveButton.addActionListener(e -> saveSettings());

        panel.add(cancelButton);
        panel.add(saveButton);

        return panel;
    }

    private void loadCurrentSettings() {
        downloadDirField.setText(config.getDownloadDirectory());
        intervalField.setText(String.valueOf(config.getCheckIntervalMinutes()));
        ytDlpPathField.setText(config.getYtDlpPath());
        audioFormatCombo.setSelectedItem(config.getAudioFormat());
        audioQualityCombo.setSelectedItem(config.getAudioQuality());
    }

    private void browseDirectory(JTextField targetField) {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Selecionar Diretório");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        String currentPath = targetField.getText();
        if (!currentPath.isEmpty()) {
            File currentDir = new File(currentPath);
            if (currentDir.exists()) {
                fileChooser.setCurrentDirectory(currentDir);
            }
        }

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            targetField.setText(selectedDir.getAbsolutePath());
        }
    }

    private void browseFile(JTextField targetField) {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Selecionar Arquivo");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        String currentPath = targetField.getText();
        if (!currentPath.isEmpty()) {
            File currentFile = new File(currentPath);
            if (currentFile.exists()) {
                fileChooser.setSelectedFile(currentFile);
            } else if (currentFile.getParentFile() != null && currentFile.getParentFile().exists()) {
                fileChooser.setCurrentDirectory(currentFile.getParentFile());
            }
        }

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            targetField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void saveSettings() {
        try {
            // Validar intervalo
            int interval = Integer.parseInt(intervalField.getText().trim());
            if (interval < 1) {
                JOptionPane.showMessageDialog(this,
                        "O intervalo deve ser no mínimo 1 minuto.",
                        "Erro de Validação",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validar qualidade de áudio
            String qualityStr = (String) audioQualityCombo.getSelectedItem();
            int quality = Integer.parseInt(qualityStr.trim());
            if (quality < 64 || quality > 320) {
                JOptionPane.showMessageDialog(this,
                        "A qualidade de áudio deve estar entre 64 e 320 kbps.",
                        "Erro de Validação",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validar formato de áudio
            String format = (String) audioFormatCombo.getSelectedItem();
            if (!format.equals("mp3") && !format.equals("m4a") && !format.equals("opus")) {
                JOptionPane.showMessageDialog(this,
                        "Formato de áudio inválido. Use: mp3, m4a ou opus.",
                        "Erro de Validação",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Salvar configurações
            boolean intervalChanged = interval != config.getCheckIntervalMinutes();

            config.setDownloadDirectory(downloadDirField.getText().trim());
            config.setCheckIntervalMinutes(interval);
            config.setYtDlpPath(ytDlpPathField.getText().trim());
            config.setAudioFormat(format);
            config.setAudioQuality(qualityStr.trim());

            // Se o intervalo mudou e o auto-sync está rodando, perguntar se quer reiniciar
            if (intervalChanged && app.isAutoSyncRunning()) {
                int result = JOptionPane.showConfirmDialog(this,
                        "A sincronização automática está ativa.\n" +
                        "Deseja reiniciá-la para aplicar o novo intervalo?",
                        "Reiniciar Sincronização?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    app.stopAutoSync();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    app.startAutoSync();
                    JOptionPane.showMessageDialog(this,
                            "Configurações salvas e sincronização reiniciada!",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Configurações salvas!\nO novo intervalo será aplicado no próximo início.",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Configurações salvas com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Valores numéricos inválidos. Verifique os campos de intervalo e qualidade.",
                    "Erro de Validação",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar configurações: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

package ui;

import application.Application;
import util.Config;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;

public class SettingsDialog extends JDialog {

    private final Application app;
    private final Config config;

    private JTextField downloadDirField;
    private JTextField intervalField;
    private JTextField ytDlpPathField;
    private JComboBox<String> audioFormatCombo;
    private JComboBox<String> audioQualityCombo;
    private JCheckBox cookiesEnabledCheckbox;
    private JComboBox<String> cookiesBrowserCombo;

    public SettingsDialog(JFrame parent, Application app) {
        super(parent, "Configuracoes", true);
        this.app    = app;
        this.config = app.getConfig();
        initUI();
        loadCurrentSettings();
    }

    private void initUI() {
        setMinimumSize(new Dimension(620, 480));
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
        setBackground(MaterialTheme.BACKGROUND);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(MaterialTheme.BACKGROUND);
        setContentPane(root);

        root.add(createHeader(),  BorderLayout.NORTH);
        root.add(createContent(), BorderLayout.CENTER);
        root.add(createFooter(),  BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getParent());
    }

    // -------------------------------------------------------------------------
    // Layout builders
    // -------------------------------------------------------------------------

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MaterialTheme.PRIMARY);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel titleLabel = new JLabel("Configuracoes");
        titleLabel.setFont(MaterialTheme.titleMedium());
        titleLabel.setForeground(MaterialTheme.ON_PRIMARY);

        header.add(titleLabel, BorderLayout.WEST);
        return header;
    }

    private JScrollPane createContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(MaterialTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        content.add(buildSection("Caminhos",                   buildPathsPanel()));
        content.add(Box.createVerticalStrut(14));
        content.add(buildSection("Sincronizacao",              buildSyncPanel()));
        content.add(Box.createVerticalStrut(14));
        content.add(buildSection("Autenticacao (Playlists Privadas)", buildAuthPanel()));
        content.add(Box.createVerticalStrut(14));
        content.add(buildSection("Audio",                      buildAudioPanel()));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, MaterialTheme.OUTLINE_VARIANT));
        scroll.getViewport().setBackground(MaterialTheme.BACKGROUND);
        return scroll;
    }

    private JPanel buildSection(String title, JPanel body) {
        JPanel card = MaterialTheme.card(MaterialTheme.SURFACE_CONTAINER_LOW);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel label = new JLabel(title);
        label.setFont(MaterialTheme.titleSmall());
        label.setForeground(MaterialTheme.PRIMARY);
        label.setBorder(new EmptyBorder(0, 0, 6, 0));

        card.add(label, BorderLayout.NORTH);
        card.add(body,  BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPathsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = defaultGbc();

        // Download directory
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(fieldLabel("Diretorio de Downloads:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        downloadDirField = new JTextField(30);
        panel.add(downloadDirField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        JButton browseDownloadBtn = MaterialTheme.outlinedButton("Procurar");
        browseDownloadBtn.addActionListener(e -> browseDirectory(downloadDirField));
        panel.add(browseDownloadBtn, gbc);

        // yt-dlp path
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(fieldLabel("Caminho do yt-dlp:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        ytDlpPathField = new JTextField(30);
        panel.add(ytDlpPathField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        JButton browseYtDlpBtn = MaterialTheme.outlinedButton("Procurar");
        browseYtDlpBtn.addActionListener(e -> browseFile(ytDlpPathField));
        panel.add(browseYtDlpBtn, gbc);

        return panel;
    }

    private JPanel buildSyncPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = defaultGbc();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(fieldLabel("Intervalo de Verificacao (minutos):"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        intervalField = new JTextField(10);
        panel.add(intervalField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(hintLabel("(minimo: 1)"), gbc);

        return panel;
    }

    private JPanel buildAuthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = defaultGbc();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(fieldLabel("Usar cookies do navegador:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 2;
        cookiesEnabledCheckbox = new JCheckBox("Habilitar acesso a playlists privadas");
        cookiesEnabledCheckbox.setOpaque(false);
        cookiesEnabledCheckbox.addActionListener(e ->
                cookiesBrowserCombo.setEnabled(cookiesEnabledCheckbox.isSelected()));
        panel.add(cookiesEnabledCheckbox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(fieldLabel("Navegador:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        cookiesBrowserCombo = new JComboBox<>(
                new String[]{"chrome", "firefox", "edge", "safari", "opera", "brave", "chromium"});
        panel.add(cookiesBrowserCombo, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(hintLabel("(Navegador deve estar logado no YouTube)"), gbc);

        return panel;
    }

    private JPanel buildAudioPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = defaultGbc();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(fieldLabel("Formato de Audio:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        audioFormatCombo = new JComboBox<>(new String[]{"mp3", "m4a", "opus"});
        panel.add(audioFormatCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(fieldLabel("Qualidade de Audio (kbps):"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        audioQualityCombo = new JComboBox<>(new String[]{"128", "192", "256", "320"});
        audioQualityCombo.setEditable(true);
        panel.add(audioQualityCombo, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(hintLabel("(64–320)"), gbc);

        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(MaterialTheme.SURFACE_CONTAINER);

        JButton cancelBtn = MaterialTheme.textButton("Cancelar");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = MaterialTheme.filledButton("Salvar");
        saveBtn.addActionListener(e -> saveSettings());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        return footer;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static GridBagConstraints defaultGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 1;
        return gbc;
    }

    private static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MaterialTheme.bodyMedium());
        label.setForeground(MaterialTheme.ON_SURFACE);
        return label;
    }

    private static JLabel hintLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MaterialTheme.bodySmall());
        label.setForeground(MaterialTheme.ON_SURFACE_VARIANT);
        return label;
    }

    // -------------------------------------------------------------------------
    // Load / Save
    // -------------------------------------------------------------------------

    private void loadCurrentSettings() {
        downloadDirField.setText(config.getDownloadDirectory());
        intervalField.setText(String.valueOf(config.getCheckIntervalMinutes()));
        ytDlpPathField.setText(config.getYtDlpPath());
        audioFormatCombo.setSelectedItem(config.getAudioFormat());
        audioQualityCombo.setSelectedItem(config.getAudioQuality());
        cookiesEnabledCheckbox.setSelected(config.getCookiesEnabled());
        cookiesBrowserCombo.setSelectedItem(config.getCookiesBrowser());
        cookiesBrowserCombo.setEnabled(config.getCookiesEnabled());
    }

    private void browseDirectory(JTextField targetField) {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Selecionar Diretorio");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        String currentPath = targetField.getText();
        if (!currentPath.isEmpty()) {
            File currentDir = new File(currentPath);
            if (currentDir.exists()) {
                fileChooser.setCurrentDirectory(currentDir);
            }
        }

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            targetField.setText(fileChooser.getSelectedFile().getAbsolutePath());
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

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            targetField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void saveSettings() {
        try {
            int interval = Integer.parseInt(intervalField.getText().trim());
            if (interval < 1) {
                JOptionPane.showMessageDialog(this,
                        "O intervalo deve ser no minimo 1 minuto.",
                        "Erro de Validacao", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String qualityStr = (String) audioQualityCombo.getSelectedItem();
            int quality = Integer.parseInt(qualityStr.trim());
            if (quality < 64 || quality > 320) {
                JOptionPane.showMessageDialog(this,
                        "A qualidade de audio deve estar entre 64 e 320 kbps.",
                        "Erro de Validacao", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String format = (String) audioFormatCombo.getSelectedItem();
            if (!format.equals("mp3") && !format.equals("m4a") && !format.equals("opus")) {
                JOptionPane.showMessageDialog(this,
                        "Formato de audio invalido. Use: mp3, m4a ou opus.",
                        "Erro de Validacao", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean intervalChanged = interval != config.getCheckIntervalMinutes();

            config.setDownloadDirectory(downloadDirField.getText().trim());
            config.setCheckIntervalMinutes(interval);
            config.setYtDlpPath(ytDlpPathField.getText().trim());
            config.setAudioFormat(format);
            config.setAudioQuality(qualityStr.trim());
            config.setCookiesEnabled(cookiesEnabledCheckbox.isSelected());
            config.setCookiesBrowser((String) cookiesBrowserCombo.getSelectedItem());

            if (intervalChanged && app.isAutoSyncRunning()) {
                int result = JOptionPane.showConfirmDialog(this,
                        "A sincronizacao automatica esta ativa.\n" +
                        "Deseja reinicia-la para aplicar o novo intervalo?",
                        "Reiniciar Sincronizacao?",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    app.stopAutoSync();
                    try { Thread.sleep(500); } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    app.startAutoSync();
                    JOptionPane.showMessageDialog(this,
                            "Configuracoes salvas e sincronizacao reiniciada!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Configuracoes salvas!\nO novo intervalo sera aplicado no proximo inicio.",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Configuracoes salvas com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Valores numericos invalidos. Verifique os campos de intervalo e qualidade.",
                    "Erro de Validacao", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar configuracoes: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}

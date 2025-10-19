package ui;

import application.Application;
import domain.Playlist;
import exception.PrivatePlaylistException;
import service.SyncService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame {

    private final Application app;
    private JTable playlistTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JButton syncButton;
    private JButton autoSyncButton;
    private JButton addButton;
    private JButton removeButton;
    private Timer updateTimer;

    public MainWindow(Application app) {
        this.app = app;
        initUI();
        setupListeners();
        startUpdateTimer();
        updateUI();
    }

    private void initUI() {
        setTitle("YT Music Sync - Gerenciador");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Status"));

        statusLabel = new JLabel("Carregando...");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(statusLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Playlists"));

        String[] columnNames = {"Título", "Vídeos", "Baixados", "Pendentes", "Última Sinc."};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        playlistTable = new JTable(tableModel);
        playlistTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlistTable.setRowHeight(25);
        playlistTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        playlistTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        playlistTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        playlistTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        playlistTable.getColumnModel().getColumn(4).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(playlistTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel playlistButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        addButton = new JButton("Adicionar Playlist");
        addButton.addActionListener(e -> addPlaylist());

        removeButton = new JButton("Remover Playlist");
        removeButton.addActionListener(e -> removePlaylist());
        removeButton.setEnabled(false);

        JButton syncSelectedButton = new JButton("Sincronizar Selecionada");
        syncSelectedButton.addActionListener(e -> syncSelected());

        playlistButtonsPanel.add(addButton);
        playlistButtonsPanel.add(removeButton);
        playlistButtonsPanel.add(syncSelectedButton);

        JPanel syncButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton settingsButton = new JButton("Configurações");
        settingsButton.addActionListener(e -> openSettings());

        syncButton = new JButton("Sincronizar Todas");
        syncButton.addActionListener(e -> syncAll());

        autoSyncButton = new JButton("Iniciar Sinc. Automática");
        autoSyncButton.addActionListener(e -> toggleAutoSync());

        syncButtonsPanel.add(settingsButton);
        syncButtonsPanel.add(syncButton);
        syncButtonsPanel.add(autoSyncButton);

        panel.add(playlistButtonsPanel, BorderLayout.WEST);
        panel.add(syncButtonsPanel, BorderLayout.EAST);

        return panel;
    }

    private void setupListeners() {
        playlistTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                removeButton.setEnabled(playlistTable.getSelectedRow() != -1);
            }
        });

        app.addPropertyChangeListener(evt -> {
            SwingUtilities.invokeLater(this::updateUI);
        });
    }

    private void startUpdateTimer() {
        updateTimer = new Timer(3000, e -> updateUI());
        updateTimer.start();
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            updateStatus();
            updateTable();
            updateButtons();
        });
    }

    private void updateStatus() {
        List<Playlist> playlists = app.getPlaylists();
        int totalPlaylists = playlists.size();
        int totalVideos = 0;
        int totalDownloaded = 0;

        for (Playlist playlist : playlists) {
            SyncService.PlaylistStats stats = app.getPlaylistStats(playlist.getId());
            totalVideos += stats.totalVideos;
            totalDownloaded += stats.downloaded;
        }

        int pending = totalVideos - totalDownloaded;

        String autoSyncStatus = app.isAutoSyncRunning() ? "ATIVA" : "DESATIVADA";
        String statusColor = app.isAutoSyncRunning() ? "green" : "red";

        String statusText = String.format(
            "<html><b>Playlists:</b> %d | <b>Vídeos:</b> %d | <b>Baixados:</b> %d | <b>Pendentes:</b> %d | " +
            "<b>Sinc. Automática:</b> <font color='%s'>%s</font></html>",
            totalPlaylists, totalVideos, totalDownloaded, pending, statusColor, autoSyncStatus
        );

        statusLabel.setText(statusText);
    }

    private void updateTable() {
        int selectedRow = playlistTable.getSelectedRow();
        String selectedId = null;

        if (selectedRow != -1 && tableModel.getRowCount() > selectedRow) {
            selectedId = getPlaylistIdAtRow(selectedRow);
        }

        tableModel.setRowCount(0);

        List<Playlist> playlists = app.getPlaylists();
        int newSelectedRow = -1;

        for (int i = 0; i < playlists.size(); i++) {
            Playlist playlist = playlists.get(i);
            SyncService.PlaylistStats stats = app.getPlaylistStats(playlist.getId());

            String lastSync = playlist.getLastSyncedAt() != null
                ? playlist.getLastSyncedAt().toString().substring(0, 19).replace("T", " ")
                : "Nunca";

            tableModel.addRow(new Object[]{
                playlist.getTitle(),
                stats.totalVideos,
                stats.downloaded,
                stats.pending,
                lastSync
            });

            if (playlist.getId().equals(selectedId)) {
                newSelectedRow = i;
            }
        }

        if (newSelectedRow != -1) {
            playlistTable.setRowSelectionInterval(newSelectedRow, newSelectedRow);
        }
    }

    private void updateButtons() {
        autoSyncButton.setText(app.isAutoSyncRunning() ? "Parar Sinc. Automática" : "Iniciar Sinc. Automática");
    }

    private void addPlaylist() {
        String url = JOptionPane.showInputDialog(this,
                "Digite a URL da playlist do YouTube:",
                "Adicionar Playlist",
                JOptionPane.PLAIN_MESSAGE);

        if (url != null && !url.trim().isEmpty()) {
            new Thread(() -> {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    Playlist playlist = app.addPlaylist(url.trim());

                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());

                        int result = JOptionPane.showConfirmDialog(this,
                                "Playlist adicionada: " + playlist.getTitle() + "\n\nDeseja sincronizar agora?",
                                "Sincronizar?",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);

                        if (result == JOptionPane.YES_OPTION) {
                            syncPlaylist(playlist.getId());
                        }
                    });
                } catch (PrivatePlaylistException ex) {
                    // Erro específico de playlist privada
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        String message = "❌ PLAYLIST PRIVADA DETECTADA\n\n" +
                                ex.getMessage() + "\n\n" +
                                "Quantidade de músicas: " + (ex.getVideoCount() > 0 ? ex.getVideoCount() : "Desconhecida") + "\n\n" +
                                "Para acessar playlists privadas:\n" +
                                "1. Vá em Configurações\n" +
                                "2. Marque 'Habilitar acesso a playlists privadas'\n" +
                                "3. Selecione seu navegador (deve estar logado no YouTube)\n" +
                                "4. Tente adicionar a playlist novamente";

                        JOptionPane.showMessageDialog(this,
                                message,
                                "Playlist Privada",
                                JOptionPane.WARNING_MESSAGE);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        JOptionPane.showMessageDialog(this,
                                "Erro ao adicionar playlist: " + ex.getMessage(),
                                "Erro",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        }
    }

    private void removePlaylist() {
        int selectedRow = playlistTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma playlist para remover",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String playlistId = getPlaylistIdAtRow(selectedRow);
        String playlistTitle = (String) tableModel.getValueAt(selectedRow, 0);

        int result = JOptionPane.showConfirmDialog(this,
                "Deseja realmente remover a playlist:\n" + playlistTitle + "?",
                "Confirmar remoção",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            app.removePlaylist(playlistId);
        }
    }

    private void syncSelected() {
        int selectedRow = playlistTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma playlist para sincronizar",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String playlistId = getPlaylistIdAtRow(selectedRow);
        syncPlaylist(playlistId);
    }

    private void syncPlaylist(String playlistId) {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                syncButton.setEnabled(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            });

            app.syncPlaylist(playlistId);

            SwingUtilities.invokeLater(() -> {
                syncButton.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this,
                        "Sincronização concluída!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            });
        }).start();
    }

    private void syncAll() {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                syncButton.setEnabled(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            });

            app.triggerSyncNow();

            SwingUtilities.invokeLater(() -> {
                syncButton.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this,
                        "Sincronização concluída!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            });
        }).start();
    }

    private void toggleAutoSync() {
        if (app.isAutoSyncRunning()) {
            app.stopAutoSync();
        } else {
            app.startAutoSync();
        }
    }

    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog(this, app);
        dialog.setVisible(true);
    }

    private String getPlaylistIdAtRow(int row) {
        String title = (String) tableModel.getValueAt(row, 0);
        List<Playlist> playlists = app.getPlaylists();

        for (Playlist playlist : playlists) {
            if (playlist.getTitle().equals(title)) {
                return playlist.getId();
            }
        }

        return null;
    }

    public void showWindow() {
        setVisible(true);
        toFront();
        requestFocus();
    }

    public void hideWindow() {
        setVisible(false);
    }

    @Override
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        super.dispose();
    }
}

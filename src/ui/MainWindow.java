package ui;

import application.Application;
import domain.Playlist;
import service.SyncService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame {

    private final Application app;
    private JTable playlistTable;
    private DefaultTableModel tableModel;
    private JButton syncButton;
    private JButton autoSyncButton;
    private JButton addButton;
    private JButton removeButton;
    private Timer updateTimer;

    // Stat card value labels
    private JLabel playlistsCountLabel;
    private JLabel videosCountLabel;
    private JLabel downloadedCountLabel;
    private JLabel pendingCountLabel;
    private JLabel autoSyncStatusLabel;

    public MainWindow(Application app) {
        this.app = app;
        initUI();
        setupListeners();
        startUpdateTimer();
        updateUI();
    }

    private void initUI() {
        setTitle("YT Music Sync");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(960, 680);
        setLocationRelativeTo(null);
        setBackground(MaterialTheme.BACKGROUND);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(MaterialTheme.BACKGROUND);
        setContentPane(root);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMain(),   BorderLayout.CENTER);
        root.add(createToolbar(), BorderLayout.SOUTH);
    }

    // -------------------------------------------------------------------------
    // Layout builders
    // -------------------------------------------------------------------------

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MaterialTheme.PRIMARY);
        header.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titleLabel = new JLabel("YT Music Sync");
        titleLabel.setFont(MaterialTheme.titleLarge());
        titleLabel.setForeground(MaterialTheme.ON_PRIMARY);

        JLabel subtitleLabel = new JLabel("Gerenciador de Playlists do YouTube");
        subtitleLabel.setFont(MaterialTheme.bodyMedium());
        subtitleLabel.setForeground(new Color(0xE8DEF8));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(subtitleLabel);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createMain() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(MaterialTheme.BACKGROUND);
        main.setBorder(new EmptyBorder(20, 20, 16, 20));

        main.add(createStatsRow(),    BorderLayout.NORTH);
        main.add(createPlaylistCard(), BorderLayout.CENTER);
        return main;
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setOpaque(false);

        playlistsCountLabel  = new JLabel("0");
        videosCountLabel     = new JLabel("0");
        downloadedCountLabel = new JLabel("0");
        pendingCountLabel    = new JLabel("0");
        autoSyncStatusLabel  = new JLabel("Desativada");

        row.add(statCard("Playlists",         playlistsCountLabel,
                MaterialTheme.PRIMARY_CONTAINER,    MaterialTheme.ON_PRIMARY_CONTAINER));
        row.add(statCard("Total de Vídeos",   videosCountLabel,
                MaterialTheme.SURFACE_CONTAINER,    MaterialTheme.ON_SURFACE));
        row.add(statCard("Baixados",          downloadedCountLabel,
                MaterialTheme.SUCCESS_CONTAINER,    MaterialTheme.ON_SUCCESS_CONTAINER));
        row.add(statCard("Pendentes",         pendingCountLabel,
                MaterialTheme.SECONDARY_CONTAINER,  MaterialTheme.ON_SECONDARY_CONTAINER));
        row.add(statCard("Sinc. Automática",  autoSyncStatusLabel,
                MaterialTheme.SURFACE_CONTAINER,    MaterialTheme.ON_SURFACE));

        return row;
    }

    private JPanel statCard(String title, JLabel valueLabel, Color bg, Color fg) {
        JPanel card = MaterialTheme.card(bg);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(MaterialTheme.labelMedium());
        titleLabel.setForeground(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 180));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(MaterialTheme.titleMedium());
        valueLabel.setForeground(fg);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLabel);
        return card;
    }

    private JPanel createPlaylistCard() {
        JPanel card = MaterialTheme.card(MaterialTheme.SURFACE_CONTAINER_LOW);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel sectionLabel = new JLabel("Playlists");
        sectionLabel.setFont(MaterialTheme.titleSmall());
        sectionLabel.setForeground(MaterialTheme.ON_SURFACE_VARIANT);
        sectionLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
        card.add(sectionLabel, BorderLayout.NORTH);

        String[] cols = {"Título", "Vídeos", "Baixados", "Pendentes", "Última Sincronização"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        playlistTable = new JTable(tableModel);
        playlistTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlistTable.setRowHeight(42);
        playlistTable.setShowGrid(false);
        playlistTable.setIntercellSpacing(new Dimension(0, 0));
        playlistTable.setBackground(MaterialTheme.SURFACE_CONTAINER_LOW);
        playlistTable.setForeground(MaterialTheme.ON_SURFACE);
        playlistTable.setSelectionBackground(MaterialTheme.PRIMARY_CONTAINER);
        playlistTable.setSelectionForeground(MaterialTheme.ON_PRIMARY_CONTAINER);
        playlistTable.setFont(MaterialTheme.bodyMedium());

        playlistTable.getColumnModel().getColumn(0).setPreferredWidth(340);
        playlistTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        playlistTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        playlistTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        playlistTable.getColumnModel().getColumn(4).setPreferredWidth(170);

        // Center-align numeric and date columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i <= 4; i++) {
            playlistTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader tableHeader = playlistTable.getTableHeader();
        tableHeader.setBackground(MaterialTheme.SURFACE_CONTAINER);
        tableHeader.setForeground(MaterialTheme.ON_SURFACE_VARIANT);
        tableHeader.setFont(MaterialTheme.labelMedium());
        tableHeader.setPreferredSize(new Dimension(0, 36));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, MaterialTheme.OUTLINE_VARIANT));

        JScrollPane scrollPane = new JScrollPane(playlistTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(MaterialTheme.OUTLINE_VARIANT, 1, true));
        scrollPane.getViewport().setBackground(MaterialTheme.SURFACE_CONTAINER_LOW);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(MaterialTheme.SURFACE_CONTAINER);
        toolbar.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Left side — playlist management
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        addButton = MaterialTheme.filledTonalButton("+ Adicionar");
        addButton.addActionListener(e -> addPlaylist());

        removeButton = MaterialTheme.outlinedButton("Remover");
        removeButton.addActionListener(e -> removePlaylist());
        removeButton.setEnabled(false);

        JButton syncSelectedButton = MaterialTheme.textButton("Sinc. Selecionada");
        syncSelectedButton.addActionListener(e -> syncSelected());

        left.add(addButton);
        left.add(removeButton);
        left.add(syncSelectedButton);

        // Right side — global actions
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton settingsButton = MaterialTheme.textButton("Configuracoes");
        settingsButton.addActionListener(e -> openSettings());

        autoSyncButton = MaterialTheme.filledTonalButton("Iniciar Sinc. Auto");
        autoSyncButton.addActionListener(e -> toggleAutoSync());

        syncButton = MaterialTheme.filledButton("Sincronizar Todas");
        syncButton.addActionListener(e -> syncAll());

        right.add(settingsButton);
        right.add(autoSyncButton);
        right.add(syncButton);

        toolbar.add(left,  BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);
        return toolbar;
    }

    // -------------------------------------------------------------------------
    // Listeners & timers
    // -------------------------------------------------------------------------

    private void setupListeners() {
        playlistTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                removeButton.setEnabled(playlistTable.getSelectedRow() != -1);
            }
        });

        app.addPropertyChangeListener(evt -> SwingUtilities.invokeLater(this::updateUI));
    }

    private void startUpdateTimer() {
        updateTimer = new Timer(3000, e -> updateUI());
        updateTimer.start();
    }

    // -------------------------------------------------------------------------
    // UI update
    // -------------------------------------------------------------------------

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            updateStats();
            updateTable();
            updateButtons();
        });
    }

    private void updateStats() {
        List<Playlist> playlists = app.getPlaylists();
        int totalVideos = 0, totalDownloaded = 0;

        for (Playlist playlist : playlists) {
            SyncService.PlaylistStats stats = app.getPlaylistStats(playlist.getId());
            totalVideos     += stats.totalVideos;
            totalDownloaded += stats.downloaded;
        }

        playlistsCountLabel.setText(String.valueOf(playlists.size()));
        videosCountLabel.setText(String.valueOf(totalVideos));
        downloadedCountLabel.setText(String.valueOf(totalDownloaded));
        pendingCountLabel.setText(String.valueOf(totalVideos - totalDownloaded));
        autoSyncStatusLabel.setText(app.isAutoSyncRunning() ? "Ativa" : "Desativada");
    }

    private void updateTable() {
        int selectedRow = playlistTable.getSelectedRow();
        String selectedId = (selectedRow != -1 && tableModel.getRowCount() > selectedRow)
                ? getPlaylistIdAtRow(selectedRow) : null;

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
        boolean running = app.isAutoSyncRunning();
        autoSyncButton.setText(running ? "Parar Sinc. Auto" : "Iniciar Sinc. Auto");
        autoSyncButton.setBackground(running
                ? MaterialTheme.TERTIARY_CONTAINER
                : MaterialTheme.SECONDARY_CONTAINER);
        autoSyncButton.setForeground(running
                ? MaterialTheme.ON_TERTIARY_CONTAINER
                : MaterialTheme.ON_SECONDARY_CONTAINER);
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

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
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        JOptionPane.showMessageDialog(this,
                                "Erro ao adicionar playlist: " + ex.getMessage(),
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        }
    }

    private void removePlaylist() {
        int selectedRow = playlistTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma playlist para remover", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String playlistId    = getPlaylistIdAtRow(selectedRow);
        String playlistTitle = (String) tableModel.getValueAt(selectedRow, 0);

        int result = JOptionPane.showConfirmDialog(this,
                "Deseja realmente remover a playlist:\n" + playlistTitle + "?",
                "Confirmar remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            app.removePlaylist(playlistId);
        }
    }

    private void syncSelected() {
        int selectedRow = playlistTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma playlist para sincronizar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        syncPlaylist(getPlaylistIdAtRow(selectedRow));
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
                        "Sincronizacao concluida!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
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
                        "Sincronizacao concluida!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
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
        for (Playlist playlist : app.getPlaylists()) {
            if (playlist.getTitle().equals(title)) {
                return playlist.getId();
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Window control
    // -------------------------------------------------------------------------

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
        if (updateTimer != null) updateTimer.stop();
        super.dispose();
    }
}

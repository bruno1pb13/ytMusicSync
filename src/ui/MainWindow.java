package ui;

import application.Application;
import domain.Playlist;
import domain.Video;
import service.SyncService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWindow extends JFrame {

    private final Application app;
    private JButton syncButton;
    private JButton autoSyncButton;
    private Timer updateTimer;

    // Stat card value labels
    private JLabel playlistsCountLabel;
    private JLabel videosCountLabel;
    private JLabel downloadedCountLabel;
    private JLabel pendingCountLabel;
    private JLabel autoSyncStatusLabel;

    // Collapsable playlists panel
    private JPanel playlistsContainer;
    private JScrollPane playlistsScrollPane;
    private final Map<String, Boolean> expandedState = new HashMap<>();

    // Sync progress
    private JPanel syncProgressPanel;
    private JProgressBar syncProgressBar;
    private JLabel syncProgressLabel;

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
        root.add(createFooter(), BorderLayout.SOUTH);
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

        main.add(createStatsRow(),         BorderLayout.NORTH);
        main.add(createPlaylistsSection(), BorderLayout.CENTER);
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
        row.add(statCard("Sinc. Automatica",  autoSyncStatusLabel,
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

    private JPanel createPlaylistsSection() {
        JPanel section = MaterialTheme.card(MaterialTheme.SURFACE_CONTAINER_LOW);
        section.setLayout(new BorderLayout(0, 10));
        section.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel sectionLabel = new JLabel("Playlists");
        sectionLabel.setFont(MaterialTheme.titleSmall());
        sectionLabel.setForeground(MaterialTheme.ON_SURFACE_VARIANT);
        sectionLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        section.add(sectionLabel, BorderLayout.NORTH);

        playlistsContainer = new JPanel();
        playlistsContainer.setLayout(new BoxLayout(playlistsContainer, BoxLayout.Y_AXIS));
        playlistsContainer.setBackground(MaterialTheme.SURFACE_CONTAINER_LOW);

        // Wrapper com BorderLayout faz o container preencher a largura do viewport
        JPanel viewportWrapper = new JPanel(new BorderLayout());
        viewportWrapper.setBackground(MaterialTheme.SURFACE_CONTAINER_LOW);
        viewportWrapper.add(playlistsContainer, BorderLayout.NORTH);

        playlistsScrollPane = new JScrollPane(viewportWrapper);
        playlistsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        playlistsScrollPane.getViewport().setBackground(MaterialTheme.SURFACE_CONTAINER_LOW);
        playlistsScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        section.add(playlistsScrollPane, BorderLayout.CENTER);
        return section;
    }

    private JPanel buildCollapsableCard(Playlist playlist, SyncService.PlaylistStats stats, List<Video> videos) {
        boolean expanded = expandedState.getOrDefault(playlist.getId(), false);

        JPanel outer = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(MaterialTheme.SURFACE_CONTAINER);
        outer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, MaterialTheme.OUTLINE_VARIANT));
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(MaterialTheme.SURFACE_CONTAINER);
        header.setBorder(new EmptyBorder(12, 14, 12, 14));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JButton toggleBtn = new JButton(expanded ? "▼" : "▶");
        toggleBtn.setFont(MaterialTheme.labelMedium());
        toggleBtn.setForeground(MaterialTheme.ON_SURFACE_VARIANT);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.setPreferredSize(new Dimension(28, 28));

        JLabel titleLabel = new JLabel(playlist.getTitle());
        titleLabel.setFont(MaterialTheme.bodyMedium().deriveFont(Font.BOLD));
        titleLabel.setForeground(MaterialTheme.ON_SURFACE);

        String lastSync = playlist.getLastSyncedAt() != null
                ? playlist.getLastSyncedAt().toString().substring(0, 19).replace("T", " ")
                : "Nunca";
        JLabel statsLabel = new JLabel(
                stats.totalVideos + " videos  |  " + stats.downloaded + " baixados  |  " +
                stats.pending + " pendentes  |  Ultima sinc: " + lastSync);
        statsLabel.setFont(MaterialTheme.labelMedium());
        statsLabel.setForeground(MaterialTheme.ON_SURFACE_VARIANT);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(statsLabel);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionPanel.setOpaque(false);

        JButton syncBtn = MaterialTheme.filledTonalButton("Sincronizar");
        syncBtn.setFont(MaterialTheme.labelMedium());
        syncBtn.addActionListener(e -> syncPlaylist(playlist.getId()));

        JButton removeBtn = MaterialTheme.outlinedButton("Remover");
        removeBtn.setFont(MaterialTheme.labelMedium());
        removeBtn.addActionListener(e -> removePlaylist(playlist.getId(), playlist.getTitle()));

        actionPanel.add(syncBtn);
        actionPanel.add(removeBtn);

        header.add(toggleBtn, BorderLayout.WEST);
        header.add(titlePanel, BorderLayout.CENTER);
        header.add(actionPanel, BorderLayout.EAST);

        // --- Body (video list) ---
        JPanel body = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, MaterialTheme.OUTLINE_VARIANT));
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.setVisible(expanded);

        if (videos.isEmpty()) {
            JLabel emptyLabel = new JLabel("  Nenhum video cadastrado nesta playlist.");
            emptyLabel.setFont(MaterialTheme.bodyMedium());
            emptyLabel.setForeground(MaterialTheme.ON_SURFACE_VARIANT);
            emptyLabel.setBorder(new EmptyBorder(12, 14, 12, 14));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(emptyLabel);
        } else {
            for (Video video : videos) {
                body.add(buildVideoRow(video));
            }
        }

        // Toggle action
        toggleBtn.addActionListener(e -> {
            boolean nowExpanded = !body.isVisible();
            expandedState.put(playlist.getId(), nowExpanded);
            body.setVisible(nowExpanded);
            toggleBtn.setText(nowExpanded ? "▼" : "▶");
            outer.revalidate();
            outer.repaint();
            playlistsContainer.revalidate();
            playlistsScrollPane.revalidate();
            playlistsScrollPane.repaint();
        });

        outer.add(header);
        outer.add(body);
        return outer;
    }

    private JPanel buildVideoRow(Video video) {
        JPanel row = new JPanel(new BorderLayout(10, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(
                        MaterialTheme.OUTLINE_VARIANT.getRed(),
                        MaterialTheme.OUTLINE_VARIANT.getGreen(),
                        MaterialTheme.OUTLINE_VARIANT.getBlue(), 80)),
                new EmptyBorder(8, 20, 8, 14)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean downloading = video.getId().equals(app.getDownloadingVideoId());
        String errorMsg     = app.getVideoError(video.getId());
        boolean downloaded  = video.isDownloaded();

        String icon;
        Color iconColor, titleColor, detailColor;
        String detail;

        if (downloading) {
            icon        = "\u23F3";  // ⏳
            iconColor   = MaterialTheme.PRIMARY;
            titleColor  = MaterialTheme.ON_SURFACE;
            detail      = "Baixando...";
            detailColor = MaterialTheme.PRIMARY;
        } else if (errorMsg != null) {
            icon        = "\u2717";  // ✗
            iconColor   = MaterialTheme.ON_ERROR_CONTAINER;
            titleColor  = MaterialTheme.ON_SURFACE_VARIANT;
            detail      = "Erro: " + errorMsg;
            detailColor = MaterialTheme.ON_ERROR_CONTAINER;
        } else if (downloaded) {
            icon        = "\u2713";  // ✓
            iconColor   = MaterialTheme.ON_SUCCESS_CONTAINER;
            titleColor  = MaterialTheme.ON_SURFACE;
            detail      = video.getDownloadedAt() != null
                    ? video.getDownloadedAt().toString().substring(0, 10)
                    : "Baixado";
            detailColor = MaterialTheme.ON_SURFACE_VARIANT;
        } else {
            icon        = "\u25CB";  // ○
            iconColor   = MaterialTheme.ON_SURFACE_VARIANT;
            titleColor  = MaterialTheme.ON_SURFACE_VARIANT;
            detail      = "Pendente";
            detailColor = MaterialTheme.ON_SURFACE_VARIANT;
        }

        JLabel statusIcon = new JLabel(icon);
        statusIcon.setFont(MaterialTheme.bodyMedium());
        statusIcon.setForeground(iconColor);
        statusIcon.setPreferredSize(new Dimension(18, 18));

        JLabel titleLabel = new JLabel(video.getTitle());
        titleLabel.setFont(MaterialTheme.bodyMedium());
        titleLabel.setForeground(titleColor);

        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(MaterialTheme.labelMedium());
        detailLabel.setForeground(detailColor);

        row.add(statusIcon, BorderLayout.WEST);
        row.add(titleLabel, BorderLayout.CENTER);
        row.add(detailLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(MaterialTheme.SURFACE_CONTAINER);

        syncProgressPanel = createSyncProgressPanel();
        syncProgressPanel.setVisible(false);
        footer.add(syncProgressPanel, BorderLayout.NORTH);
        footer.add(createToolbar(), BorderLayout.CENTER);
        return footer;
    }

    private JPanel createSyncProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(MaterialTheme.PRIMARY_CONTAINER);
        panel.setBorder(new EmptyBorder(8, 20, 8, 20));

        syncProgressLabel = new JLabel("Sincronizando...");
        syncProgressLabel.setFont(MaterialTheme.bodyMedium());
        syncProgressLabel.setForeground(MaterialTheme.ON_PRIMARY_CONTAINER);

        syncProgressBar = new JProgressBar(0, 100);
        syncProgressBar.setIndeterminate(true);
        syncProgressBar.setPreferredSize(new Dimension(200, 10));
        syncProgressBar.setBackground(MaterialTheme.PRIMARY_CONTAINER);
        syncProgressBar.setForeground(MaterialTheme.PRIMARY);

        panel.add(syncProgressLabel, BorderLayout.CENTER);
        panel.add(syncProgressBar, BorderLayout.EAST);
        return panel;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(MaterialTheme.SURFACE_CONTAINER);
        toolbar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JButton addButton = MaterialTheme.filledTonalButton("+ Adicionar");
        addButton.addActionListener(e -> addPlaylist());
        left.add(addButton);

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
        app.addPropertyChangeListener(evt -> SwingUtilities.invokeLater(() -> {
            updateUI();
            String prop = evt.getPropertyName();
            if ("syncProgress".equals(prop) || "syncInProgress".equals(prop)) {
                updateSyncProgress();
            }
        }));
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
            updatePlaylistCards();
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

    private void updatePlaylistCards() {
        int scrollPos = playlistsScrollPane.getVerticalScrollBar().getValue();

        playlistsContainer.removeAll();
        List<Playlist> playlists = app.getPlaylists();

        if (playlists.isEmpty()) {
            JLabel emptyLabel = new JLabel("Nenhuma playlist cadastrada. Clique em '+ Adicionar' para comecar.");
            emptyLabel.setFont(MaterialTheme.bodyMedium());
            emptyLabel.setForeground(MaterialTheme.ON_SURFACE_VARIANT);
            emptyLabel.setBorder(new EmptyBorder(24, 20, 24, 20));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            playlistsContainer.add(emptyLabel);
        } else {
            for (Playlist playlist : playlists) {
                SyncService.PlaylistStats stats = app.getPlaylistStats(playlist.getId());
                List<Video> videos = app.getVideosByPlaylistId(playlist.getId());
                JPanel card = buildCollapsableCard(playlist, stats, videos);
                playlistsContainer.add(card);
            }
        }

        playlistsContainer.add(Box.createVerticalGlue());
        playlistsContainer.revalidate();
        playlistsContainer.repaint();

        SwingUtilities.invokeLater(() ->
                playlistsScrollPane.getVerticalScrollBar().setValue(scrollPos));
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

    private void updateSyncProgress() {
        boolean syncing = app.isSyncInProgress();
        syncProgressPanel.setVisible(syncing);

        if (syncing) {
            String currentVideo = app.getSyncCurrentVideo();
            int current = app.getSyncDownloadCurrent();
            int total = app.getSyncDownloadTotal();

            if (total > 0) {
                syncProgressBar.setIndeterminate(false);
                syncProgressBar.setMaximum(total);
                syncProgressBar.setValue(current);
                syncProgressLabel.setText("Baixando " + current + "/" + total + ":  " +
                        (currentVideo != null ? currentVideo : ""));
            } else {
                syncProgressBar.setIndeterminate(true);
                syncProgressLabel.setText(currentVideo != null ? currentVideo : "Sincronizando...");
            }
        }

        revalidate();
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

    private void removePlaylist(String playlistId, String playlistTitle) {
        int result = JOptionPane.showConfirmDialog(this,
                "Deseja realmente remover a playlist:\n" + playlistTitle + "?",
                "Confirmar remocao", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            app.removePlaylist(playlistId);
            updateUI();
        }
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

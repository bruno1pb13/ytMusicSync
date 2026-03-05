package ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Material You design tokens and component factories for the YT Music Sync UI.
 */
public class MaterialTheme {

    // --- Color tokens (Material You Light scheme - Purple seed) ---
    public static final Color PRIMARY               = new Color(0x6750A4);
    public static final Color ON_PRIMARY            = new Color(0xFFFFFF);
    public static final Color PRIMARY_CONTAINER     = new Color(0xEADDFF);
    public static final Color ON_PRIMARY_CONTAINER  = new Color(0x21005D);

    public static final Color SECONDARY_CONTAINER     = new Color(0xE8DEF8);
    public static final Color ON_SECONDARY_CONTAINER  = new Color(0x1D192B);

    public static final Color TERTIARY_CONTAINER     = new Color(0xFFD8E4);
    public static final Color ON_TERTIARY_CONTAINER  = new Color(0x31111D);

    public static final Color ERROR_CONTAINER     = new Color(0xF9DEDC);
    public static final Color ON_ERROR_CONTAINER  = new Color(0x410E0B);

    public static final Color BACKGROUND              = new Color(0xFFFBFE);
    public static final Color SURFACE                 = new Color(0xFEF7FF);
    public static final Color SURFACE_CONTAINER_LOW  = new Color(0xF7F2FA);
    public static final Color SURFACE_CONTAINER       = new Color(0xF3EDF7);
    public static final Color SURFACE_CONTAINER_HIGH  = new Color(0xECE6F0);
    public static final Color SURFACE_VARIANT         = new Color(0xE7E0EC);

    public static final Color ON_SURFACE         = new Color(0x1D1B20);
    public static final Color ON_SURFACE_VARIANT = new Color(0x49454F);
    public static final Color OUTLINE            = new Color(0x79747E);
    public static final Color OUTLINE_VARIANT    = new Color(0xCAC4D0);

    public static final Color SUCCESS_CONTAINER     = new Color(0xC8F5D0);
    public static final Color ON_SUCCESS_CONTAINER  = new Color(0x002111);

    // --- Typography ---
    public static Font titleLarge()  { return new Font("Dialog", Font.BOLD,  22); }
    public static Font titleMedium() { return new Font("Dialog", Font.BOLD,  16); }
    public static Font titleSmall()  { return new Font("Dialog", Font.BOLD,  14); }
    public static Font bodyLarge()   { return new Font("Dialog", Font.PLAIN, 16); }
    public static Font bodyMedium()  { return new Font("Dialog", Font.PLAIN, 14); }
    public static Font bodySmall()   { return new Font("Dialog", Font.PLAIN, 12); }
    public static Font labelLarge()  { return new Font("Dialog", Font.BOLD,  14); }
    public static Font labelMedium() { return new Font("Dialog", Font.PLAIN, 12); }

    /**
     * Applies Material You tweaks on top of FlatLaf.
     * Must be called after FlatLightLaf.install().
     */
    public static void apply() {
        javax.swing.UIManager.put("Button.arc",           50);
        javax.swing.UIManager.put("Component.arc",        12);
        javax.swing.UIManager.put("TextComponent.arc",     8);
        javax.swing.UIManager.put("ScrollBar.thumbArc",  999);
        javax.swing.UIManager.put("ScrollBar.showButtons", false);
        javax.swing.UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

        javax.swing.UIManager.put("Table.alternateRowColor",    SURFACE_CONTAINER_LOW);
        javax.swing.UIManager.put("Table.selectionBackground",  PRIMARY_CONTAINER);
        javax.swing.UIManager.put("Table.selectionForeground",  ON_PRIMARY_CONTAINER);
        javax.swing.UIManager.put("TableHeader.background",     SURFACE_CONTAINER);

        javax.swing.UIManager.put("Panel.background",           BACKGROUND);
        javax.swing.UIManager.put("OptionPane.background",      BACKGROUND);
        javax.swing.UIManager.put("OptionPane.messageForeground", ON_SURFACE);
    }

    // --- Component factories ---

    /**
     * Rounded card panel that clips its children to rounded corners.
     */
    public static JPanel card(Color bg) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        panel.setBackground(bg);
        panel.setOpaque(false);
        return panel;
    }

    /** Material You filled button (primary). */
    public static JButton filledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(ON_PRIMARY);
        btn.setFont(labelLarge());
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        return btn;
    }

    /** Material You filled tonal button (secondary container). */
    public static JButton filledTonalButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(SECONDARY_CONTAINER);
        btn.setForeground(ON_SECONDARY_CONTAINER);
        btn.setFont(labelLarge());
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        return btn;
    }

    /** Material You outlined button. */
    public static JButton outlinedButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(PRIMARY);
        btn.setFont(labelLarge());
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(OUTLINE, 1, true),
            new EmptyBorder(9, 23, 9, 23)
        ));
        return btn;
    }

    /** Material You text button. */
    public static JButton textButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(PRIMARY);
        btn.setFont(labelLarge());
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(10, 12, 10, 12));
        return btn;
    }
}

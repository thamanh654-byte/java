package com.store.util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Bộ style dùng chung cho toàn bộ giao diện Swing.
 * Mọi Form dùng cùng font, màu và kiểu bảng để giao diện đồng nhất, dễ trình bày.
 */
public final class UiUtils {

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_PLAIN = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONEY = new Font("Segoe UI", Font.BOLD, 18);

    public static final Color PRIMARY = new Color(30, 90, 168);
    public static final Color PRIMARY_DARK = new Color(20, 62, 120);
    public static final Color ACCENT = new Color(16, 150, 96);
    public static final Color DANGER = new Color(196, 52, 52);
    public static final Color WARNING = new Color(184, 118, 14);
    public static final Color NEUTRAL = new Color(90, 98, 110);

    public static final Color BG = new Color(244, 246, 249);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(214, 220, 228);
    public static final Color TEXT = new Color(33, 37, 43);
    public static final Color TEXT_MUTED = new Color(110, 118, 129);
    public static final Color HEADER_BG = new Color(235, 240, 247);
    public static final Color ROW_ALT = new Color(248, 250, 253);
    public static final Color LOW_STOCK_BG = new Color(255, 236, 236);
    public static final Color LOW_STOCK_FG = new Color(168, 32, 32);

    /** Ngưỡng cảnh báo sắp hết hàng. */
    public static final int LOW_STOCK_THRESHOLD = 5;

    private UiUtils() {
    }

    /** Nút màu theo vai trò (chữ trắng, nền đậm, con trỏ bàn tay). */
    public static JButton button(String text, Color background) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(background);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(TEXT);
        return lbl;
    }

    public static JLabel muted(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    /** Viền + tiêu đề cho từng khối nội dung. */
    public static Border cardBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER),
                "  " + title + "  ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                FONT_BOLD,
                PRIMARY);
    }

    /** Thanh tiêu đề xanh đậm nằm trên cùng mỗi cửa sổ. */
    public static JPanel header(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        if (subtitle != null && !subtitle.isBlank()) {
            JLabel lblSub = new JLabel(subtitle);
            lblSub.setFont(FONT_SUBTITLE);
            lblSub.setForeground(new Color(210, 225, 245));
            header.add(lblSub, BorderLayout.SOUTH);
        }
        return header;
    }

    /** Style chung cho mọi JTable: header đậm, dòng cao, không viền lưới. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_PLAIN);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(214, 230, 250));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setBackground(CARD);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(HEADER_BG);
        header.setForeground(PRIMARY_DARK);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 34));
    }

    public static void pad(JComponent component) {
        component.setFont(FONT_PLAIN);
    }

    /** Tô nền hồng các dòng có tồn kho thấp, xen kẽ màu các dòng còn lại. */
    public static void applyLowStockRenderer(JTable table, int quantityColumn) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    Object qty = t.getValueAt(row, quantityColumn);
                    boolean low = qty instanceof Integer q && q <= LOW_STOCK_THRESHOLD;
                    c.setBackground(low ? LOW_STOCK_BG : (row % 2 == 0 ? CARD : ROW_ALT));
                    c.setForeground(low ? LOW_STOCK_FG : TEXT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    /** Renderer bảng thường: xen kẽ màu dòng cho dễ đọc. */
    public static void applyStripeRenderer(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD : ROW_ALT);
                    c.setForeground(TEXT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.JTextComponent;

public final class Theme {
    public static final Color LIGHT_BACKGROUND = new Color(245, 247, 250);
    public static final Color LIGHT_PANEL = Color.WHITE;
    public static final Color LIGHT_HEADER = new Color(235, 239, 245);
    public static final Color LIGHT_INPUT = new Color(250, 251, 253);
    public static final Color LIGHT_TEXT = new Color(27, 35, 48);
    public static final Color LIGHT_MUTED = new Color(102, 113, 130);
    public static final Color LIGHT_BORDER = new Color(221, 226, 234);
    public static final Color LIGHT_SCROLL_TRACK = new Color(235, 239, 245);
    public static final Color LIGHT_SCROLL_THUMB = new Color(169, 180, 196);
    public static final Color LIGHT_DIVIDER = new Color(205, 213, 224);
    public static final Color LIGHT_SIDEBAR_BACKGROUND = new Color(238, 242, 247);
    public static final Color LIGHT_SIDEBAR_TEXT = new Color(42, 52, 68);
    public static final Color LIGHT_SIDEBAR_MUTED = new Color(96, 108, 126);
    public static final Color LIGHT_SIDEBAR_SELECTED = new Color(205, 224, 248);
    public static final Color LIGHT_SIDEBAR_SELECTED_TEXT = new Color(20, 67, 125);
    public static final Color DARK_BACKGROUND = new Color(30, 36, 47);
    public static final Color DARK_PANEL = new Color(41, 49, 63);
    public static final Color DARK_HEADER = new Color(48, 58, 74);
    public static final Color DARK_INPUT = new Color(51, 61, 77);
    public static final Color DARK_TEXT = new Color(230, 235, 243);
    public static final Color DARK_MUTED = new Color(173, 184, 201);
    public static final Color DARK_BORDER = new Color(74, 86, 105);
    public static final Color DARK_SCROLL_TRACK = new Color(34, 41, 54);
    public static final Color DARK_SCROLL_THUMB = new Color(91, 105, 127);
    public static final Color DARK_DIVIDER = new Color(24, 30, 40);
    public static final Color BUTTON = new Color(39, 93, 173);
    public static final Color BUTTON_HOVER = new Color(57, 112, 194);
    public static final Color BUTTON_PRESSED = new Color(28, 72, 137);
    public static final Color SIDEBAR_BUTTON = new Color(31, 41, 57);
    public static final Color SIDEBAR_BUTTON_HOVER = new Color(47, 60, 80);
    public static final Color SIDEBAR_BUTTON_PRESSED = new Color(18, 25, 37);
    public static final Color STOP_ICON_RED = new Color(203, 54, 54);
    public static final Color SIDEBAR_BACKGROUND = new Color(22, 29, 41);
    public static final Color SIDEBAR_TEXT = new Color(202, 211, 226);
    public static final Color SIDEBAR_BORDER = new Color(48, 58, 75);
    public static final Color SIDEBAR_MUTED = new Color(137, 148, 166);
    public static final Color SIDEBAR_SELECTED = new Color(39, 93, 173);

    private Theme() {}

    public static void apply(Component component, boolean dark) {
        Color background = dark ? DARK_PANEL : LIGHT_PANEL;
        Color page = dark ? DARK_BACKGROUND : LIGHT_BACKGROUND;
        Color header = dark ? DARK_HEADER : LIGHT_HEADER;
        Color text = dark ? DARK_TEXT : LIGHT_TEXT;
        Color border = dark ? DARK_BORDER : LIGHT_BORDER;
        boolean sidebar = isInSidebar(component);

        if (component instanceof AnimatedButton) {
            AnimatedButton button = (AnimatedButton) component;
            button.applyTheme(dark, sidebar, Boolean.TRUE.equals(button.getClientProperty("theme.selected")));
        } else if (component instanceof JLabel) {
            component.setForeground(sidebar ? (dark ? SIDEBAR_TEXT : LIGHT_SIDEBAR_TEXT) : text);
            if (((JLabel) component).isOpaque()) ((JLabel) component).setBackground(sidebar ? (dark ? SIDEBAR_BACKGROUND : LIGHT_SIDEBAR_BACKGROUND) : header);
        } else if (component instanceof JTextComponent) {
            component.setForeground(text);
            component.setBackground(dark ? DARK_INPUT : LIGHT_INPUT);
            ((JTextComponent) component).setCaretColor(text);
            ((JTextComponent) component).setSelectionColor(dark ? DARK_HEADER : new Color(200, 220, 245));
        } else if (component instanceof JComboBox<?>) {
            JComboBox<?> combo = (JComboBox<?>) component;
            Color input = dark ? DARK_INPUT : LIGHT_INPUT;
            Color selection = dark ? DARK_HEADER : new Color(220, 233, 250);
            combo.setForeground(text);
            combo.setBackground(input);
            combo.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, selected, cellHasFocus);
                    setForeground(text);
                    setBackground(selected ? selection : input);
                    return this;
                }
            });
        } else if (component instanceof JTable) {
            JTable table = (JTable) component;
            table.setForeground(text);
            table.setBackground(dark ? DARK_INPUT : LIGHT_INPUT);
            table.setGridColor(border);
            table.setSelectionForeground(text);
            table.setSelectionBackground(dark ? DARK_HEADER : new Color(220, 233, 250));
            table.getTableHeader().setForeground(text);
            table.getTableHeader().setBackground(header);
        } else if (component instanceof JScrollBar) {
            JScrollBar scrollBar = (JScrollBar) component;
            Color track = dark ? DARK_SCROLL_TRACK : LIGHT_SCROLL_TRACK;
            Color thumb = dark ? DARK_SCROLL_THUMB : LIGHT_SCROLL_THUMB;
            scrollBar.setBackground(track);
            scrollBar.setForeground(thumb);
            scrollBar.setUI(new BasicScrollBarUI() {
                @Override protected void configureScrollBarColors() {
                    thumbColor = thumb;
                    trackColor = track;
                    thumbHighlightColor = thumb.brighter();
                    thumbDarkShadowColor = thumb.darker();
                }
            });
        } else if (component instanceof JSplitPane) {
            component.setBackground(dark ? DARK_DIVIDER : LIGHT_DIVIDER);
            ((JSplitPane) component).setDividerSize(8);
        } else if (component instanceof JViewport) {
            component.setBackground(background);
        } else if (component instanceof JScrollPane) {
            component.setBackground(background);
            ((JScrollPane) component).getViewport().setBackground(background);
        } else if (component instanceof JPanel) {
            boolean pagePanel = component instanceof JComponent && Boolean.TRUE.equals(((JComponent) component).getClientProperty("theme.page"));
            component.setBackground(sidebar ? (dark ? SIDEBAR_BACKGROUND : LIGHT_SIDEBAR_BACKGROUND) : pagePanel ? page : background);
        } else if (component instanceof JButton) {
            component.setForeground(Color.WHITE);
            component.setBackground(sidebar ? SIDEBAR_BUTTON : BUTTON);
        } else if (component instanceof JComponent && ((JComponent) component).isOpaque()) {
            component.setBackground(background);
        }

        if (component instanceof JComponent) {
            JComponent swingComponent = (JComponent) component;
            Border currentBorder = swingComponent.getBorder();
            if (currentBorder instanceof TitledBorder) {
                TitledBorder titled = (TitledBorder) currentBorder;
                titled.setTitleColor(text);
                if (titled.getBorder() != null) titled.setBorder(themeBorder(titled.getBorder(), border));
            } else if (component instanceof JScrollPane) {
                swingComponent.setBorder(new LineBorder(border));
            } else if (currentBorder != null) {
                swingComponent.setBorder(themeBorder(currentBorder, border));
            }
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                apply(child, dark);
            }
        }
    }

    private static boolean isInSidebar(Component component) {
        for (Component current = component; current != null; current = current.getParent()) {
            if (current instanceof JComponent && Boolean.TRUE.equals(((JComponent) current).getClientProperty("theme.sidebar"))) return true;
        }
        return false;
    }

    private static Border themeBorder(Border border, Color color) {
        if (border instanceof TitledBorder) {
            TitledBorder titled = (TitledBorder) border;
            titled.setTitleColor(color);
            if (titled.getBorder() != null) titled.setBorder(themeBorder(titled.getBorder(), color));
            return titled;
        }
        if (border instanceof CompoundBorder) {
            CompoundBorder compound = (CompoundBorder) border;
            return new CompoundBorder(themeBorder(compound.getOutsideBorder(), color), themeBorder(compound.getInsideBorder(), color));
        }
        if (border instanceof LineBorder) {
            LineBorder line = (LineBorder) border;
            return new LineBorder(color, line.getThickness(), line.getRoundedCorners());
        }
        if (border instanceof MatteBorder) {
            Insets insets = border.getBorderInsets(null);
            return new MatteBorder(insets.top, insets.left, insets.bottom, insets.right, color);
        }
        return border;
    }
}

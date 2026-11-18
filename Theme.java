import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

public final class Theme {
    public static final Color LIGHT_BACKGROUND = new Color(245, 247, 250);
    public static final Color LIGHT_PANEL = Color.WHITE;
    public static final Color LIGHT_INPUT = new Color(250, 251, 253);
    public static final Color LIGHT_TEXT = new Color(27, 35, 48);
    public static final Color LIGHT_MUTED = new Color(102, 113, 130);
    public static final Color LIGHT_BORDER = new Color(221, 226, 234);
    public static final Color DARK_BACKGROUND = new Color(30, 36, 47);
    public static final Color DARK_PANEL = new Color(41, 49, 63);
    public static final Color DARK_INPUT = new Color(51, 61, 77);
    public static final Color DARK_TEXT = new Color(230, 235, 243);
    public static final Color DARK_MUTED = new Color(173, 184, 201);
    public static final Color DARK_BORDER = new Color(74, 86, 105);
    public static final Color BUTTON = new Color(39, 93, 173);
    public static final Color SIDEBAR_BACKGROUND = new Color(22, 29, 41);
    public static final Color SIDEBAR_TEXT = new Color(202, 211, 226);
    public static final Color SIDEBAR_BORDER = new Color(48, 58, 75);
    public static final Color SIDEBAR_MUTED = new Color(137, 148, 166);

    private Theme() {}

    public static void apply(Component component, boolean dark) {
        Color background = dark ? DARK_PANEL : LIGHT_PANEL;
        Color text = dark ? DARK_TEXT : LIGHT_TEXT;

        if (component instanceof JPanel || component instanceof JScrollPane) {
            component.setBackground(background);
        }
        if (component instanceof JLabel) {
            component.setForeground(text);
        } else if (component instanceof JTextComponent) {
            component.setForeground(text);
            component.setBackground(dark ? DARK_INPUT : LIGHT_INPUT);
            ((JTextComponent) component).setCaretColor(text);
        } else if (component instanceof JComboBox<?>) {
            component.setForeground(text);
            component.setBackground(dark ? DARK_INPUT : LIGHT_INPUT);
        } else if (component instanceof JButton) {
            component.setForeground(Color.WHITE);
            component.setBackground(BUTTON);
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                apply(child, dark);
            }
        }
    }
}

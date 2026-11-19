import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.Timer;

public final class AnimatedButton extends JButton {
    private static final int ANIMATION_MILLIS = 150;
    private Color normalColor = Theme.BUTTON;
    private Color hoverColor = Theme.BUTTON_HOVER;
    private Color pressedColor = Theme.BUTTON_PRESSED;
    private Color currentColor = normalColor;
    private Color startColor = normalColor;
    private Color targetColor = normalColor;
    private boolean darkTheme;
    private boolean hovered;
    private boolean pressed;
    private long animationStart;
    private Timer colorTimer;

    public AnimatedButton(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent event) {
                hovered = true;
                animateTo(pressed ? pressedColor : hoverColor);
            }
            @Override public void mouseExited(MouseEvent event) {
                hovered = false;
                if (!pressed) animateTo(normalColor);
            }
            @Override public void mousePressed(MouseEvent event) {
                if (!isEnabled()) return;
                pressed = true;
                animateTo(pressedColor);
            }
            @Override public void mouseReleased(MouseEvent event) {
                pressed = false;
                animateTo(hovered ? hoverColor : normalColor);
            }
        });
    }

    public void applyTheme(boolean dark, boolean sidebar) {
        applyTheme(dark, sidebar, false);
    }

    public void applyTheme(boolean dark, boolean sidebar, boolean selected) {
        darkTheme = dark;
        Color nextNormal = sidebar
            ? (selected ? (dark ? Theme.SIDEBAR_SELECTED : Theme.LIGHT_SIDEBAR_SELECTED) : (dark ? Theme.SIDEBAR_BUTTON : Theme.LIGHT_SIDEBAR_BACKGROUND))
            : Theme.BUTTON;
        Color nextHover = sidebar ? Theme.SIDEBAR_BUTTON_HOVER : Theme.BUTTON_HOVER;
        Color nextPressed = sidebar ? Theme.SIDEBAR_BUTTON_PRESSED : Theme.BUTTON_PRESSED;
        boolean paletteChanged = !nextNormal.equals(normalColor) || !nextHover.equals(hoverColor) || !nextPressed.equals(pressedColor);
        normalColor = nextNormal;
        hoverColor = nextHover;
        pressedColor = nextPressed;
        setBackground(normalColor);
        setForeground(sidebar ? (selected && !dark ? Theme.LIGHT_SIDEBAR_SELECTED_TEXT : dark ? Theme.SIDEBAR_TEXT : Theme.LIGHT_SIDEBAR_TEXT) : Color.WHITE);
        if (paletteChanged || hovered || pressed) animateTo(pressed ? pressedColor : hovered ? hoverColor : normalColor);
        else {
            if (colorTimer != null) colorTimer.stop();
            colorTimer = null;
            currentColor = normalColor;
            targetColor = normalColor;
            repaint();
        }
    }

    private void animateTo(Color color) {
        if (colorTimer != null) colorTimer.stop();
        startColor = currentColor;
        targetColor = color;
        animationStart = System.nanoTime();
        colorTimer = new Timer(15, event -> {
            float progress = Math.min(1f, (System.nanoTime() - animationStart) / (ANIMATION_MILLIS * 1_000_000f));
            currentColor = blend(startColor, targetColor, progress);
            repaint();
            if (progress >= 1f) {
                currentColor = targetColor;
                colorTimer.stop();
                colorTimer = null;
            }
        });
        colorTimer.start();
    }

    private Color blend(Color from, Color to, float amount) {
        float inverse = 1f - amount;
        return new Color(
            Math.round(from.getRed() * inverse + to.getRed() * amount),
            Math.round(from.getGreen() * inverse + to.getGreen() * amount),
            Math.round(from.getBlue() * inverse + to.getBlue() * amount));
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D painter = (Graphics2D) graphics.create();
        painter.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color background = isEnabled() ? currentColor : blend(normalColor, darkTheme ? Theme.DARK_PANEL : Theme.LIGHT_PANEL, 0.55f);
        if (pressed && isEnabled()) {
            painter.translate(getWidth() * 0.015, getHeight() * 0.015);
            painter.scale(0.97, 0.97);
        }
        painter.setColor(background);
        painter.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        super.paintComponent(painter);
        painter.dispose();
    }

    public static final class PausePlayIcon implements Icon {
        private float pauseOpacity = 1f;
        private float startOpacity = 1f;
        private float targetOpacity = 1f;
        private long transitionStart;
        private Timer transitionTimer;

        public void setRunning(boolean running, JButton button) {
            if (transitionTimer != null) transitionTimer.stop();
            startOpacity = pauseOpacity;
            targetOpacity = running ? 1f : 0f;
            transitionStart = System.nanoTime();
            transitionTimer = new Timer(15, event -> {
                float progress = Math.min(1f, (System.nanoTime() - transitionStart) / 140_000_000f);
                pauseOpacity = startOpacity + (targetOpacity - startOpacity) * progress;
                button.repaint();
                if (progress >= 1f) {
                    pauseOpacity = targetOpacity;
                    transitionTimer.stop();
                    transitionTimer = null;
                }
            });
            transitionTimer.start();
        }

        @Override public int getIconWidth() { return 22; }
        @Override public int getIconHeight() { return 22; }

        @Override public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
            Graphics2D painter = (Graphics2D) graphics.create();
            painter.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            painter.setColor(Color.WHITE);
            float enabledOpacity = component.isEnabled() ? 1f : 0.42f;
            if (pauseOpacity > 0f) {
                painter.setComposite(AlphaComposite.SrcOver.derive(pauseOpacity * enabledOpacity));
                painter.fillRoundRect(x + 5, y + 4, 4, 14, 2, 2);
                painter.fillRoundRect(x + 13, y + 4, 4, 14, 2, 2);
            }
            if (pauseOpacity < 1f) {
                painter.setComposite(AlphaComposite.SrcOver.derive((1f - pauseOpacity) * enabledOpacity));
                Path2D triangle = new Path2D.Float();
                triangle.moveTo(x + 6, y + 3);
                triangle.lineTo(x + 18, y + 11);
                triangle.lineTo(x + 6, y + 19);
                triangle.closePath();
                painter.fill(triangle);
            }
            painter.dispose();
        }
    }

    public static final class StopIcon implements Icon {
        @Override public int getIconWidth() { return 22; }
        @Override public int getIconHeight() { return 22; }

        @Override public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
            Graphics2D painter = (Graphics2D) graphics.create();
            painter.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            painter.setComposite(AlphaComposite.SrcOver.derive(component.isEnabled() ? 1f : 0.42f));
            painter.setColor(Theme.STOP_ICON_RED);
            painter.fillOval(x + 2, y + 2, 18, 18);
            painter.setColor(Color.WHITE);
            painter.fillRoundRect(x + 8, y + 8, 6, 6, 1, 1);
            painter.dispose();
        }
    }
}

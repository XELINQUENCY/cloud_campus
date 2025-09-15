package gui.shop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class CustomUI {

    //自定义圆形标识类
    public static class CircleLabel extends JLabel {
        public CircleLabel(String text) {
            super(text);
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制圆形背景
            g2.setColor(getBackground());
            g2.fillOval(0, 0, getWidth(), getHeight());

            // 绘制文本
            super.paintComponent(g);
            g2.dispose();
        }
    }
    //自定义滚动条
    public static void customizeScrollBars(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(100, 100, 100, 150);
                this.trackColor = new Color(240, 240, 240);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton();
            }
            private JButton createInvisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) { //绘制圆角矩形滑块
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fill(new RoundRectangle2D.Double(
                        thumbBounds.x + 2,
                        thumbBounds.y + 2,
                        thumbBounds.width - 4,
                        thumbBounds.height - 4,
                        10, 10));
                g2.dispose();
            }
        });
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(14, 0));

        scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(100, 100, 100, 150);
                this.trackColor = new Color(240, 240, 240);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton();
            }
            private JButton createInvisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) { //绘制圆角矩形滑块
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fill(new RoundRectangle2D.Double(
                        thumbBounds.x + 2,
                        thumbBounds.y + 2,
                        thumbBounds.width - 4,
                        thumbBounds.height - 4,
                        10, 10));
                g2.dispose();
            }
        });
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
    }
    //自定义圆角按钮类
    public static class RoundedButton extends JButton {
        private Color backgroundColor;
        private Color hoverColor;
        private Color pressedColor;
        private Color borderColor;
        private int cornerRadius;
        private int borderThickness;

        public RoundedButton(String text, Color bgColor, Color hover,
                             Color pressed, Color border, int radius, int thickness) {
            super(text);
            backgroundColor = bgColor;
            hoverColor = hover;  //悬停时
            pressedColor = pressed;  //按下时
            borderColor = border;
            cornerRadius = radius;
            borderThickness = thickness;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setFont(getFont().deriveFont(Font.BOLD));
            setMargin(new Insets(thickness, thickness, thickness, thickness));
        }
        public void setBackgroundColor(Color color) { this.backgroundColor = color; }
        public void setHoverColor(Color color) { this.hoverColor = color; }
        public void setPressedColor(Color color) { this.pressedColor = color; }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) {
                g2.setColor(pressedColor);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(backgroundColor);
            }
            g2.fillRoundRect(borderThickness, borderThickness, getWidth() - borderThickness * 2,
                    getHeight() - borderThickness * 2, cornerRadius, cornerRadius);
            if (borderThickness > 0) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(borderThickness));
                g2.drawRoundRect(borderThickness / 2, borderThickness / 2, getWidth() - borderThickness,
                        getHeight() - borderThickness, cornerRadius, cornerRadius);
            }
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            Rectangle stringBounds = fm.getStringBounds(this.getText(), g2).getBounds();
            int textX = (getWidth() - stringBounds.width) / 2;
            int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();
            g2.drawString(getText(), textX, textY);
            g2.dispose();
        }
    }
}

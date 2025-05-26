import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.Timer;
import javax.sound.sampled.*;
import java.text.SimpleDateFormat;

public class TaskAutomation {
    private JFrame frame;
    private JComboBox<String> taskComboBox;
    private JButton startButton;
    private JButton stopButton;
    private JButton voiceButton;
    private JLabel statusLabel;
    private Robot robot;
    private boolean isRunning;
    private boolean isVoiceListening;
    private Map<String, String> voiceCommands;
    private ArrayList<Particle> particles;

    public TaskAutomation() {
        // Initialize Robot
        try {
            robot = new Robot();
        } catch (AWTException e) {
            JOptionPane.showMessageDialog(null, "Robot initialization failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        isRunning = false;
        isVoiceListening = false;

        // Initialize voice commands
        voiceCommands = new HashMap<>();
        voiceCommands.put("open notepad", "notepad");
        voiceCommands.put("open chrome", "chrome");
        voiceCommands.put("open explorer", "explorer");
        voiceCommands.put("open outlook", "outlook");

        // Initialize particles
        particles = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            particles.add(new Particle());
        }

        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set up the main frame
        frame = new JFrame("Task Automation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 450);
        frame.setMinimumSize(new Dimension(500, 450));
        frame.setUndecorated(true);
        frame.setShape(new RoundRectangle2D.Double(0, 0, 500, 450, 20, 20));
        frame.setLayout(new BorderLayout());

        // Background panel with radial gradient and particles
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                RadialGradientPaint gradient = new RadialGradientPaint(
                    getWidth() / 2f, getHeight() / 2f, getWidth() / 2f,
                    new float[]{0.0f, 1.0f},
                    new Color[]{new Color(0x4B0082), new Color(0x1C2526)}
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw particles
                for (Particle p : particles) {
                    g2d.fillOval((int) p.x, (int) p.y, 3, 3);
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setOpaque(false);

        // Glassmorphic card panel
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(new Color(0x00FFFF));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setMaximumSize(new Dimension(400, 350));

        // Title label
        JLabel titleLabel = new JLabel("Task Automation", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 28));
        titleLabel.setForeground(new Color(000000));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Task selection
        JPanel taskPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        taskPanel.setOpaque(false);
        JLabel taskLabel = new JLabel("Task:");
        taskLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        taskLabel.setForeground(new Color(0x00000));
        String[] tasks = {"Open Notepad", "Open Email Client", "Open Browser", "Open File Explorer"};
        taskComboBox = new JComboBox<>(tasks);
        taskComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskComboBox.setBackground(new Color(0x1C2526));
        taskComboBox.setForeground(new Color(0x00000));
        taskComboBox.setBorder(BorderFactory.createLineBorder(new Color(0x00FFFF), 2));
        taskComboBox.setPreferredSize(new Dimension(200, 30));
        taskPanel.add(taskLabel);
        taskPanel.add(taskComboBox);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);

        // Start button
        startButton = new GlowButton("Start Automation", new Color(0x00FFFF));
        startButton.addActionListener(e -> startAutomation());
        buttonPanel.add(startButton);

        // Stop button
        stopButton = new GlowButton("Stop", new Color(0xFF00FF));
        stopButton.addActionListener(e -> stopAutomation());
        buttonPanel.add(stopButton);

        // Voice button
        voiceButton = new GlowButton("Start Voice", new Color(0x00FFFF));
        voiceButton.addActionListener(e -> toggleVoiceControl());
        buttonPanel.add(voiceButton);

        // Status label
        statusLabel = new JLabel("Select a task or use voice", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        statusLabel.setForeground(new Color(0x00FFFF));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Assemble card
        cardPanel.add(titleLabel);
        cardPanel.add(taskPanel);
        cardPanel.add(buttonPanel);
        cardPanel.add(statusLabel);

        // Center card in background
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(cardPanel);

        backgroundPanel.add(centerPanel, BorderLayout.CENTER);
        frame.add(backgroundPanel, BorderLayout.CENTER);

        // Make frame draggable
        FrameDragger dragger = new FrameDragger(frame);
        frame.addMouseListener(dragger);
        frame.addMouseMotionListener(dragger);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.revalidate();
        frame.repaint();

        // Animate particles
        Timer particleTimer = new Timer();
        particleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Particle p : particles) {
                    p.update(frame.getWidth(), frame.getHeight());
                }
                backgroundPanel.repaint();
            }
        }, 0, 100);

        // Pulse status
        Timer pulseTimer = new Timer();
        pulseTimer.scheduleAtFixedRate(new TimerTask() {
            float opacity = 0.7f;
            boolean increasing = true;
            @Override
            public void run() {
                opacity += increasing ? 0.05f : -0.05f;
                if (opacity >= 1.0f) increasing = false;
                if (opacity <= 0.7f) increasing = true;
                int alpha = (int) (opacity * 255);
                statusLabel.setForeground(new Color(0x00, 0xFF, 0xFF, alpha));
            }
        }, 0, 100);
    }

    private void startAutomation() {
        if (isRunning) {
            showAnimatedStatus("Automation already running!", new Color(0xFF0000), true);
            playSound("error");
            return;
        }
        isRunning = true;
        String selectedTask = (String) taskComboBox.getSelectedItem();
        showAnimatedStatus("Starting " + selectedTask + "...", new Color(0x00FFFF), false);
        playSound("move");

        new Thread(() -> {
            try {
                switch (selectedTask) {
                    case "Open Notepad":
                        automateNotepad();
                        break;
                    case "Open Email Client":
                        automateEmail();
                        break;
                    case "Open Browser":
                        automateBrowser();
                        break;
                    case "Open File Explorer":
                        automateFileExplorer();
                        break;
                    default:
                        throw new IllegalStateException("Unknown task: " + selectedTask);
                }
                SwingUtilities.invokeLater(() -> {
                    showAnimatedStatus(selectedTask + " completed successfully!", new Color(0x00FF00), false);
                    playSound("win");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showAnimatedStatus("Error in " + selectedTask + ": " + e.getMessage(), new Color(0xFF0000), true);
                    playSound("error");
                });
            } finally {
                isRunning = false;
            }
        }).start();
    }

    private void automateNotepad() throws Exception {
        Runtime.getRuntime().exec("notepad");
        robot.delay(1000);
        String message = "Hello, this is an automated note!\nCreated on: " + getCurrentTimestamp();
        typeString(message);
        robot.delay(500);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_S);
        robot.keyRelease(KeyEvent.VK_S);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.delay(500);
        String filename = "Note_" + getCurrentTimestamp().replace(":", "").replace(" ", "_") + ".txt";
        typeString(filename);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.delay(500);
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_F4);
        robot.keyRelease(KeyEvent.VK_F4);
        robot.keyRelease(KeyEvent.VK_ALT);
    }

    private void automateEmail() throws Exception {
        Runtime.getRuntime().exec("cmd /c start mailto:example@domain.com?subject=Automated%20Email&body=This%20is%20an%20automated%20email%20sent%20on%20" + getCurrentTimestamp());
        robot.delay(2000);
    }

    private void automateBrowser() throws Exception {
        Runtime.getRuntime().exec("cmd /c start https://www.google.com");
        robot.delay(1000);
    }

    private void automateFileExplorer() throws Exception {
        Runtime.getRuntime().exec("explorer Documents");
        robot.delay(1000);
    }

    private void toggleVoiceControl() {
        if (isVoiceListening) {
            isVoiceListening = false;
            voiceButton.setText("Start Voice");
            showAnimatedStatus("Voice control stopped", new Color(0x00FFFF), false);
            playSound("move");
        } else {
            isVoiceListening = true;
            voiceButton.setText("Stop Voice");
            showAnimatedStatus("Listening for voice commands...", new Color(0x00FFFF), false);
            playSound("move");
            startVoiceSimulation();
        }
    }

    private void startVoiceSimulation() {
        new Thread(() -> {
            while (isVoiceListening) {
                String voiceInput = JOptionPane.showInputDialog(frame, "Enter voice command (e.g., 'open notepad'):");
                if (voiceInput == null || !isVoiceListening) {
                    break;
                }
                String processedInput = voiceInput.toLowerCase().trim();
                if (voiceCommands.containsKey(processedInput)) {
                    try {
                        String command = voiceCommands.get(processedInput);
                        Runtime.getRuntime().exec(command);
                        SwingUtilities.invokeLater(() -> {
                            showAnimatedStatus("Opened " + processedInput.substring(5) + " successfully!", new Color(0x00FF00), false);
                            playSound("win");
                        });
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> {
                            showAnimatedStatus("Error opening " + processedInput.substring(5) + ": " + e.getMessage(), new Color(0xFF0000), true);
                            playSound("error");
                        });
                    }
                } else {
                    SwingUtilities.invokeLater(() -> {
                        showAnimatedStatus("Unknown command: " + processedInput, new Color(0xFF0000), true);
                        playSound("error");
                    });
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            if (isVoiceListening) {
                isVoiceListening = false;
                SwingUtilities.invokeLater(() -> {
                    voiceButton.setText("Start Voice");
                    showAnimatedStatus("Voice control stopped", new Color(0x00FFFF), false);
                    playSound("move");
                });
            }
        }).start();
    }

    private void stopAutomation() {
        if (!isRunning) {
            showAnimatedStatus("No automation running!", new Color(0xFF0000), true);
            playSound("error");
            return;
        }
        isRunning = false;
        showAnimatedStatus("Automation stopped", new Color(0x00FFFF), false);
        playSound("move");
    }

    private void typeString(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(Character.toUpperCase(c));
                robot.keyRelease(Character.toUpperCase(c));
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else {
                int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
                if (keyCode != KeyEvent.VK_UNDEFINED) {
                    robot.keyPress(keyCode);
                    robot.keyRelease(keyCode);
                }
            }
            robot.delay(50);
        }
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
    }

    private void showAnimatedStatus(String message, Color color, boolean shake) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
        Timer timer = new Timer();
        final int[] alpha = {0};
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                alpha[0] += 25;
                statusLabel.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(alpha[0], 255)));
                if (alpha[0] >= 255) {
                    timer.cancel();
                }
            }
        }, 0, 50);

        if (shake) {
            Timer shakeTimer = new Timer();
            final int[] offset = {0};
            final int shakeAmplitude = 5;
            final int shakeDuration = 200;
            shakeTimer.scheduleAtFixedRate(new TimerTask() {
                int time = 0;
                @Override
                public void run() {
                    time += 50;
                    offset[0] = (int) (shakeAmplitude * Math.sin(time * 2 * Math.PI / shakeDuration));
                    statusLabel.setLocation(statusLabel.getX() + offset[0], statusLabel.getY());
                    if (time >= shakeDuration) {
                        statusLabel.setLocation(statusLabel.getX() - offset[0], statusLabel.getY());
                        shakeTimer.cancel();
                    }
                }
            }, 0, 50);
        }
    }

    private void playSound(String type) {
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private class GlowButton extends JButton {
        private Color baseColor;
        private boolean isPressed;

        public GlowButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(new Color(0xFFFFFF));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            isPressed = false;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            if (getMousePosition() != null) {
                g2d.setColor(new Color(0xFF00FF));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
            } else {
                g2d.setColor(baseColor);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
            }

            
            g2d.fillRoundRect(0, 0, w, h, 20, 20);

            if (isPressed) {
                g2d.scale(0.95, 0.95);
                g2d.translate(w * 0.025, h * 0.025);
            }

            super.paintComponent(g);
        }
    }

    private class Particle {
        double x, y, vx, vy;

        Particle() {
            Random rand = new Random();
            x = rand.nextInt(500);
            y = rand.nextInt(450);
            vx = (rand.nextDouble() - 0.5) * 2;
            vy = (rand.nextDouble() - 0.5) * 2;
        }

        void update(int maxX, int maxY) {
            x += vx;
            y += vy;
            if (x < 0 || x > maxX) vx = -vx;
            if (y < 0 || y > maxY) vy = -vy;
        }
    }

    private class FrameDragger extends MouseAdapter {
        private Point mousePoint;
        private JFrame frame;

        FrameDragger(JFrame frame) {
            this.frame = frame;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mousePoint = e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point newPoint = e.getLocationOnScreen();
            frame.setLocation(newPoint.x - mousePoint.x, newPoint.y - mousePoint.y);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new TaskAutomation();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to start: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
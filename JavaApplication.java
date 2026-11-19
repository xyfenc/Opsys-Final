import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

public class JavaApplication extends JFrame {
    private final CardLayout moduleCardLayout = new CardLayout();
    private final JPanel moduleForms = new JPanel(moduleCardLayout);
    private final JTextArea resultOutput = new JTextArea();
    private final JLabel moduleTitle = new JLabel();
    private final java.util.List<JPanel> cards = new ArrayList<>();
    private final JPanel root = new JPanel(new BorderLayout());
    private final JPanel main = new JPanel(new BorderLayout(0, 18));
    private final JScrollPane outputPane = new JScrollPane(resultOutput);
    private final AnimatedButton pauseResumeButton = new AnimatedButton("");
    private final AnimatedButton stopButton = new AnimatedButton("");
    private final AnimatedButton.PausePlayIcon pausePlayIcon = new AnimatedButton.PausePlayIcon();
    private javax.swing.Timer stepTimer;
    private String[] resultSteps = new String[0];
    private int currentStep;
    private boolean darkMode;

    private static final String[] MODULES = {
        "CPU Scheduling", "Banker's Algorithm", "Deadlock Detection", "Paging",
        "Page Replacement", "Segmentation", "Fragmentation", "Disk Scheduling", "Multiprogramming"
    };

    @FunctionalInterface interface Action { void run() throws Exception; }
    @FunctionalInterface interface ResultAction { String run() throws Exception; }
    private void execute(Action action) {
        try { action.run(); } catch (Exception ex) { showError(ex); }
    }

    public JavaApplication() {
        setTitle("Operating System Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
        root.putClientProperty("theme.page", Boolean.TRUE);
        main.putClientProperty("theme.page", Boolean.TRUE);

        root.add(sidebar(), BorderLayout.WEST);

        main.setBorder(new EmptyBorder(35, 42, 35, 42));
        
        var heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        moduleTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        heading.add(moduleTitle, BorderLayout.WEST);
        
        var reset = button("Reset form");
        reset.addActionListener(e -> showModule(0));
        var headingButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headingButtons.setOpaque(false);
        var themeButton = button("Dark mode");
        themeButton.addActionListener(e -> {
            darkMode = !darkMode;
            themeButton.setText(darkMode ? "Light mode" : "Dark mode");
            applyTheme();
        });
        headingButtons.add(pauseResumeButton);
        headingButtons.add(stopButton);
        headingButtons.add(themeButton);
        headingButtons.add(reset);
        heading.add(headingButtons, BorderLayout.EAST);
        main.add(heading, BorderLayout.NORTH);

        for (int i = 0; i < MODULES.length; i++) moduleForms.add(createForm(i), "m" + i);
        
        resultOutput.setEditable(false);
        resultOutput.setLineWrap(true);
        resultOutput.setWrapStyleWord(true);
        resultOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultOutput.setBorder(new EmptyBorder(18, 18, 18, 18));
        
        outputPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        configureButton(pauseResumeButton);
        configureButton(stopButton);
        pauseResumeButton.setIcon(pausePlayIcon);
        pauseResumeButton.setToolTipText("Pause");
        stopButton.setIcon(new AnimatedButton.StopIcon());
        stopButton.setToolTipText("Stop");
        pauseResumeButton.setEnabled(false);
        stopButton.setEnabled(false);
        pauseResumeButton.addActionListener(e -> togglePauseResume());
        stopButton.addActionListener(e -> stopSteps());

        var body = new JPanel(new GridLayout(1, 2, 18, 0));
        body.setOpaque(false);
        body.add(moduleForms);
        body.add(outputPane);
        main.add(body, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        applyTheme();
        showModule(0);
    }

    private JPanel sidebar() {
        var panel = new JPanel();
        panel.putClientProperty("theme.sidebar", Boolean.TRUE);
        panel.setPreferredSize(new Dimension(245, 0));
        panel.setBorder(new EmptyBorder(25, 16, 20, 16));
        panel.setBackground(Theme.SIDEBAR_BACKGROUND);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        var brand = new JLabel("  OS SIMULATOR");
        brand.setFont(new Font("SansSerif", Font.BOLD, 18));
        brand.setForeground(Theme.LIGHT_PANEL);
        panel.add(brand);
        panel.add(Box.createVerticalStrut(25));
        
        for (int i = 0; i < MODULES.length; i++) {
            final int index = i;
            var item = new AnimatedButton(String.format("%02d   %s", i + 1, MODULES[i]));
            item.setHorizontalAlignment(SwingConstants.LEFT);
            item.setForeground(Theme.SIDEBAR_TEXT);
            item.setBackground(Theme.SIDEBAR_BACKGROUND);
            item.setOpaque(true);
            item.setBorder(new EmptyBorder(10, 10, 10, 8));
            item.setFont(new Font("SansSerif", Font.PLAIN, 13));
            item.addActionListener(e -> showModule(index));
            panel.add(item);
        }
        
        panel.add(Box.createVerticalGlue());
        var note = new JLabel("<html>Opsys Fnal Project</html>");
        note.setForeground(Theme.SIDEBAR_MUTED);
        panel.add(note);
        return panel;
    }

    private JPanel createForm(int index) {
        var panel = card();
        panel.setLayout(new BorderLayout(0, 15));
        
        var fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        
        switch (index) {
            case 0 -> cpu(fields); case 1 -> banker(fields, true); case 2 -> banker(fields, false);
            case 3 -> paging(fields); case 4 -> replacement(fields); case 5 -> segmentation(fields);
            case 6 -> fragmentation(fields); case 7 -> disk(fields); case 8 -> multiprogramming(fields);
        }
        panel.add(new JScrollPane(fields), BorderLayout.CENTER);
        return panel;
    }

    private void cpu(JPanel p) {
        var algorithm = combo("FCFS", "SJF (non-preemptive)", "Priority (non-preemptive)", "SRTF (preemptive SJF)", "Round Robin");
        var count = input("4");
        var quantum = input("2");
        var processes = area("0 5 1\n1 3 2\n2 8 3\n3 4 4");
        add(p, "Algorithm", algorithm); add(p, "Process count", count); add(p, "Time quantum (Round Robin)", quantum);
        add(p, "Processes: arrival burst priority (one per line)", processes);
        
        addSimulationButtons(p, "Run CPU scheduling", () -> CpuScheduling.simulate(
            algorithm.getSelectedIndex(), Integer.parseInt(count.getText()), 
            Integer.parseInt(quantum.getText()), parseMatrix(processes.getText())));
    }

    private void banker(JPanel p, boolean safety) {
        var processes = input("3"); var resources = input("3");
        var allocation = area("0 1 0\n2 0 0\n3 0 2");
        var second = area(safety ? "7 5 3\n3 2 2\n9 0 2" : "0 0 0\n2 0 2\n0 0 2");
        var available = area("3 3 2");
        add(p, "Processes (1-5)", processes); add(p, "Resource types (1-3)", resources);
        add(p, "Allocation matrix", allocation); add(p, safety ? "Max matrix" : "Request matrix", second); add(p, "Available resources", available);
        
        addSimulationButtons(p, safety ? "Run safety check" : "Detect deadlock", () -> ResourceAllocation.analyze(
            safety, Integer.parseInt(processes.getText()), Integer.parseInt(resources.getText()), 
            parseMatrix(allocation.getText()), parseMatrix(second.getText()), parseNumbers(available.getText())));
    }

    private void paging(JPanel p) {
        var size = input("256"); var pages = input("4"); var table = input("2 5 -1 1"); var address = input("520");
        add(p, "Page size", size); add(p, "Logical pages", pages); add(p, "Page table (frame values)", table); add(p, "Logical address", address);
        addSimulationButtons(p, "Translate address", () -> Paging.translate(
            Integer.parseInt(size.getText()), Integer.parseInt(pages.getText()), 
            Integer.parseInt(address.getText()), parseNumbers(table.getText())));
    }

    private void replacement(JPanel p) {
        var algorithm = combo("FIFO", "LRU", "Optimal");
        var frames = input("3"); var references = input("7 0 1 2 0 3 0 4 2 3 0 3 2");
        add(p, "Algorithm", algorithm); add(p, "Number of frames", frames); add(p, "Reference string", references);
        addSimulationButtons(p, "Run page replacement", () -> PageReplacement.simulate(
            algorithm.getSelectedIndex(), Integer.parseInt(frames.getText()), parseNumbers(references.getText())));
    }

    private void segmentation(JPanel p) {
        var bases = input("1000 2000 3000"); var limits = input("400 500 250"); var segment = input("1"); var offset = input("120");
        add(p, "Segment bases", bases); add(p, "Segment limits", limits); add(p, "Segment number", segment); add(p, "Offset", offset);
        addSimulationButtons(p, "Translate segment address", () -> Segmentation.translate(
            parseNumbers(bases.getText()), parseNumbers(limits.getText()), 
            Integer.parseInt(segment.getText()), Integer.parseInt(offset.getText())));
    }

    private void fragmentation(JPanel p) {
        var strategy = combo("First Fit", "Best Fit", "Worst Fit");
        var blocks = input("100 500 200 300 600"); var processes = input("212 417 112 426");
        add(p, "Strategy", strategy); add(p, "Memory blocks", blocks); add(p, "Process sizes", processes);
        addSimulationButtons(p, "Allocate processes", () -> Fragmentation.allocate(
            strategy.getSelectedIndex(), parseNumbers(blocks.getText()), parseNumbers(processes.getText())));
    }

    private void disk(JPanel p) {
        var algorithm = combo("FCFS", "SSTF", "SCAN", "C-SCAN");
        var direction = combo("Higher cylinders", "Lower cylinders");
        var head = input("53"); var max = input("199"); var requests = input("98 183 37 122 14 124 65");
        add(p, "Algorithm", algorithm); add(p, "Direction", direction); add(p, "Initial head", head); add(p, "Max cylinder", max); add(p, "Request queue", requests);
        addSimulationButtons(p, "Run disk scheduling", () -> DiskScheduling.simulate(
            algorithm.getSelectedIndex(), direction.getSelectedIndex(), Integer.parseInt(head.getText()), 
            Integer.parseInt(max.getText()), parseNumbers(requests.getText())));
    }

    private void multiprogramming(JPanel p) {
        var memory = input("100"); var quantum = input("3"); var bursts = input("8 4 9 5"); var needs = input("30 40 50 20");
        add(p, "Total physical memory", memory); add(p, "Time quantum", quantum); add(p, "CPU burst times", bursts); add(p, "Memory requirements", needs);
        addSimulationButtons(p, "Run multiprogramming", () -> Multiprogramming.simulate(
            Integer.parseInt(memory.getText()), Integer.parseInt(quantum.getText()), 
            parseNumbers(bursts.getText()), parseNumbers(needs.getText())));
    }

    private JPanel card() { var p = new JPanel(); cards.add(p); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LIGHT_BORDER), new EmptyBorder(22, 22, 22, 22))); return p; }
    private JLabel label(String text) { var l = new JLabel(text); l.setOpaque(true); l.setBackground(Theme.LIGHT_HEADER); l.setForeground(Theme.LIGHT_TEXT); l.setFont(new Font("SansSerif", Font.BOLD, 12)); l.setBorder(new EmptyBorder(8, 8, 4, 8)); return l; }
    private JTextField input(String text) { var f = new JTextField(text); style(f); return f; }
    private JTextArea area(String text) { var a = new JTextArea(text, 4, 20); style(a); return a; }
    private JComboBox<String> combo(String... values) { var c = new JComboBox<>(values); style(c); return c; }
    
    private void style(JComponent c) { 
        c.setForeground(Theme.LIGHT_TEXT); c.setBackground(Theme.LIGHT_INPUT); 
        if (c instanceof JTextComponent) ((JTextComponent) c).setCaretColor(Theme.LIGHT_TEXT); 
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LIGHT_BORDER), new EmptyBorder(7, 8, 7, 8))); 
    }
    private void add(JPanel p, String name, JComponent component) { p.add(label(name)); p.add(component); }
    private AnimatedButton button(String text) {
        var b = new AnimatedButton(text);
        configureButton(b);
        return b;
    }

    private void configureButton(AnimatedButton button) {
        button.setForeground(Color.WHITE);
        button.setBackground(Theme.BUTTON);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
    }

    private void addSimulationButtons(JPanel panel, String runText, ResultAction action) {
        var buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        var run = button(runText);
        run.addActionListener(e -> execute(() -> {
            stopSteps();
            resultOutput.setText(action.run());
        }));
        var step = button("Step by step");
        step.addActionListener(e -> execute(() -> startSteps(action.run())));
        buttons.add(run);
        buttons.add(step);
        panel.add(buttons);
    }

    private void startSteps(String result) {
        stopSteps();
        resultSteps = result.split("\\R");
        currentStep = 0;
        resultOutput.setText("");
        pausePlayIcon.setRunning(true, pauseResumeButton);
        pauseResumeButton.setToolTipText("Pause");
        pauseResumeButton.setEnabled(true);
        stopButton.setEnabled(true);
        stepTimer = new javax.swing.Timer(800, e -> {
            if (currentStep < resultSteps.length) {
                resultOutput.append(resultSteps[currentStep++] + "\n");
                if (currentStep == resultSteps.length) stopSteps();
            } else {
                stopSteps();
            }
        });
        stepTimer.start();
    }

    private void togglePauseResume() {
        if (stepTimer == null) return;
        if (stepTimer.isRunning()) {
            stepTimer.stop();
            pausePlayIcon.setRunning(false, pauseResumeButton);
            pauseResumeButton.setToolTipText("Resume");
        } else {
            stepTimer.start();
            pausePlayIcon.setRunning(true, pauseResumeButton);
            pauseResumeButton.setToolTipText("Pause");
        }
    }

    private void stopSteps() {
        if (stepTimer != null) {
            stepTimer.stop();
            stepTimer = null;
        }
        pausePlayIcon.setRunning(true, pauseResumeButton);
        pauseResumeButton.setToolTipText("Pause");
        pauseResumeButton.setEnabled(false);
        stopButton.setEnabled(false);
    }

    private void applyTheme() {
        Color border = darkMode ? Theme.DARK_BORDER : Theme.LIGHT_BORDER;
        Color text = darkMode ? Theme.DARK_TEXT : Theme.LIGHT_TEXT;
        outputPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(border), "Simulation result",
            0, 0, new Font("SansSerif", Font.BOLD, 15), text));
        Theme.apply(root, darkMode);
        main.revalidate();
        main.repaint();
    }
    
    private int[] parseNumbers(String text) { return Arrays.stream(text.trim().split("[,\\s]+")).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray(); }
    private int[][] parseMatrix(String text) { return Arrays.stream(text.trim().split("\\n|;")).filter(s -> !s.trim().isEmpty()).map(this::parseNumbers).toArray(int[][]::new); }
    private void showModule(int i) { stopSteps(); moduleTitle.setText(MODULES[i]); moduleCardLayout.show(moduleForms, "m" + i); applyTheme(); resultOutput.setText("Fill out the inputs and then run the simulation."); }
    private void showError(Exception e) { stopSteps(); resultOutput.setText("INPUT ERROR\n\n" + e.getMessage()); }
    
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new JavaApplication().setVisible(true)); }
}
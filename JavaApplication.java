import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

public class JavaApplication extends JFrame {
    private final CardLayout moduleCardLayout = new CardLayout();
    private final JPanel moduleForms = new JPanel(moduleCardLayout);
    private final JTextArea resultOutput = new JTextArea();
    private final JLabel moduleTitle = new JLabel();
    private final JLabel moduleDescription = new JLabel();
    private final java.util.List<JPanel> cards = new ArrayList<>();
    private final JPanel root = new JPanel(new BorderLayout());
    private final JPanel main = new JPanel(new BorderLayout(0, 18));
    private final JScrollPane outputPane = new JScrollPane(resultOutput);
    private final java.util.List<AnimatedButton> sidebarItems = new ArrayList<>();
    private final java.util.Map<Integer, java.util.List<JComponent>> formInputs = new HashMap<>();
    private final AnimatedButton pauseResumeButton = new AnimatedButton("");
    private final AnimatedButton stopButton = new AnimatedButton("");
    private final AnimatedButton.PausePlayIcon pausePlayIcon = new AnimatedButton.PausePlayIcon();
    private javax.swing.Timer stepTimer;
    private String[] resultSteps = new String[0];
    private int currentStep;
    private boolean darkMode;
    private int activeModule;
    private int activeFormIndex = -1;

    private static final String[] MODULES = {
        "CPU Scheduling", "Banker's Algorithm", "Deadlock Detection", "Paging",
        "Page Replacement", "Segmentation", "Fragmentation", "Disk Scheduling", "Multiprogramming"
    };
    private static final String[] MODULE_DESCRIPTIONS = {
        "Compare scheduling policies and see how each process gets completed.",
        "Calculate the need matrix and check whether the system is safe.",
        "Check whether processes are blocked by unavailable resources.",
        "Translate a logical address into a physical address.",
        "Track page hits, faults, and frame replacement decisions.",
        "Check a segment offset and calculate its physical address.",
        "Place processes into memory blocks and measure wasted space.",
        "Calculate the order and total movement of disk requests.",
        "Simulate CPU sharing while processes compete for memory."
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
        
        var heading = new JPanel(new BorderLayout(18, 0));
        heading.setOpaque(false);
        var headingText = new JPanel();
        headingText.setOpaque(false);
        headingText.setLayout(new BoxLayout(headingText, BoxLayout.Y_AXIS));
        moduleTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        moduleTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        moduleDescription.setFont(new Font("SansSerif", Font.PLAIN, 14));
        moduleDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        headingText.add(moduleTitle);
        headingText.add(Box.createVerticalStrut(5));
        headingText.add(moduleDescription);
        heading.add(headingText, BorderLayout.CENTER);
        
        var autofill = button("Autofill");
        autofill.addActionListener(e -> autofillCurrentForm());
        var reset = button("Clear form");
        reset.addActionListener(e -> clearCurrentForm());
        var headingButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headingButtons.setOpaque(false);
        var themeButton = button("Dark mode");
        themeButton.addActionListener(e -> {
            darkMode = !darkMode;
            themeButton.setText(darkMode ? "Light mode" : "Dark mode");
            applyTheme();
        });
        headingButtons.setBorder(new EmptyBorder(4, 0, 0, 0));
        headingButtons.add(pauseResumeButton);
        headingButtons.add(stopButton);
        headingButtons.add(themeButton);
        headingButtons.add(autofill);
        headingButtons.add(reset);
        heading.add(headingButtons, BorderLayout.EAST);
        main.add(heading, BorderLayout.NORTH);

        for (int i = 0; i < MODULES.length; i++) moduleForms.add(createForm(i), "m" + i);
        
        resultOutput.setEditable(false);
        resultOutput.setLineWrap(false);
        resultOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultOutput.setBorder(new EmptyBorder(18, 18, 18, 18));
        
        outputPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.setPreferredSize(new Dimension(213, 38));
            item.setMinimumSize(new Dimension(213, 38));
            item.setMaximumSize(new Dimension(213, 38));
            item.addActionListener(e -> showModule(index));
            sidebarItems.add(item);
            panel.add(item);
        }
        
        panel.add(Box.createVerticalGlue());
        var note = new JLabel("<html>Opsys Final Project</html>");
        note.setForeground(Theme.SIDEBAR_MUTED);
        panel.add(note);
        return panel;
    }

    private JPanel createForm(int index) {
        var panel = card();
        panel.setLayout(new BorderLayout(0, 15));
        formInputs.put(index, new ArrayList<>());
        activeFormIndex = index;
        
        var fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        
        switch (index) {
            case 0 -> cpu(fields); case 1 -> banker(fields, true); case 2 -> banker(fields, false);
            case 3 -> paging(fields); case 4 -> replacement(fields); case 5 -> segmentation(fields);
            case 6 -> fragmentation(fields); case 7 -> disk(fields); case 8 -> multiprogramming(fields);
        }
        activeFormIndex = -1;
        panel.add(new JScrollPane(fields), BorderLayout.CENTER);
        return panel;
    }

    private void cpu(JPanel p) {
        var algorithm = combo("FCFS", "SJF (non-preemptive)", "Priority (non-preemptive)", "SRTF (preemptive SJF)", "Round Robin");
        var count = input("");
        var quantum = input("");
        var processes = tableInput("Process", "Arrival time", "Burst time", "Priority");
        add(p, "Algorithm", algorithm); add(p, "Process count", count); add(p, "Time quantum (Round Robin)", quantum);
        add(p, "Processes: arrival burst priority (one per line)", processes);
        
        addSimulationButtons(p, "Run CPU scheduling", () -> CpuScheduling.simulate(
            algorithm.getSelectedIndex(), Integer.parseInt(count.getText()), 
            Integer.parseInt(quantum.getText()), parseMatrix(processes)));
    }

    private void banker(JPanel p, boolean safety) {
        var processes = input(""); var resources = input("");
        var allocation = tableInput("Process", "R1", "R2", "R3", "R4", "R5");
        var second = tableInput("Process", "R1", "R2", "R3", "R4", "R5");
        var available = tableInput("Resource", "R1", "R2", "R3", "R4", "R5");
        add(p, "Processes (1-5)", processes); add(p, "Resource types (1-3)", resources);
        add(p, "Allocation matrix", allocation); add(p, safety ? "Max matrix" : "Request matrix", second); add(p, "Available resources", available);
        
        addSimulationButtons(p, safety ? "Run safety check" : "Detect deadlock", () -> ResourceAllocation.analyze(
            safety, Integer.parseInt(processes.getText()), Integer.parseInt(resources.getText()), 
            parseMatrix(allocation), parseMatrix(second), parseNumbers(available)));
    }

    private void paging(JPanel p) {
        var size = input(""); var pages = input(""); var table = tableInput("Page", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8"); var address = input("");
        add(p, "Page size", size); add(p, "Logical pages", pages); add(p, "Page table (frame values)", table); add(p, "Logical address", address);
        addSimulationButtons(p, "Translate address", () -> Paging.translate(
            Integer.parseInt(size.getText()), Integer.parseInt(pages.getText()), 
            Integer.parseInt(address.getText()), parseNumbers(table)));
    }

    private void replacement(JPanel p) {
        var algorithm = combo("FIFO", "LRU", "Optimal");
        var frames = input(""); var references = tableInput("Reference", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        add(p, "Algorithm", algorithm); add(p, "Number of frames", frames); add(p, "Reference string", references);
        addSimulationButtons(p, "Run page replacement", () -> PageReplacement.simulate(
            algorithm.getSelectedIndex(), Integer.parseInt(frames.getText()), parseNumbers(references)));
    }

    private void segmentation(JPanel p) {
        var bases = tableInput("Segment", "S1", "S2", "S3", "S4", "S5"); var limits = tableInput("Segment", "S1", "S2", "S3", "S4", "S5"); var segment = input(""); var offset = input("");
        add(p, "Segment bases", bases); add(p, "Segment limits", limits); add(p, "Segment number", segment); add(p, "Offset", offset);
        addSimulationButtons(p, "Translate segment address", () -> Segmentation.translate(
            parseNumbers(bases), parseNumbers(limits), 
            Integer.parseInt(segment.getText()), Integer.parseInt(offset.getText())));
    }

    private void fragmentation(JPanel p) {
        var strategy = combo("First Fit", "Best Fit", "Worst Fit");
        var blocks = tableInput("Block", "B1", "B2", "B3", "B4", "B5"); var processes = tableInput("Process", "P1", "P2", "P3", "P4", "P5");
        add(p, "Strategy", strategy); add(p, "Memory blocks", blocks); add(p, "Process sizes", processes);
        addSimulationButtons(p, "Allocate processes", () -> Fragmentation.allocate(
            strategy.getSelectedIndex(), parseNumbers(blocks), parseNumbers(processes)));
    }

    private void disk(JPanel p) {
        var algorithm = combo("FCFS", "SSTF", "SCAN", "C-SCAN");
        var direction = combo("Higher cylinders", "Lower cylinders");
        var head = input(""); var max = input(""); var requests = tableInput("Request", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        add(p, "Algorithm", algorithm); add(p, "Direction", direction); add(p, "Initial head", head); add(p, "Max cylinder", max); add(p, "Request queue", requests);
        addSimulationButtons(p, "Run disk scheduling", () -> DiskScheduling.simulate(
            algorithm.getSelectedIndex(), direction.getSelectedIndex(), Integer.parseInt(head.getText()), 
            Integer.parseInt(max.getText()), parseNumbers(requests)));
    }

    private void multiprogramming(JPanel p) {
        var memory = input(""); var quantum = input(""); var bursts = tableInput("Process", "P1", "P2", "P3", "P4", "P5"); var needs = tableInput("Process", "P1", "P2", "P3", "P4", "P5");
        add(p, "Total physical memory", memory); add(p, "Time quantum", quantum); add(p, "CPU burst times", bursts); add(p, "Memory requirements", needs);
        addSimulationButtons(p, "Run multiprogramming", () -> Multiprogramming.simulate(
            Integer.parseInt(memory.getText()), Integer.parseInt(quantum.getText()), 
            parseNumbers(bursts), parseNumbers(needs)));
    }

    private JPanel card() { var p = new JPanel(); cards.add(p); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LIGHT_BORDER), new EmptyBorder(22, 22, 22, 22))); return p; }
    private JLabel label(String text) {
        var l = new JLabel(text);
        l.setOpaque(true);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        l.setBackground(Theme.LIGHT_HEADER);
        l.setForeground(Theme.LIGHT_TEXT);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.LIGHT_BORDER),
            new EmptyBorder(8, 10, 8, 10)));
        return l;
    }
    private JTextField input(String text) { var f = new JTextField(text); style(f); return f; }
    private JTextArea area(String text) { var a = new JTextArea(text, 4, 20); style(a); return a; }
    private JComboBox<String> combo(String... values) { var c = new JComboBox<>(values); style(c); return c; }
    private JTable tableInput(String... headers) {
        boolean matrix = headers[0].equals("Process")
            && (headers[1].equals("Arrival time") || headers[1].startsWith("R"));
        int rows = matrix ? 8 : 1;
        var model = new DefaultTableModel(headers, rows) {
            @Override public boolean isCellEditable(int row, int column) { return column > 0; }
        };
        var table = new JTable(model);
        for (int row = 0; row < rows; row++) table.setValueAt(headers[0].equals("Process") ? "P" + (row + 1) : "Value", row, 0);
        table.setRowHeight(28);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getTableHeader().setReorderingAllowed(false);
        style(table);
        return table;
    }
    
    private void style(JComponent c) { 
        c.setForeground(Theme.LIGHT_TEXT); c.setBackground(Theme.LIGHT_INPUT); 
        if (c instanceof JTextComponent) ((JTextComponent) c).setCaretColor(Theme.LIGHT_TEXT); 
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LIGHT_BORDER), new EmptyBorder(7, 8, 7, 8))); 
    }
    private void add(JPanel p, String name, JComponent component) {
        p.add(label(name));
        p.add(component);
        if (activeFormIndex >= 0) formInputs.get(activeFormIndex).add(component);
    }

    private void autofillCurrentForm() {
        java.util.List<JComponent> controls = formInputs.get(activeModule);
        if (controls == null) return;
        String[] values = randomValues(activeModule);
        int valueIndex = 0;
        for (JComponent control : controls) {
            if (control instanceof JTextComponent text && valueIndex < values.length)
                text.setText(values[valueIndex++]);
            else if (control instanceof JTable table && valueIndex < values.length)
                fillTable(table, values[valueIndex++]);
        }
        resultOutput.setText("Random values filled in. Adjust them if needed, then run the simulation.");
    }

    private String[] randomValues(int index) {
        Random random = new Random();
        return switch (index) {
            case 0 -> {
                int count = 3 + random.nextInt(4);
                StringBuilder rows = new StringBuilder();
                for (int i = 0; i < count; i++)
                    rows.append(random.nextInt(5)).append(' ').append(1 + random.nextInt(8)).append(' ').append(1 + random.nextInt(5)).append('\n');
                yield new String[] {String.valueOf(count), String.valueOf(1 + random.nextInt(4)), rows.toString().trim()};
            }
            case 1, 2 -> {
                int count = 2 + random.nextInt(3), resources = 2 + random.nextInt(3);
                StringBuilder allocation = new StringBuilder(), second = new StringBuilder();
                for (int row = 0; row < count; row++) {
                    for (int column = 0; column < resources; column++) {
                        if (column > 0) { allocation.append(' '); second.append(' '); }
                        allocation.append(random.nextInt(3));
                        second.append(random.nextInt(index == 1 ? 7 : 3));
                    }
                    if (row + 1 < count) { allocation.append('\n'); second.append('\n'); }
                }
                StringBuilder available = new StringBuilder();
                for (int column = 0; column < resources; column++) {
                    if (column > 0) available.append(' ');
                    available.append(2 + random.nextInt(5));
                }
                yield new String[] {String.valueOf(count), String.valueOf(resources), allocation.toString(), second.toString(), available.toString()};
            }
            case 3 -> {
                int pageCount = 3 + random.nextInt(4), pageSize = 128 + random.nextInt(3) * 128;
                StringBuilder table = new StringBuilder();
                for (int i = 0; i < pageCount; i++) {
                    if (i > 0) table.append(' ');
                    table.append(random.nextInt(6) == 0 ? -1 : random.nextInt(8));
                }
                yield new String[] {String.valueOf(pageSize), String.valueOf(pageCount), table.toString(), String.valueOf(random.nextInt(pageCount * pageSize))};
            }
            case 4 -> new String[] {String.valueOf(2 + random.nextInt(3)), randomSequence(random, 10 + random.nextInt(7), 6)};
            case 5 -> new String[] {randomSequence(random, 3, 3000), randomSequence(random, 3, 600), String.valueOf(random.nextInt(3)), String.valueOf(20 + random.nextInt(200))};
            case 6 -> new String[] {randomSequence(random, 5, 700, 50), randomSequence(random, 4, 450, 50)};
            case 7 -> new String[] {"40", "199", randomSequence(random, 7, 190)};
            case 8 -> new String[] {"100", String.valueOf(1 + random.nextInt(4)), randomSequence(random, 4, 10, 1), randomSequence(random, 4, 70, 10)};
            default -> new String[0];
        };
    }

    private String randomSequence(Random random, int count, int maximum) {
        return randomSequence(random, count, maximum, 0);
    }

    private String randomSequence(Random random, int count, int maximum, int minimum) {
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) values.append(' ');
            values.append(minimum + random.nextInt(Math.max(1, maximum - minimum + 1)));
        }
        return values.toString();
    }

    private void clearCurrentForm() {
        java.util.List<JComponent> controls = formInputs.get(activeModule);
        if (controls != null) {
            for (JComponent control : controls)
                if (control instanceof JTextComponent text) text.setText("");
                else if (control instanceof JTable table)
                    for (int row = 0; row < table.getRowCount(); row++)
                        for (int column = 1; column < table.getColumnCount(); column++) table.setValueAt("", row, column);
        }
        stopSteps();
        resultOutput.setText("Inputs cleared. Enter values or click Autofill.");
    }

    private void fillTable(JTable table, String values) {
        String[] rows = values.trim().split("\\R");
        for (int row = 0; row < table.getRowCount(); row++)
            for (int column = 1; column < table.getColumnCount(); column++) table.setValueAt("", row, column);
        for (int row = 0; row < rows.length && row < table.getRowCount(); row++) {
            String[] cells = rows[row].trim().split("[,\\s]+");
            for (int column = 0; column < cells.length && column + 1 < table.getColumnCount(); column++)
                table.setValueAt(cells[column], row, column + 1);
        }
    }
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
    private int[] parseNumbers(JComponent component) {
        if (component instanceof JTable table) {
            ArrayList<Integer> values = new ArrayList<>();
            for (int row = 0; row < table.getRowCount(); row++)
                for (int column = 1; column < table.getColumnCount(); column++) {
                    Object value = table.getValueAt(row, column);
                    if (value != null && !value.toString().trim().isEmpty()) values.add(Integer.parseInt(value.toString().trim()));
                }
            return values.stream().mapToInt(Integer::intValue).toArray();
        }
        return parseNumbers(((JTextComponent) component).getText());
    }
    private int[][] parseMatrix(JComponent component) {
        if (component instanceof JTable table) {
            ArrayList<int[]> rows = new ArrayList<>();
            for (int row = 0; row < table.getRowCount(); row++) {
                ArrayList<Integer> values = new ArrayList<>();
                for (int column = 1; column < table.getColumnCount(); column++) {
                    Object value = table.getValueAt(row, column);
                    if (value != null && !value.toString().trim().isEmpty()) values.add(Integer.parseInt(value.toString().trim()));
                }
                if (!values.isEmpty()) rows.add(values.stream().mapToInt(Integer::intValue).toArray());
            }
            return rows.toArray(int[][]::new);
        }
        return parseMatrix(((JTextComponent) component).getText());
    }
    private void showModule(int i) {
        stopSteps();
        activeModule = i;
        for (int index = 0; index < sidebarItems.size(); index++)
            sidebarItems.get(index).putClientProperty("theme.selected", index == i);
        moduleTitle.setText(MODULES[i]);
        moduleDescription.setText(MODULE_DESCRIPTIONS[i]);
        moduleCardLayout.show(moduleForms, "m" + i);
        applyTheme();
        resultOutput.setText("Fill out the inputs and then run the simulation.");
    }
    private void showError(Exception e) { stopSteps(); resultOutput.setText("INPUT ERROR\n\n" + e.getMessage()); }
    
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new JavaApplication().setVisible(true)); }
}
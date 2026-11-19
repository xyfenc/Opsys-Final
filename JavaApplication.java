import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import javax.swing.table.DefaultTableModel;

public class JavaApplication extends JFrame {
    private final CardLayout moduleCardLayout = new CardLayout();
    private final JPanel moduleForms = new JPanel(moduleCardLayout);
    private final JTextArea resultOutput = new JTextArea();
    private final JPanel resultTablesPanel = new JPanel();
    private final java.util.List<JTable> displayedTables = new ArrayList<>();
    private final JLabel moduleTitle = new JLabel();
    private final java.util.List<JPanel> cards = new ArrayList<>();
    private final JPanel root = new JPanel(new BorderLayout());
    private final JPanel main = new JPanel(new BorderLayout(0, 18));
    private final JScrollPane resultTableScroll = new JScrollPane(resultTablesPanel);
    private final JScrollPane resultTextScroll = new JScrollPane(resultOutput);
    private final JSplitPane outputPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultTableScroll, resultTextScroll);
    private final java.util.List<java.util.List<JComponent>> moduleInputs = new ArrayList<>();
    private final int[] autofillCycles = new int[MODULES.length];
    private final JButton pauseResumeButton = button("Pause");
    private final JButton stopButton = button("Stop");
    private javax.swing.Timer stepTimer;
    private String[] resultSteps = new String[0];
    private int currentStep;
    private boolean darkMode;
    private int activeModule;
    private java.util.List<JComponent> activeInputs;

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

        root.add(sidebar(), BorderLayout.WEST);

        main.setBorder(new EmptyBorder(35, 42, 35, 42));
        
        var heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        moduleTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        heading.add(moduleTitle, BorderLayout.WEST);
        
        var reset = button("Reset form");
        reset.addActionListener(e -> resetCurrentForm());
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
        resultTablesPanel.setLayout(new BoxLayout(resultTablesPanel, BoxLayout.Y_AXIS));
        resultTableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        outputPane.setResizeWeight(0.45);
        outputPane.setContinuousLayout(true);
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
            var item = new JButton(String.format("%02d   %s", i + 1, MODULES[i]));
            item.setHorizontalAlignment(SwingConstants.LEFT);
            item.setForeground(Theme.SIDEBAR_TEXT);
            item.setBackground(Theme.SIDEBAR_BACKGROUND);
            item.setOpaque(true);
            item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.SIDEBAR_BORDER),
                new EmptyBorder(10, 10, 10, 8)));
            item.setFocusPainted(false);
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
        activeModule = index;
        activeInputs = new ArrayList<>();
        
        var fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        
        switch (index) {
            case 0 -> cpu(fields); case 1 -> banker(fields, true); case 2 -> banker(fields, false);
            case 3 -> paging(fields); case 4 -> replacement(fields); case 5 -> segmentation(fields);
            case 6 -> fragmentation(fields); case 7 -> disk(fields); case 8 -> multiprogramming(fields);
        }
        moduleInputs.add(activeInputs);
        panel.add(new JScrollPane(fields), BorderLayout.CENTER);
        return panel;
    }

    private void cpu(JPanel p) {
        var algorithm = combo("FCFS", "SJF (non-preemptive)", "Priority (non-preemptive)", "SRTF (preemptive SJF)", "Round Robin");
        var count = input("");
        var quantum = input("");
        var processes = area("");
        add(p, "Algorithm", algorithm); add(p, "Process count", count); add(p, "Time quantum (Round Robin)", quantum);
        add(p, "Processes: arrival burst priority (one per line)", processes);
        
        addSimulationButtons(p, "Run CPU scheduling", () -> CpuScheduling.simulate(
            algorithm.getSelectedIndex(), Integer.parseInt(count.getText()), 
            Integer.parseInt(quantum.getText()), parseMatrix(processes.getText())));
    }

    private void banker(JPanel p, boolean safety) {
        var processes = input(""); var resources = input("");
        var allocation = area("");
        var second = area("");
        var available = area("");
        add(p, "Processes (1-5)", processes); add(p, "Resource types (1-3)", resources);
        add(p, "Allocation matrix", allocation); add(p, safety ? "Max matrix" : "Request matrix", second); add(p, "Available resources", available);
        
        addSimulationButtons(p, safety ? "Run safety check" : "Detect deadlock", () -> ResourceAllocation.analyze(
            safety, Integer.parseInt(processes.getText()), Integer.parseInt(resources.getText()), 
            parseMatrix(allocation.getText()), parseMatrix(second.getText()), parseNumbers(available.getText())));
    }

    private void paging(JPanel p) {
        var size = input(""); var pages = input(""); var table = input(""); var address = input("");
        add(p, "Page size", size); add(p, "Logical pages", pages); add(p, "Page table (frame values)", table); add(p, "Logical address", address);
        addSimulationButtons(p, "Translate address", () -> Paging.translate(
            Integer.parseInt(size.getText()), Integer.parseInt(pages.getText()), 
            Integer.parseInt(address.getText()), parseNumbers(table.getText())));
    }

    private void replacement(JPanel p) {
        var algorithm = combo("FIFO", "LRU", "Optimal");
        var frames = input(""); var references = input("");
        add(p, "Algorithm", algorithm); add(p, "Number of frames", frames); add(p, "Reference string", references);
        addSimulationButtons(p, "Run page replacement", () -> PageReplacement.simulate(
            algorithm.getSelectedIndex(), Integer.parseInt(frames.getText()), parseNumbers(references.getText())));
    }

    private void segmentation(JPanel p) {
        var bases = input(""); var limits = input(""); var segment = input(""); var offset = input("");
        add(p, "Segment bases", bases); add(p, "Segment limits", limits); add(p, "Segment number", segment); add(p, "Offset", offset);
        addSimulationButtons(p, "Translate segment address", () -> Segmentation.translate(
            parseNumbers(bases.getText()), parseNumbers(limits.getText()), 
            Integer.parseInt(segment.getText()), Integer.parseInt(offset.getText())));
    }

    private void fragmentation(JPanel p) {
        var strategy = combo("First Fit", "Best Fit", "Worst Fit");
        var blocks = input(""); var processes = input("");
        add(p, "Strategy", strategy); add(p, "Memory blocks", blocks); add(p, "Process sizes", processes);
        addSimulationButtons(p, "Allocate processes", () -> Fragmentation.allocate(
            strategy.getSelectedIndex(), parseNumbers(blocks.getText()), parseNumbers(processes.getText())));
    }

    private void disk(JPanel p) {
        var algorithm = combo("FCFS", "SSTF", "SCAN", "C-SCAN");
        var direction = combo("Higher cylinders", "Lower cylinders");
        var head = input(""); var max = input(""); var requests = input("");
        add(p, "Algorithm", algorithm); add(p, "Direction", direction); add(p, "Initial head", head); add(p, "Max cylinder", max); add(p, "Request queue", requests);
        addSimulationButtons(p, "Run disk scheduling", () -> DiskScheduling.simulate(
            algorithm.getSelectedIndex(), direction.getSelectedIndex(), Integer.parseInt(head.getText()), 
            Integer.parseInt(max.getText()), parseNumbers(requests.getText())));
    }

    private void multiprogramming(JPanel p) {
        var memory = input(""); var quantum = input(""); var bursts = input(""); var needs = input("");
        add(p, "Total physical memory", memory); add(p, "Time quantum", quantum); add(p, "CPU burst times", bursts); add(p, "Memory requirements", needs);
        addSimulationButtons(p, "Run multiprogramming", () -> Multiprogramming.simulate(
            Integer.parseInt(memory.getText()), Integer.parseInt(quantum.getText()), 
            parseNumbers(bursts.getText()), parseNumbers(needs.getText())));
    }

    private JPanel card() { var p = new JPanel(); cards.add(p); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LIGHT_BORDER), new EmptyBorder(22, 22, 22, 22))); return p; }
    private JLabel label(String text) { var l = new JLabel(text); l.setFont(new Font("SansSerif", Font.BOLD, 12)); l.setBorder(new EmptyBorder(8, 0, 4, 0)); return l; }
    private JTextField input(String text) { var f = new JTextField(text); style(f); activeInputs.add(f); return f; }
    private JTextArea area(String text) { var a = new JTextArea(text, 4, 20); style(a); activeInputs.add(a); return a; }
    private JComboBox<String> combo(String... values) { var c = new JComboBox<>(values); style(c); activeInputs.add(c); return c; }
    
    private void style(JComponent c) { 
        c.setForeground(Theme.LIGHT_TEXT); c.setBackground(Theme.LIGHT_INPUT); 
        if (c instanceof JTextComponent) ((JTextComponent) c).setCaretColor(Theme.LIGHT_TEXT); 
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LIGHT_BORDER), new EmptyBorder(7, 8, 7, 8))); 
    }
    private void add(JPanel p, String name, JComponent component) { p.add(label(name)); p.add(component); }
    private JButton button(String text) { 
        var b = new JButton(text); b.setForeground(Color.WHITE); b.setBackground(Theme.BUTTON); b.setFont(new Font("SansSerif", Font.BOLD, 12)); b.setFocusPainted(false); b.setBorder(new EmptyBorder(10, 14, 10, 14)); return b; 
    }

    private void addSimulationButtons(JPanel panel, String runText, ResultAction action) {
        var buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        int moduleIndex = activeModule;
        var run = button(runText);
        run.addActionListener(e -> execute(() -> {
            stopSteps();
            showResults(action.run());
        }));
        var autofill = button("Autofill");
        autofill.addActionListener(e -> autofillForm(moduleIndex));
        var step = button("Step by step");
        step.addActionListener(e -> execute(() -> startSteps(action.run())));
        buttons.add(run);
        buttons.add(autofill);
        buttons.add(step);
        panel.add(buttons);
    }

    private void startSteps(String result) {
        stopSteps();
        resultSteps = renderResult(result).split("\\R");
        currentStep = 0;
        resultOutput.setText("");
        pauseResumeButton.setText("Pause");
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
            pauseResumeButton.setText("Resume");
        } else {
            stepTimer.start();
            pauseResumeButton.setText("Pause");
        }
    }

    private void stopSteps() {
        if (stepTimer != null) {
            stepTimer.stop();
            stepTimer = null;
        }
        pauseResumeButton.setText("Pause");
        pauseResumeButton.setEnabled(false);
        stopButton.setEnabled(false);
    }

    private void applyTheme() {
        Theme.apply(main, darkMode);
        Color page = darkMode ? Theme.DARK_BACKGROUND : Theme.LIGHT_BACKGROUND;
        Color panel = darkMode ? Theme.DARK_PANEL : Theme.LIGHT_PANEL;
        Color text = darkMode ? Theme.DARK_TEXT : Theme.LIGHT_TEXT;
        Color border = darkMode ? Theme.DARK_BORDER : Theme.LIGHT_BORDER;
        for (JPanel card : cards) {
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border), new EmptyBorder(22, 22, 22, 22)));
        }
        root.setBackground(page);
        main.setBackground(page);
        moduleForms.setBackground(panel);
        resultOutput.setBackground(panel);
        resultOutput.setForeground(text);
        resultOutput.setCaretColor(text);
        resultTablesPanel.setBackground(panel);
        resultTableScroll.getViewport().setBackground(panel);
        resultTextScroll.getViewport().setBackground(panel);
        for (JTable table : displayedTables) {
            table.setBackground(darkMode ? Theme.DARK_INPUT : Theme.LIGHT_INPUT);
            table.setForeground(text);
            table.setGridColor(border);
            table.setSelectionBackground(darkMode ? Theme.DARK_BACKGROUND : new Color(220, 233, 250));
            table.setSelectionForeground(text);
            table.getTableHeader().setBackground(darkMode ? Theme.DARK_BACKGROUND : Theme.LIGHT_BACKGROUND);
            table.getTableHeader().setForeground(text);
        }
        outputPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(border), "Simulation result",
            0, 0, new Font("SansSerif", Font.BOLD, 15), text));
        main.revalidate();
        main.repaint();
    }
    
    private int[] parseNumbers(String text) { return Arrays.stream(text.trim().split("[,\\s]+")).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray(); }
    private int[][] parseMatrix(String text) { return Arrays.stream(text.trim().split("\\n|;")).filter(s -> !s.trim().isEmpty()).map(this::parseNumbers).toArray(int[][]::new); }

    private void resetCurrentForm() {
        stopSteps();
        for (JComponent input : moduleInputs.get(activeModule)) {
            if (input instanceof JTextComponent) ((JTextComponent) input).setText("");
            else if (input instanceof JComboBox<?>) ((JComboBox<?>) input).setSelectedIndex(0);
        }
        showResults("Form cleared.");
    }

    private void autofillForm(int moduleIndex) {
        String[][] presets = autofillPresets(moduleIndex);
        int presetIndex = autofillCycles[moduleIndex]++ % presets.length;
        java.util.List<JComponent> inputs = moduleInputs.get(moduleIndex);
        String[] preset = presets[presetIndex];
        if (preset.length != inputs.size()) {
            showError(new IllegalStateException("Autofill data does not match this form."));
            return;
        }
        for (int fieldIndex = 0; fieldIndex < preset.length; fieldIndex++) {
            JComponent input = inputs.get(fieldIndex);
            if (input instanceof JTextComponent) ((JTextComponent) input).setText(preset[fieldIndex]);
            else if (input instanceof JComboBox<?>) ((JComboBox<?>) input).setSelectedIndex(Integer.parseInt(preset[fieldIndex]));
        }
        showResults("Sample " + (presetIndex + 1) + " of " + presets.length + " loaded.");
    }

    private String[][] autofillPresets(int moduleIndex) {
        return switch (moduleIndex) {
            case 0 -> new String[][] {
                {"0", "4", "2", "0 5 1\n1 3 2\n2 8 3\n3 4 4"},
                {"1", "3", "3", "0 7 2\n2 4 1\n3 2 3"},
                {"4", "4", "2", "0 6 2\n1 3 1\n2 5 3\n4 2 2"}
            };
            case 1 -> new String[][] {
                {"3", "2", "1 0\n0 1\n1 1", "2 1\n1 2\n1 1", "1 1"},
                {"3", "3", "1 0 0\n0 1 1\n1 1 0", "2 0 1\n0 2 2\n1 1 1", "1 1 1"},
                {"2", "2", "1 0\n0 1", "1 1\n1 1", "0 1"}
            };
            case 2 -> new String[][] {
                {"3", "2", "1 0\n0 1\n1 1", "0 1\n1 0\n0 0", "0 0"},
                {"3", "2", "1 0\n0 1\n1 1", "0 1\n1 0\n1 1", "0 0"},
                {"2", "2", "1 0\n0 1", "0 1\n1 0", "1 0"}
            };
            case 3 -> new String[][] {
                {"256", "4", "0 1 2 3", "520"},
                {"128", "5", "2 0 4 1 3", "520"},
                {"64", "3", "1 2 0", "145"}
            };
            case 4 -> new String[][] {
                {"0", "3", "7 0 1 2 0 3 0 4 2 3 0 3 2"},
                {"1", "4", "1 2 3 1 4 5 2 1 2 3"},
                {"2", "2", "1 2 3 1 4 2 5 1"}
            };
            case 5 -> new String[][] {
                {"1000 2000 3000", "400 500 250", "1", "120"},
                {"1200 2400", "300 600", "0", "299"},
                {"500 1500 2500 3500", "200 300 400 500", "2", "125"}
            };
            case 6 -> new String[][] {
                {"0", "100 500 200 300 600", "212 417 112 426"},
                {"1", "120 350 700 250", "115 340 240 400"},
                {"2", "90 180 400 650", "80 170 390 200"}
            };
            case 7 -> new String[][] {
                {"0", "0", "53", "199", "98 183 37 122 14 124 65"},
                {"1", "1", "50", "199", "10 20 60 90 150"},
                {"2", "0", "100", "499", "20 80 140 220 350 480"}
            };
            case 8 -> new String[][] {
                {"100", "3", "8 4 9 5", "30 40 50 20"},
                {"120", "2", "5 7 3", "40 60 30"},
                {"80", "4", "6 4 8 2", "25 35 40 20"}
            };
            default -> new String[0][];
        };
    }

    private void showResults(String result) {
        resultOutput.setText(renderResult(result));
        resultOutput.setCaretPosition(0);
    }

    private String renderResult(String result) {
        resultTablesPanel.removeAll();
        displayedTables.clear();
        StringBuilder text = new StringBuilder();
        String title = null;
        String[] headers = null;
        java.util.List<String[]> rows = new ArrayList<>();
        for (String line : result.split("\\R", -1)) {
            if (line.startsWith("@TABLE|")) {
                if (title != null) addResultTable(title, headers, rows);
                String[] parts = line.split("\\|", -1);
                title = parts.length > 1 ? parts[1] : "Results";
                headers = parts.length > 2 ? Arrays.copyOfRange(parts, 2, parts.length) : new String[0];
                rows = new ArrayList<>();
            } else if (line.equals("@ENDTABLE")) {
                if (title != null) addResultTable(title, headers, rows);
                title = null;
                headers = null;
                rows = new ArrayList<>();
            } else if (title != null && line.startsWith("@ROW|")) {
                rows.add(line.substring(5).split("\\|", -1));
            } else if (title == null) {
                text.append(line).append('\n');
            }
        }
        if (title != null) addResultTable(title, headers, rows);
        resultTablesPanel.revalidate();
        resultTablesPanel.repaint();
        return text.toString().stripTrailing();
    }

    private void addResultTable(String title, String[] headers, java.util.List<String[]> rows) {
        if (headers == null || headers.length == 0) return;
        Object[][] data = new Object[rows.size()][headers.length];
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < headers.length; columnIndex++) {
                data[rowIndex][columnIndex] = columnIndex < rows.get(rowIndex).length ? rows.get(rowIndex)[columnIndex] : "";
            }
        }
        var model = new DefaultTableModel(data, headers) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        var table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        displayedTables.add(table);
        applyResultTableTheme(table);

        var section = new JPanel(new BorderLayout(0, 5));
        section.setBorder(new EmptyBorder(10, 12, 4, 12));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBackground(darkMode ? Theme.DARK_PANEL : Theme.LIGHT_PANEL);
        var titleLabel = label(title);
        titleLabel.setForeground(darkMode ? Theme.DARK_TEXT : Theme.LIGHT_TEXT);
        section.add(titleLabel, BorderLayout.NORTH);
        var tableScroll = new JScrollPane(table);
        tableScroll.getViewport().setBackground(darkMode ? Theme.DARK_PANEL : Theme.LIGHT_PANEL);
        tableScroll.setBorder(BorderFactory.createLineBorder(darkMode ? Theme.DARK_BORDER : Theme.LIGHT_BORDER));
        tableScroll.setPreferredSize(new Dimension(520, Math.min(220, 36 + (rows.size() + 1) * table.getRowHeight())));
        section.add(tableScroll, BorderLayout.CENTER);
        resultTablesPanel.add(section);
    }

    private void applyResultTableTheme(JTable table) {
        Color text = darkMode ? Theme.DARK_TEXT : Theme.LIGHT_TEXT;
        Color border = darkMode ? Theme.DARK_BORDER : Theme.LIGHT_BORDER;
        table.setBackground(darkMode ? Theme.DARK_INPUT : Theme.LIGHT_INPUT);
        table.setForeground(text);
        table.setGridColor(border);
        table.setSelectionBackground(darkMode ? Theme.DARK_BACKGROUND : new Color(220, 233, 250));
        table.setSelectionForeground(text);
        table.getTableHeader().setBackground(darkMode ? Theme.DARK_BACKGROUND : Theme.LIGHT_BACKGROUND);
        table.getTableHeader().setForeground(text);
    }

    private void showModule(int i) { stopSteps(); activeModule = i; moduleTitle.setText(MODULES[i]); moduleCardLayout.show(moduleForms, "m" + i); showResults("Fill out the inputs and then run the simulation."); }
    private void showError(Exception e) { stopSteps(); showResults("INPUT ERROR\n\n" + e.getMessage()); }
    
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new JavaApplication().setVisible(true)); }
}
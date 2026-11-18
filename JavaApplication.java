import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

public class JavaApplication extends JFrame {
    private static final Color PAGE = new Color(245, 247, 250), WHITE = Color.WHITE,
        INK = new Color(27, 35, 48), MUTED = new Color(102, 113, 130),
        BORDER = new Color(221, 226, 234), DARK = new Color(22, 29, 41), BLUE = new Color(39, 93, 173);

    private final CardLayout moduleCardLayout = new CardLayout();
    private final JPanel moduleForms = new JPanel(moduleCardLayout);
    private final JTextArea resultOutput = new JTextArea();
    private final JLabel moduleTitle = new JLabel();

    private static final String[] MODULES = {
        "CPU Scheduling", "Banker's Algorithm", "Deadlock Detection", "Paging",
        "Page Replacement", "Segmentation", "Fragmentation", "Disk Scheduling", "Multiprogramming"
    };

    @FunctionalInterface interface Action { void run() throws Exception; }
    private void execute(Action action) {
        try { action.run(); } catch (Exception ex) { showError(ex); }
    }

    public JavaApplication() {
        setTitle("Operating System Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);

        var root = new JPanel(new BorderLayout());
        root.setBackground(PAGE);
        root.add(sidebar(), BorderLayout.WEST);

        var main = new JPanel(new BorderLayout(0, 18));
        main.setBorder(new EmptyBorder(35, 42, 35, 42));
        main.setBackground(PAGE);
        
        var heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        moduleTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        moduleTitle.setForeground(INK);
        heading.add(moduleTitle, BorderLayout.WEST);
        
        var reset = button("Reset form");
        reset.addActionListener(e -> showModule(0));
        heading.add(reset, BorderLayout.EAST);
        main.add(heading, BorderLayout.NORTH);

        for (int i = 0; i < MODULES.length; i++) moduleForms.add(createForm(i), "m" + i);
        
        resultOutput.setEditable(false);
        resultOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultOutput.setForeground(INK);
        resultOutput.setBackground(WHITE);
        resultOutput.setBorder(new EmptyBorder(18, 18, 18, 18));
        
        var outputPane = new JScrollPane(resultOutput);
        outputPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER), "Simulation result",
            0, 0, new Font("SansSerif", Font.BOLD, 15), INK));

        var body = new JPanel(new GridLayout(1, 2, 18, 0));
        body.setOpaque(false);
        body.add(moduleForms);
        body.add(outputPane);
        main.add(body, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        showModule(0);
    }

    private JPanel sidebar() {
        var panel = new JPanel();
        panel.setPreferredSize(new Dimension(245, 0));
        panel.setBorder(new EmptyBorder(25, 16, 20, 16));
        panel.setBackground(DARK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        var brand = new JLabel("  OS SIMULATOR");
        brand.setFont(new Font("SansSerif", Font.BOLD, 18));
        brand.setForeground(WHITE);
        panel.add(brand);
        panel.add(Box.createVerticalStrut(25));
        
        for (int i = 0; i < MODULES.length; i++) {
            final int index = i;
            var item = new JButton(String.format("%02d   %s", i + 1, MODULES[i]));
            item.setHorizontalAlignment(SwingConstants.LEFT);
            item.setForeground(new Color(202, 211, 226));
            item.setBackground(DARK);
            item.setOpaque(true);
            item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(48, 58, 75)),
                new EmptyBorder(10, 10, 10, 8)));
            item.setFocusPainted(false);
            item.addActionListener(e -> showModule(index));
            panel.add(item);
        }
        
        panel.add(Box.createVerticalGlue());
        var note = new JLabel("<html>Opsys Fnal Project</html>");
        note.setForeground(new Color(137, 148, 166));
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
        
        var run = button("Run CPU scheduling");
        run.addActionListener(e -> execute(() -> resultOutput.setText(CpuScheduling.simulate(
            algorithm.getSelectedIndex(), Integer.parseInt(count.getText()), 
            Integer.parseInt(quantum.getText()), parseMatrix(processes.getText())))));
        p.add(run);
    }

    private void banker(JPanel p, boolean safety) {
        var processes = input("3"); var resources = input("3");
        var allocation = area("0 1 0\n2 0 0\n3 0 2");
        var second = area(safety ? "7 5 3\n3 2 2\n9 0 2" : "0 0 0\n2 0 2\n0 0 2");
        var available = area("3 3 2");
        add(p, "Processes (1-5)", processes); add(p, "Resource types (1-3)", resources);
        add(p, "Allocation matrix", allocation); add(p, safety ? "Max matrix" : "Request matrix", second); add(p, "Available resources", available);
        
        var run = button(safety ? "Run safety check" : "Detect deadlock");
        run.addActionListener(e -> execute(() -> resultOutput.setText(ResourceAllocation.analyze(
            safety, Integer.parseInt(processes.getText()), Integer.parseInt(resources.getText()), 
            parseMatrix(allocation.getText()), parseMatrix(second.getText()), parseNumbers(available.getText())))));
        p.add(run);
    }

    private void paging(JPanel p) {
        var size = input("256"); var pages = input("4"); var table = input("2 5 -1 1"); var address = input("520");
        add(p, "Page size", size); add(p, "Logical pages", pages); add(p, "Page table (frame values)", table); add(p, "Logical address", address);
        var run = button("Translate address");
        run.addActionListener(e -> execute(() -> resultOutput.setText(Paging.translate(
            Integer.parseInt(size.getText()), Integer.parseInt(pages.getText()), 
            Integer.parseInt(address.getText()), parseNumbers(table.getText())))));
        p.add(run);
    }

    private void replacement(JPanel p) {
        var algorithm = combo("FIFO", "LRU", "Optimal");
        var frames = input("3"); var references = input("7 0 1 2 0 3 0 4 2 3 0 3 2");
        add(p, "Algorithm", algorithm); add(p, "Number of frames", frames); add(p, "Reference string", references);
        var run = button("Run page replacement");
        run.addActionListener(e -> execute(() -> resultOutput.setText(PageReplacement.simulate(
            algorithm.getSelectedIndex(), Integer.parseInt(frames.getText()), parseNumbers(references.getText())))));
        p.add(run);
    }

    private void segmentation(JPanel p) {
        var bases = input("1000 2000 3000"); var limits = input("400 500 250"); var segment = input("1"); var offset = input("120");
        add(p, "Segment bases", bases); add(p, "Segment limits", limits); add(p, "Segment number", segment); add(p, "Offset", offset);
        var run = button("Translate segment address");
        run.addActionListener(e -> execute(() -> resultOutput.setText(Segmentation.translate(
            parseNumbers(bases.getText()), parseNumbers(limits.getText()), 
            Integer.parseInt(segment.getText()), Integer.parseInt(offset.getText())))));
        p.add(run);
    }

    private void fragmentation(JPanel p) {
        var strategy = combo("First Fit", "Best Fit", "Worst Fit");
        var blocks = input("100 500 200 300 600"); var processes = input("212 417 112 426");
        add(p, "Strategy", strategy); add(p, "Memory blocks", blocks); add(p, "Process sizes", processes);
        var run = button("Allocate processes");
        run.addActionListener(e -> execute(() -> resultOutput.setText(Fragmentation.allocate(
            strategy.getSelectedIndex(), parseNumbers(blocks.getText()), parseNumbers(processes.getText())))));
        p.add(run);
    }

    private void disk(JPanel p) {
        var algorithm = combo("FCFS", "SSTF", "SCAN", "C-SCAN");
        var direction = combo("Higher cylinders", "Lower cylinders");
        var head = input("53"); var max = input("199"); var requests = input("98 183 37 122 14 124 65");
        add(p, "Algorithm", algorithm); add(p, "Direction", direction); add(p, "Initial head", head); add(p, "Max cylinder", max); add(p, "Request queue", requests);
        var run = button("Run disk scheduling");
        run.addActionListener(e -> execute(() -> resultOutput.setText(DiskScheduling.simulate(
            algorithm.getSelectedIndex(), direction.getSelectedIndex(), Integer.parseInt(head.getText()), 
            Integer.parseInt(max.getText()), parseNumbers(requests.getText())))));
        p.add(run);
    }

    private void multiprogramming(JPanel p) {
        var memory = input("100"); var quantum = input("3"); var bursts = input("8 4 9 5"); var needs = input("30 40 50 20");
        add(p, "Total physical memory", memory); add(p, "Time quantum", quantum); add(p, "CPU burst times", bursts); add(p, "Memory requirements", needs);
        var run = button("Run multiprogramming");
        run.addActionListener(e -> execute(() -> resultOutput.setText(Multiprogramming.simulate(
            Integer.parseInt(memory.getText()), Integer.parseInt(quantum.getText()), 
            parseNumbers(bursts.getText()), parseNumbers(needs.getText())))));
        p.add(run);
    }

    private JPanel card() { var p = new JPanel(); p.setBackground(WHITE); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(22, 22, 22, 22))); return p; }
    private JLabel label(String text) { var l = new JLabel(text); l.setForeground(INK); l.setFont(new Font("SansSerif", Font.BOLD, 12)); l.setBorder(new EmptyBorder(8, 0, 4, 0)); return l; }
    private JTextField input(String text) { var f = new JTextField(text); style(f); return f; }
    private JTextArea area(String text) { var a = new JTextArea(text, 4, 20); style(a); return a; }
    private JComboBox<String> combo(String... values) { var c = new JComboBox<>(values); style(c); return c; }
    
    private void style(JComponent c) { 
        c.setForeground(INK); c.setBackground(new Color(250, 251, 253)); 
        if (c instanceof JTextComponent) ((JTextComponent) c).setCaretColor(INK); 
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 8, 7, 8))); 
    }
    private void add(JPanel p, String name, JComponent component) { p.add(label(name)); p.add(component); }
    private JButton button(String text) { 
        var b = new JButton(text); b.setForeground(WHITE); b.setBackground(BLUE); b.setFont(new Font("SansSerif", Font.BOLD, 12)); b.setFocusPainted(false); b.setBorder(new EmptyBorder(10, 14, 10, 14)); return b; 
    }
    
    private int[] parseNumbers(String text) { return Arrays.stream(text.trim().split("[,\\s]+")).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray(); }
    private int[][] parseMatrix(String text) { return Arrays.stream(text.trim().split("\\n|;")).filter(s -> !s.trim().isEmpty()).map(this::parseNumbers).toArray(int[][]::new); }
    private void showModule(int i) { moduleTitle.setText(MODULES[i]); moduleCardLayout.show(moduleForms, "m" + i); resultOutput.setText("Fill out the inputs and then run the simulation."); }
    private void showError(Exception e) { resultOutput.setText("INPUT ERROR\n\n" + e.getMessage()); }
    
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new JavaApplication().setVisible(true)); }
}
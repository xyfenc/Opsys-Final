import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

public class JavaApplication extends JFrame {
    private static final Color PAGE = new Color(245, 247, 250);
    private static final Color WHITE = Color.WHITE;
    private static final Color INK = new Color(27, 35, 48);
    private static final Color MUTED = new Color(102, 113, 130);
    private static final Color BORDER = new Color(221, 226, 234);
    private static final Color DARK = new Color(22, 29, 41);
    private static final Color BLUE = new Color(39, 93, 173);

    private final CardLayout moduleCardLayout = new CardLayout();
    private final JPanel moduleForms = new JPanel(moduleCardLayout);
    private final JTextArea resultOutput = new JTextArea();
    private final JLabel moduleTitle = new JLabel();

    private static final String[] MODULES = {
        "CPU Scheduling", "Banker's Algorithm", "Deadlock Detection", "Paging",
        "Page Replacement", "Segmentation", "Fragmentation", "Disk Scheduling",
        "Multiprogramming"
    };

    public JavaApplication() {
        setTitle("Operating System Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE);
        root.add(sidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBorder(new EmptyBorder(35, 42, 35, 42));
        main.setBackground(PAGE);
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        moduleTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        moduleTitle.setForeground(INK);
        heading.add(moduleTitle, BorderLayout.WEST);
        JButton reset = button("Reset form");
        reset.addActionListener(e -> showModule(0));
        heading.add(reset, BorderLayout.EAST);
        main.add(heading, BorderLayout.NORTH);

        for (int moduleIndex = 0; moduleIndex < MODULES.length; moduleIndex++) moduleForms.add(createForm(moduleIndex), "m" + moduleIndex);
        resultOutput.setEditable(false);
        resultOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultOutput.setForeground(INK);
        resultOutput.setBackground(WHITE);
        resultOutput.setBorder(new EmptyBorder(18, 18, 18, 18));
        JScrollPane outputPane = new JScrollPane(resultOutput);
        outputPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER), "Simulation result",
            0, 0, new Font("SansSerif", Font.BOLD, 15), INK));

        JPanel body = new JPanel(new GridLayout(1, 2, 18, 0));
        body.setOpaque(false);
        body.add(moduleForms);
        body.add(outputPane);
        main.add(body, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        showModule(0);
    }

    private JPanel sidebar() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(245, 0));
        panel.setBorder(new EmptyBorder(25, 16, 20, 16));
        panel.setBackground(DARK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("  OS SIMULATOR");
        brand.setFont(new Font("SansSerif", Font.BOLD, 18));
        brand.setForeground(WHITE);
        panel.add(brand);
        panel.add(Box.createVerticalStrut(25));
        for (int i = 0; i < MODULES.length; i++) {
            final int index = i;
            JButton item = new JButton(String.format("%02d   %s", i + 1, MODULES[i]));
            item.setHorizontalAlignment(SwingConstants.LEFT);
            item.setForeground(new Color(202, 211, 226));
            item.setBackground(DARK);
            item.setBorderPainted(false);
            item.setFocusPainted(false);
            item.addActionListener(e -> showModule(index));
            panel.add(item);
        }
        panel.add(Box.createVerticalGlue());
        JLabel note = new JLabel("<html>Opsys Fnal Project</html>");
        note.setForeground(new Color(137, 148, 166));
        panel.add(note);
        return panel;
    }

    private JPanel createForm(int moduleIndex) {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 15));
        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        switch (moduleIndex) {
            case 0 -> cpu(fields);
            case 1 -> banker(fields, true);
            case 2 -> banker(fields, false);
            case 3 -> paging(fields);
            case 4 -> replacement(fields);
            case 5 -> segmentation(fields);
            case 6 -> fragmentation(fields);
            case 7 -> disk(fields);
            case 8 -> multiprogramming(fields);
        }
        panel.add(new JScrollPane(fields), BorderLayout.CENTER);
        return panel;
    }

    private void cpu(JPanel p) {
        JComboBox<String> algorithm = combo("FCFS", "SJF (non-preemptive)", "Priority (non-preemptive)", "SRTF (preemptive SJF)", "Round Robin");
        JTextField count = input("4");
        JTextField quantum = input("2");
        JTextArea processes = area("0 5 1\n1 3 2\n2 8 3\n3 4 4");
        add(p, "Algorithm", algorithm); add(p, "Process count", count); add(p, "Time quantum (Round Robin)", quantum);
        add(p, "Processes: arrival burst priority (one per line)", processes);
        JButton run = button("Run CPU scheduling");
        run.addActionListener(e -> {
            try { resultOutput.setText(CpuScheduling.simulate(algorithm.getSelectedIndex(), Integer.parseInt(count.getText()), Integer.parseInt(quantum.getText()), parseMatrix(processes.getText()))); }
            catch (Exception ex) { showError(ex); }
        });
        p.add(run);
    }

    private void banker(JPanel p, boolean safety) {
        JTextField processes = input("3"), resources = input("3");
        JTextArea allocation = area("0 1 0\n2 0 0\n3 0 2");
        JTextArea second = area(safety ? "7 5 3\n3 2 2\n9 0 2" : "0 0 0\n2 0 2\n0 0 2");
        JTextArea available = area("3 3 2");
        add(p, "Processes (1-5)", processes); add(p, "Resource types (1-3)", resources);
        add(p, "Allocation matrix", allocation); add(p, safety ? "Max matrix" : "Request matrix", second); add(p, "Available resources", available);
        JButton run = button(safety ? "Run safety check" : "Detect deadlock");
        run.addActionListener(e -> {
            try { resultOutput.setText(ResourceAllocation.analyze(safety, Integer.parseInt(processes.getText()), Integer.parseInt(resources.getText()), parseMatrix(allocation.getText()), parseMatrix(second.getText()), parseNumbers(available.getText()))); }
            catch (Exception ex) { showError(ex); }
        });
        p.add(run);
    }

    private void paging(JPanel p) {
        JTextField size = input("256"), pages = input("4"), table = input("2 5 -1 1"), address = input("520");
        add(p, "Page size", size); add(p, "Logical pages", pages); add(p, "Page table (frame values)", table); add(p, "Logical address", address);
        JButton run = button("Translate address");
        run.addActionListener(e -> { try { resultOutput.setText(Paging.translate(Integer.parseInt(size.getText()), Integer.parseInt(pages.getText()), Integer.parseInt(address.getText()), parseNumbers(table.getText()))); } catch (Exception ex) { showError(ex); } });
        p.add(run);
    }

    private void replacement(JPanel p) {
        JComboBox<String> algorithm = combo("FIFO", "LRU", "Optimal");
        JTextField frames = input("3"), references = input("7 0 1 2 0 3 0 4 2 3 0 3 2");
        add(p, "Algorithm", algorithm); add(p, "Number of frames", frames); add(p, "Reference string", references);
        JButton run = button("Run page replacement");
        run.addActionListener(e -> { try { resultOutput.setText(PageReplacement.simulate(algorithm.getSelectedIndex(), Integer.parseInt(frames.getText()), parseNumbers(references.getText()))); } catch (Exception ex) { showError(ex); } });
        p.add(run);
    }

    private void segmentation(JPanel p) {
        JTextField bases = input("1000 2000 3000"), limits = input("400 500 250"), segment = input("1"), offset = input("120");
        add(p, "Segment bases", bases); add(p, "Segment limits", limits); add(p, "Segment number", segment); add(p, "Offset", offset);
        JButton run = button("Translate segment address");
        run.addActionListener(e -> { try { resultOutput.setText(Segmentation.translate(parseNumbers(bases.getText()), parseNumbers(limits.getText()), Integer.parseInt(segment.getText()), Integer.parseInt(offset.getText()))); } catch (Exception ex) { showError(ex); } });
        p.add(run);
    }

    private void fragmentation(JPanel p) {
        JComboBox<String> strategy = combo("First Fit", "Best Fit", "Worst Fit");
        JTextField blocks = input("100 500 200 300 600"), processes = input("212 417 112 426");
        add(p, "Strategy", strategy); add(p, "Memory blocks", blocks); add(p, "Process sizes", processes);
        JButton run = button("Allocate processes");
        run.addActionListener(e -> { try { resultOutput.setText(Fragmentation.allocate(strategy.getSelectedIndex(), parseNumbers(blocks.getText()), parseNumbers(processes.getText()))); } catch (Exception ex) { showError(ex); } });
        p.add(run);
    }

    private void disk(JPanel p) {
        JComboBox<String> algorithm = combo("FCFS", "SSTF", "SCAN", "C-SCAN");
        JComboBox<String> direction = combo("Higher cylinders", "Lower cylinders");
        JTextField head = input("53"), max = input("199"), requests = input("98 183 37 122 14 124 65");
        add(p, "Algorithm", algorithm); add(p, "Direction", direction); add(p, "Initial head", head); add(p, "Max cylinder", max); add(p, "Request queue", requests);
        JButton run = button("Run disk scheduling");
        run.addActionListener(e -> { try { resultOutput.setText(DiskScheduling.simulate(algorithm.getSelectedIndex(), direction.getSelectedIndex(), Integer.parseInt(head.getText()), Integer.parseInt(max.getText()), parseNumbers(requests.getText()))); } catch (Exception ex) { showError(ex); } });
        p.add(run);
    }

    private void multiprogramming(JPanel p) {
        JTextField memory = input("100"), quantum = input("3"), bursts = input("8 4 9 5"), needs = input("30 40 50 20");
        add(p, "Total physical memory", memory); add(p, "Time quantum", quantum); add(p, "CPU burst times", bursts); add(p, "Memory requirements", needs);
        JButton run = button("Run multiprogramming");
        run.addActionListener(e -> { try { resultOutput.setText(Multiprogramming.simulate(Integer.parseInt(memory.getText()), Integer.parseInt(quantum.getText()), parseNumbers(bursts.getText()), parseNumbers(needs.getText()))); } catch (Exception ex) { showError(ex); } });
        p.add(run);
    }

    private String cpuResult(int algorithmChoice, int processCount, int timeQuantum, String processInput) throws Exception {
        int[][] processRows = parseMatrix(processInput); if (processRows.length != processCount || processCount < 1 || processCount > 8) throw new Exception("Provide one row per process.");
        int[] arrivalTimes = new int[processCount], burstTimes = new int[processCount], priorities = new int[processCount], remainingTimes = new int[processCount], completionTimes = new int[processCount];
        for (int processIndex = 0; processIndex < processCount; processIndex++) { if (processRows[processIndex].length < 2) throw new Exception("Each process needs arrival and burst."); arrivalTimes[processIndex] = processRows[processIndex][0]; burstTimes[processIndex] = remainingTimes[processIndex] = processRows[processIndex][1]; priorities[processIndex] = processRows[processIndex].length > 2 ? processRows[processIndex][2] : processIndex; }
        int currentTime = 0, completedProcesses = 0; ArrayDeque<Integer> readyQueue = new ArrayDeque<>();
        while (completedProcesses < processCount) {
            if (algorithmChoice == 4) {
                for (int processIndex = 0; processIndex < processCount; processIndex++) if (remainingTimes[processIndex] > 0 && arrivalTimes[processIndex] <= currentTime && !readyQueue.contains(processIndex)) readyQueue.add(processIndex);
                if (readyQueue.isEmpty()) { currentTime++; continue; } int processIndex = readyQueue.remove(), executionTime = Math.min(timeQuantum, remainingTimes[processIndex]); currentTime += executionTime; remainingTimes[processIndex] -= executionTime;
                for (int otherProcessIndex = 0; otherProcessIndex < processCount; otherProcessIndex++) if (remainingTimes[otherProcessIndex] > 0 && arrivalTimes[otherProcessIndex] <= currentTime && !readyQueue.contains(otherProcessIndex) && otherProcessIndex != processIndex) readyQueue.add(otherProcessIndex);
                if (remainingTimes[processIndex] > 0) readyQueue.add(processIndex); else { completionTimes[processIndex] = currentTime; completedProcesses++; }
            } else {
                int selectedProcess = -1; for (int processIndex = 0; processIndex < processCount; processIndex++) if (remainingTimes[processIndex] > 0 && arrivalTimes[processIndex] <= currentTime && (selectedProcess < 0 || (algorithmChoice == 1 ? burstTimes[processIndex] < burstTimes[selectedProcess] : algorithmChoice == 2 ? priorities[processIndex] < priorities[selectedProcess] : algorithmChoice == 3 ? remainingTimes[processIndex] < remainingTimes[selectedProcess] : arrivalTimes[processIndex] < arrivalTimes[selectedProcess]))) selectedProcess = processIndex;
                if (selectedProcess < 0) { currentTime++; continue; } currentTime++; remainingTimes[selectedProcess]--; if (algorithmChoice != 3) { currentTime += remainingTimes[selectedProcess]; remainingTimes[selectedProcess] = 0; } if (remainingTimes[selectedProcess] == 0) { completionTimes[selectedProcess] = currentTime; completedProcesses++; }
            }
        }
        StringBuilder result = new StringBuilder("CPU SCHEDULING RESULT\n\nProcess  Arrival  Burst  Completion  Turnaround  Waiting\n"); double totalTurnaround = 0, totalWaiting = 0;
        for (int processIndex = 0; processIndex < processCount; processIndex++) { int turnaroundTime = completionTimes[processIndex] - arrivalTimes[processIndex], waitingTime = turnaroundTime - burstTimes[processIndex]; totalTurnaround += turnaroundTime; totalWaiting += waitingTime; result.append(String.format("P%-7d %-8d %-6d %-11d %-11d %d%n", processIndex, arrivalTimes[processIndex], burstTimes[processIndex], completionTimes[processIndex], turnaroundTime, waitingTime)); }
        return result.append(String.format("%nAverage turnaround time = %.2f%nAverage waiting time    = %.2f", totalTurnaround / processCount, totalWaiting / processCount)).toString();
    }

    private String matrixResult(boolean safetyCheck, int processCount, int resourceCount, int[][] allocationMatrix, int[][] requestedOrMaximumMatrix, int[] availableResources) throws Exception {
        if (allocationMatrix.length != processCount || requestedOrMaximumMatrix.length != processCount || availableResources.length != resourceCount) throw new Exception("Matrix dimensions do not match.");
        int[] remainingResources = availableResources.clone(); boolean[] processFinished = new boolean[processCount]; ArrayList<Integer> safeSequence = new ArrayList<>(); boolean madeProgress = true;
        while (madeProgress) { madeProgress = false; for (int processIndex = 0; processIndex < processCount; processIndex++) if (!processFinished[processIndex]) { boolean canFinish = true; for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) { int requiredResources = safetyCheck ? Math.max(0, requestedOrMaximumMatrix[processIndex][resourceIndex] - allocationMatrix[processIndex][resourceIndex]) : requestedOrMaximumMatrix[processIndex][resourceIndex]; if (requiredResources > remainingResources[resourceIndex]) canFinish = false; } if (canFinish) { for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) remainingResources[resourceIndex] += allocationMatrix[processIndex][resourceIndex]; processFinished[processIndex] = true; safeSequence.add(processIndex); madeProgress = true; } } }
        StringBuilder result = new StringBuilder(safetyCheck ? "BANKER'S ALGORITHM RESULT\n\n" : "DEADLOCK DETECTION RESULT\n\n");
        boolean hasBlockedProcess = false; for (boolean finished : processFinished) if (!finished) hasBlockedProcess = true;
        if (safetyCheck) result.append(hasBlockedProcess ? "SYSTEM IS NOT SAFE (deadlock possible)" : "SYSTEM IS IN A SAFE STATE\nSafe sequence: " + safeSequence.stream().map(processIndex -> "P" + processIndex).reduce((first, second) -> first + " -> " + second).orElse(""));
        else result.append(hasBlockedProcess ? "DEADLOCK DETECTED\nDeadlocked processes: " : "NO DEADLOCK - all processes can complete");
        if (!safetyCheck && hasBlockedProcess) for (int processIndex = 0; processIndex < processCount; processIndex++) if (!processFinished[processIndex]) result.append("P").append(processIndex).append(" ");
        return result.toString();
    }

    private String replacementResult(int algorithm, int frameCount, int[] refs) {
        int[] frames = new int[frameCount], last = new int[frameCount];
        Arrays.fill(frames, -1); Arrays.fill(last, -1);
        int pointer = 0, hits = 0, faults = 0;
        StringBuilder out = new StringBuilder("PAGE REPLACEMENT RESULT\n\nReference  Status  Frames\n");
        for (int i = 0; i < refs.length; i++) {
            int page = refs[i], victim = -1;
            for (int j = 0; j < frameCount; j++) if (frames[j] == page) victim = j;
            String status = "HIT";
            if (victim < 0) {
                faults++; status = "FAULT";
                for (int j = 0; j < frameCount; j++) if (frames[j] < 0) { victim = j; break; }
                if (victim < 0) {
                    if (algorithm == 0) victim = pointer++ % frameCount;
                    else if (algorithm == 1) {
                        victim = 0;
                        for (int j = 1; j < frameCount; j++) if (last[j] < last[victim]) victim = j;
                    } else {
                        int farthest = -1;
                        for (int j = 0; j < frameCount; j++) {
                            int next = refs.length;
                            for (int k = i + 1; k < refs.length; k++) if (refs[k] == frames[j]) { next = k; break; }
                            if (next > farthest) { farthest = next; victim = j; }
                        }
                    }
                }
            } else hits++;
            frames[victim] = page; last[victim] = i;
            out.append(String.format("%-10d %-7s %s%n", page, status, Arrays.toString(frames)));
        }
        return out.append(String.format("%nPage faults = %d%nPage hits   = %d%nHit ratio   = %.2f%%", faults, hits, hits * 100.0 / refs.length)).toString();
    }

    private String fragmentationResult(int strategy, int[] blocks, int[] processes) {
        boolean[] used = new boolean[blocks.length]; int internal = 0, unallocated = 0; StringBuilder out = new StringBuilder("FRAGMENTATION RESULT\n\n");
        for (int i = 0; i < processes.length; i++) { int chosen = -1; for (int j = 0; j < blocks.length; j++) if (!used[j] && blocks[j] >= processes[i] && (chosen < 0 || strategy == 1 && blocks[j] < blocks[chosen] || strategy == 2 && blocks[j] > blocks[chosen])) chosen = j; if (chosen < 0) { unallocated++; out.append("P").append(i).append(" -> NOT ALLOCATED\n"); } else { used[chosen] = true; int waste = blocks[chosen] - processes[i]; internal += waste; out.append("P").append(i).append(" -> Block ").append(chosen).append(" (waste ").append(waste).append(")\n"); } }
        return out.append("\nTotal internal fragmentation = ").append(internal).append("\nProcesses not allocated = ").append(unallocated).toString();
    }

    private String diskResult(int algorithm, int direction, int head, int max, int[] requests) {
        ArrayList<Integer> order = new ArrayList<>(); int current = head; ArrayList<Integer> remaining = new ArrayList<>(); for (int r : requests) remaining.add(r);
        if (algorithm == 0) order.addAll(remaining);
        else if (algorithm == 1) while (!remaining.isEmpty()) {
            final int position = current;
            remaining.sort(Comparator.comparingInt(a -> Math.abs(a - position)));
            current = remaining.remove(0); order.add(current);
        } else {
            Collections.sort(remaining); ArrayList<Integer> high = new ArrayList<>(), low = new ArrayList<>();
            for (int r : remaining) if (r >= head) high.add(r); else low.add(r);
            if (direction == 0) { order.addAll(high); order.add(max); Collections.reverse(low); order.addAll(low); }
            else { Collections.reverse(low); order.addAll(low); order.add(0); order.addAll(high); }
        }
        int movement = 0; current = head; for (int v : order) { movement += Math.abs(v - current); current = v; } return "DISK SCHEDULING RESULT\n\nService order: " + head + " -> " + order.toString().replace("[", "").replace("]", "") + "\nTotal head movement = " + movement + " cylinders";
    }

    private String multiprogrammingResult(int memory, int quantum, int[] bursts, int[] needs) throws Exception {
        if (bursts.length != needs.length) throw new Exception("Burst and memory lists must have equal length.");
        int used = 0, time = 0, peak = 0; int[] remaining = bursts.clone(); boolean[] admitted = new boolean[bursts.length], done = new boolean[bursts.length]; ArrayDeque<Integer> queue = new ArrayDeque<>(); StringBuilder out = new StringBuilder("MULTIPROGRAMMING RESULT\n\n");
        while (hasIncomplete(done)) {
            for (int i = 0; i < bursts.length; i++) if (!admitted[i] && used + needs[i] <= memory) {
                admitted[i] = true; used += needs[i]; queue.add(i);
                out.append("Time ").append(time).append(": P").append(i).append(" admitted\n");
            }
            int inMemory = 0;
            for (int i = 0; i < bursts.length; i++) if (admitted[i] && !done[i]) inMemory++;
            peak = Math.max(peak, inMemory);
            if (queue.isEmpty()) { time++; continue; }
            int i = queue.remove(), run = Math.min(quantum, remaining[i]);
            remaining[i] -= run; time += run;
            if (remaining[i] == 0) {
                done[i] = true; used -= needs[i];
                out.append("Time ").append(time).append(": P").append(i).append(" finished\n");
            } else queue.add(i);
        }
        return out.append("\nPeak degree of multiprogramming = ").append(peak).append(" processes").toString();
    }

    private boolean hasIncomplete(boolean[] done) {
        for (boolean value : done) if (!value) return true;
        return false;
    }

    private JPanel card() { JPanel p = new JPanel(); p.setBackground(WHITE); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(22, 22, 22, 22))); return p; }
    private JLabel label(String text) { JLabel l = new JLabel(text); l.setForeground(INK); l.setFont(new Font("SansSerif", Font.BOLD, 12)); l.setBorder(new EmptyBorder(8, 0, 4, 0)); return l; }
    private JTextField input(String text) { JTextField f = new JTextField(text); style(f); return f; }
    private JTextArea area(String text) { JTextArea a = new JTextArea(text, 4, 20); style(a); return a; }
    private void style(JComponent c) { c.setForeground(INK); c.setBackground(new Color(250, 251, 253)); if (c instanceof JTextComponent) ((JTextComponent) c).setCaretColor(INK); c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 8, 7, 8))); }
    private JComboBox<String> combo(String... values) { JComboBox<String> c = new JComboBox<>(values); style(c); return c; }
    private void add(JPanel p, String name, JComponent component) { p.add(label(name)); p.add(component); }
    private JButton button(String text) { JButton b = new JButton(text); b.setForeground(WHITE); b.setBackground(BLUE); b.setFont(new Font("SansSerif", Font.BOLD, 12)); b.setFocusPainted(false); b.setBorder(new EmptyBorder(10, 14, 10, 14)); return b; }
    private int[] parseNumbers(String text) { return Arrays.stream(text.trim().split("[,\\s]+")).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray(); }
    private int[][] parseMatrix(String text) { return Arrays.stream(text.trim().split("\\n|;")).filter(s -> !s.trim().isEmpty()).map(this::parseNumbers).toArray(int[][]::new); }
    private void showModule(int moduleIndex) { moduleTitle.setText(MODULES[moduleIndex]); moduleCardLayout.show(moduleForms, "m" + moduleIndex); resultOutput.setText("Fillout the inputs and then run the simulation."); }
    private void showError(Exception exception) { resultOutput.setText("INPUT ERROR\n\n" + exception.getMessage()); }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new JavaApplication().setVisible(true)); }
}

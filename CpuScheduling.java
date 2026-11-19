import java.util.ArrayDeque;

public final class CpuScheduling {
    private CpuScheduling() {}

    public static String simulate(int algorithmChoice, int processCount, int timeQuantum, int[][] processRows) throws Exception {
        if (processRows.length != processCount || processCount < 1 || processCount > 8) throw new Exception("Provide one row per process.");
        int[] arrivalTimes = new int[processCount], burstTimes = new int[processCount], priorities = new int[processCount];
        int[] remainingTimes = new int[processCount], completionTimes = new int[processCount];
        for (int processIndex = 0; processIndex < processCount; processIndex++) {
            if (processRows[processIndex].length < 2) throw new Exception("Each process needs arrival and burst.");
            arrivalTimes[processIndex] = processRows[processIndex][0];
            burstTimes[processIndex] = remainingTimes[processIndex] = processRows[processIndex][1];
            priorities[processIndex] = processRows[processIndex].length > 2 ? processRows[processIndex][2] : processIndex;
        }
        int currentTime = 0, completedProcesses = 0;
        ArrayDeque<Integer> readyQueue = new ArrayDeque<>();
        StringBuilder calculation = new StringBuilder("CALCULATION WALKTHROUGH\n");
        int stepNumber = 1;
        while (completedProcesses < processCount) {
            if (algorithmChoice == 4) {
                for (int processIndex = 0; processIndex < processCount; processIndex++)
                    if (remainingTimes[processIndex] > 0 && arrivalTimes[processIndex] <= currentTime && !readyQueue.contains(processIndex)) readyQueue.add(processIndex);
                if (readyQueue.isEmpty()) {
                    calculation.append(String.format("Step %d: t=%d, no arrived process; advance to t=%d.%n", stepNumber++, currentTime, currentTime + 1));
                    currentTime++;
                    continue;
                }
                int processIndex = readyQueue.remove();
                int startTime = currentTime;
                int executionTime = Math.min(timeQuantum, remainingTimes[processIndex]);
                currentTime += executionTime; remainingTimes[processIndex] -= executionTime;
                calculation.append(String.format("Step %d: run P%d from t=%d to t=%d (min(quantum %d, remaining %d) = %d).%n",
                    stepNumber++, processIndex, startTime, currentTime, timeQuantum,
                    remainingTimes[processIndex] + executionTime, executionTime));
                for (int otherProcessIndex = 0; otherProcessIndex < processCount; otherProcessIndex++)
                    if (remainingTimes[otherProcessIndex] > 0 && arrivalTimes[otherProcessIndex] <= currentTime && !readyQueue.contains(otherProcessIndex) && otherProcessIndex != processIndex) readyQueue.add(otherProcessIndex);
                if (remainingTimes[processIndex] > 0) readyQueue.add(processIndex);
                else { completionTimes[processIndex] = currentTime; completedProcesses++; }
            } else {
                int selectedProcess = -1;
                for (int processIndex = 0; processIndex < processCount; processIndex++)
                    if (remainingTimes[processIndex] > 0 && arrivalTimes[processIndex] <= currentTime &&
                        (selectedProcess < 0 || algorithmChoice == 1 && burstTimes[processIndex] < burstTimes[selectedProcess] ||
                         algorithmChoice == 2 && priorities[processIndex] < priorities[selectedProcess] ||
                         algorithmChoice == 3 && remainingTimes[processIndex] < remainingTimes[selectedProcess] ||
                         algorithmChoice == 0 && arrivalTimes[processIndex] < arrivalTimes[selectedProcess])) selectedProcess = processIndex;
                if (selectedProcess < 0) {
                    calculation.append(String.format("Step %d: t=%d, no ready process; advance to t=%d.%n", stepNumber++, currentTime, currentTime + 1));
                    currentTime++;
                    continue;
                }
                int startTime = currentTime;
                currentTime++; remainingTimes[selectedProcess]--;
                if (algorithmChoice != 3) {
                    int remainingBeforeFinish = remainingTimes[selectedProcess];
                    currentTime += remainingTimes[selectedProcess]; remainingTimes[selectedProcess] = 0;
                    calculation.append(String.format("Step %d: choose P%d at t=%d; run for %d units, so completion = %d.%n",
                        stepNumber++, selectedProcess, startTime, remainingBeforeFinish + 1, currentTime));
                } else {
                    calculation.append(String.format("Step %d: choose shortest remaining P%d for t=%d to t=%d; remaining = %d.%n",
                        stepNumber++, selectedProcess, startTime, currentTime, remainingTimes[selectedProcess]));
                }
                if (remainingTimes[selectedProcess] == 0) { completionTimes[selectedProcess] = currentTime; completedProcesses++; }
            }
        }
        StringBuilder result = new StringBuilder("CPU SCHEDULING RESULT\n\n");
        result.append(calculation).append("\n");
        result.append("FORMULAS: turnaround = completion - arrival; waiting = turnaround - burst.\n\n");
        result.append("Process  Arrival  Burst  Completion  Turnaround  Waiting\n");
        double totalTurnaround = 0, totalWaiting = 0;
        for (int processIndex = 0; processIndex < processCount; processIndex++) {
            int turnaroundTime = completionTimes[processIndex] - arrivalTimes[processIndex];
            int waitingTime = turnaroundTime - burstTimes[processIndex];
            totalTurnaround += turnaroundTime; totalWaiting += waitingTime;
            result.append(String.format("P%-7d %-8d %-6d %-11d %-11d %d%n", processIndex, arrivalTimes[processIndex], burstTimes[processIndex], completionTimes[processIndex], turnaroundTime, waitingTime));
        }
        return result.append(String.format("%nAverage turnaround time = %.2f%nAverage waiting time    = %.2f", totalTurnaround / processCount, totalWaiting / processCount)).toString();
    }
}

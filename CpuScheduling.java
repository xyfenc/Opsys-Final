import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;

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
        while (completedProcesses < processCount) {
            if (algorithmChoice == 4) {
                for (int processIndex = 0; processIndex < processCount; processIndex++)
                    if (remainingTimes[processIndex] > 0 && arrivalTimes[processIndex] <= currentTime && !readyQueue.contains(processIndex)) readyQueue.add(processIndex);
                if (readyQueue.isEmpty()) { currentTime++; continue; }
                int processIndex = readyQueue.remove();
                int executionTime = Math.min(timeQuantum, remainingTimes[processIndex]);
                currentTime += executionTime; remainingTimes[processIndex] -= executionTime;
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
                if (selectedProcess < 0) { currentTime++; continue; }
                currentTime++; remainingTimes[selectedProcess]--;
                if (algorithmChoice != 3) { currentTime += remainingTimes[selectedProcess]; remainingTimes[selectedProcess] = 0; }
                if (remainingTimes[selectedProcess] == 0) { completionTimes[selectedProcess] = currentTime; completedProcesses++; }
            }
        }
        StringBuilder result = new StringBuilder("CPU SCHEDULING RESULT\n\n@TABLE|PROCESS TIMING|Process|Arrival|Burst|Completion|Turnaround|Waiting\n");
        Integer[] completionOrder = new Integer[processCount];
        for (int processIndex = 0; processIndex < processCount; processIndex++) completionOrder[processIndex] = processIndex;
        Arrays.sort(completionOrder, Comparator.comparingInt(processIndex -> completionTimes[processIndex]));
        double totalTurnaround = 0, totalWaiting = 0;
        for (int processIndex : completionOrder) {
            int turnaroundTime = completionTimes[processIndex] - arrivalTimes[processIndex];
            int waitingTime = turnaroundTime - burstTimes[processIndex];
            totalTurnaround += turnaroundTime; totalWaiting += waitingTime;
            result.append("@ROW|P").append(processIndex).append('|').append(arrivalTimes[processIndex]).append('|')
                .append(burstTimes[processIndex]).append('|').append(completionTimes[processIndex]).append('|')
                .append(turnaroundTime).append('|').append(waitingTime).append('\n');
        }
        result.append("@ENDTABLE\n\nCALCULATIONS\n");
        for (int processIndex : completionOrder) {
            int turnaroundTime = completionTimes[processIndex] - arrivalTimes[processIndex];
            int waitingTime = turnaroundTime - burstTimes[processIndex];
            result.append(String.format("P%d: Turnaround = Completion - Arrival -> %d - %d = %d; Waiting = Turnaround - Burst -> %d - %d = %d%n",
                processIndex, completionTimes[processIndex], arrivalTimes[processIndex], turnaroundTime,
                turnaroundTime, burstTimes[processIndex], waitingTime));
        }
        return result.append(String.format("%nAverage turnaround time = %.2f%nAverage waiting time = %.2f", totalTurnaround / processCount, totalWaiting / processCount)).toString();
    }
}

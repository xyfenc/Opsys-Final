import java.util.ArrayDeque;
import java.util.Locale;

public final class Multiprogramming {
    private Multiprogramming() {}

    public static String simulate(int totalMemory, int timeQuantum, int[] burstTimes, int[] memoryRequirements) throws Exception {
        if (totalMemory <= 0) throw new Exception("Total physical memory must be a positive integer.");
        if (timeQuantum <= 0) throw new Exception("Time quantum must be a positive integer.");
        if (burstTimes == null || memoryRequirements == null || burstTimes.length == 0)
            throw new Exception("Enter at least one process.");
        if (burstTimes.length != memoryRequirements.length)
            throw new Exception("The number of CPU burst times must match the number of memory requirements.");

        int processCount = burstTimes.length;
        int[] remainingTimes = burstTimes.clone();
        for (int processIndex = 0; processIndex < processCount; processIndex++) {
            if (burstTimes[processIndex] <= 0)
                throw new Exception("CPU burst times must be positive integers.");
            if (memoryRequirements[processIndex] <= 0)
                throw new Exception("Memory requirements must be positive integers.");
            if (memoryRequirements[processIndex] > totalMemory)
                throw new Exception("P" + (processIndex + 1) + " requires more memory than the total physical memory.");
        }

        ArrayDeque<Integer> readyQueue = new ArrayDeque<>();
        ArrayDeque<Integer> waitingQueue = new ArrayDeque<>();
        for (int processIndex = 0; processIndex < processCount; processIndex++) waitingQueue.addLast(processIndex);

        StringBuilder result = new StringBuilder("MULTIPROGRAMMING RESULT\n\nRUN CHECK\n");
        result.append("Total physical memory = ").append(totalMemory)
            .append("\nTime quantum = ").append(timeQuantum)
            .append("\nProcesses = ").append(processCount).append("\n\n");

        int memoryInUse = 0;
        int completedProcesses = 0;
        long currentTime = 0;
        long busyTime = 0;
        long memoryTime = 0;
        int peakMemoryUse = 0;

        int admitted = admitProcesses(waitingQueue, readyQueue, memoryRequirements, totalMemory, memoryInUse);
        for (int processIndex = 0; processIndex < processCount; processIndex++) {
            result.append("P").append(processIndex + 1).append(": burst ")
                .append(burstTimes[processIndex]).append(", memory ")
                .append(memoryRequirements[processIndex]).append(" -> ")
                .append(readyQueue.contains(processIndex) ? "ADMITTED" : "WAITING").append("\n");
        }
        memoryInUse = admitted;
        peakMemoryUse = memoryInUse;
        result.append("\nInitial memory in use = ").append(memoryInUse).append(" / ").append(totalMemory)
            .append("\nReady queue = ").append(formatQueue(readyQueue))
            .append("\nWaiting queue = ").append(formatQueue(waitingQueue)).append("\n\n");

        while (completedProcesses < processCount) {
            if (readyQueue.isEmpty()) {
                int newlyAdmitted = admitProcesses(waitingQueue, readyQueue, memoryRequirements, totalMemory, memoryInUse);
                memoryInUse += newlyAdmitted;
                if (readyQueue.isEmpty()) {
                    throw new Exception("No waiting process can be admitted with the currently available memory.");
                }
            }

            int processIndex = readyQueue.removeFirst();
            int executionTime = Math.min(timeQuantum, remainingTimes[processIndex]);
            int memoryDuringExecution = memoryInUse;
            result.append("Time ").append(currentTime).append(" - ").append(currentTime + executionTime)
                .append(": P").append(processIndex + 1).append(" runs for ").append(executionTime)
                .append(" unit(s); memory in use = ").append(memoryDuringExecution).append(" / ")
                .append(totalMemory).append("\n");

            currentTime += executionTime;
            busyTime += executionTime;
            memoryTime += (long) memoryDuringExecution * executionTime;
            remainingTimes[processIndex] -= executionTime;
            if (remainingTimes[processIndex] == 0) {
                memoryInUse -= memoryRequirements[processIndex];
                completedProcesses++;
                result.append("P").append(processIndex + 1).append(" completes; memory released = ")
                    .append(memoryRequirements[processIndex]).append("\n");
            } else {
                readyQueue.addLast(processIndex);
                result.append("P").append(processIndex + 1).append(" has ")
                    .append(remainingTimes[processIndex]).append(" burst unit(s) remaining; moved to the back of the ready queue.\n");
            }

            int newlyAdmitted = admitProcesses(waitingQueue, readyQueue, memoryRequirements, totalMemory, memoryInUse);
            memoryInUse += newlyAdmitted;
            peakMemoryUse = Math.max(peakMemoryUse, memoryInUse);
            if (newlyAdmitted > 0) result.append("New process(es) admitted; memory in use = ")
                .append(memoryInUse).append(" / ").append(totalMemory).append("\n");
            result.append("Ready queue = ").append(formatQueue(readyQueue))
                .append("; waiting queue = ").append(formatQueue(waitingQueue)).append("\n\n");
        }

        double cpuUtilization = currentTime == 0 ? 0 : 100.0 * busyTime / currentTime;
        double averageMemoryUtilization = currentTime == 0 ? 0 : 100.0 * memoryTime / ((double) totalMemory * currentTime);
        double peakMemoryUtilization = 100.0 * peakMemoryUse / totalMemory;
        result.append("FINAL SUMMARY\n")
            .append("All processes completed = ").append(completedProcesses).append(" / ").append(processCount).append("\n")
            .append("Total elapsed time = ").append(currentTime).append("\n")
            .append("CPU utilization = (busy time ").append(busyTime).append(" / elapsed time ").append(currentTime).append(") * 100\n")
            .append("Average memory utilization = (memory-time ").append(memoryTime).append(" / (memory ")
                .append(totalMemory).append(" * elapsed time ").append(currentTime).append(")) * 100\n")
            .append(String.format(Locale.ROOT, "CPU utilization = %.2f%%%n", cpuUtilization))
            .append(String.format(Locale.ROOT, "Average memory utilization = %.2f%%%n", averageMemoryUtilization))
            .append(String.format(Locale.ROOT, "Peak memory utilization = %.2f%%", peakMemoryUtilization));
        return result.toString();
    }

    private static int admitProcesses(ArrayDeque<Integer> waitingQueue, ArrayDeque<Integer> readyQueue,
                                      int[] memoryRequirements, int totalMemory, int memoryInUse) {
        int admittedMemory = 0;
        int processesToCheck = waitingQueue.size();
        for (int i = 0; i < processesToCheck; i++) {
            int processIndex = waitingQueue.removeFirst();
            int availableMemory = totalMemory - memoryInUse - admittedMemory;
            if (memoryRequirements[processIndex] <= availableMemory) {
                readyQueue.addLast(processIndex);
                admittedMemory += memoryRequirements[processIndex];
            } else {
                waitingQueue.addLast(processIndex);
            }
        }
        return admittedMemory;
    }

    private static String formatQueue(ArrayDeque<Integer> queue) {
        if (queue.isEmpty()) return "[]";
        StringBuilder formatted = new StringBuilder("[");
        for (int processIndex : queue) {
            if (formatted.length() > 1) formatted.append(", ");
            formatted.append("P").append(processIndex + 1);
        }
        return formatted.append("]").toString();
    }
}

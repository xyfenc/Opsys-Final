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

        StringBuilder result = new StringBuilder("MULTIPROGRAMMING RESULT\n\n");
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
        result.append("@TABLE|PROCESS MEMORY ADMISSION|Process|CPU burst|Memory required|Admission\n");
        StringBuilder admissionCalculations = new StringBuilder("CALCULATIONS\n");
        int memoryDuringAdmission = 0;
        for (int processIndex = 0; processIndex < processCount; processIndex++) {
            boolean isAdmitted = readyQueue.contains(processIndex);
            int availableForProcess = totalMemory - memoryDuringAdmission;
            result.append("@ROW|P").append(processIndex + 1).append('|').append(burstTimes[processIndex]).append('|')
                .append(memoryRequirements[processIndex]).append('|').append(isAdmitted ? "ADMITTED" : "WAITING").append('\n');
            admissionCalculations.append("P").append(processIndex + 1).append(": Required memory <= available memory -> ")
                .append(memoryRequirements[processIndex]).append(" <= ").append(availableForProcess).append(" = ").append(isAdmitted).append('\n');
            if (isAdmitted) memoryDuringAdmission += memoryRequirements[processIndex];
            result.append("P").append(processIndex + 1).append(": burst ")
                .append(burstTimes[processIndex]).append(", memory ")
                .append(memoryRequirements[processIndex]).append(" -> ")
                .append(isAdmitted ? "ADMITTED" : "WAITING").append("\n");
        }
        result.append("@ENDTABLE\n\n").append(admissionCalculations).append('\n');
        memoryInUse = admitted;
        peakMemoryUse = memoryInUse;
        StringBuilder executionRows = new StringBuilder();
        StringBuilder executionCalculations = new StringBuilder("CALCULATIONS\n");
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
            long startTime = currentTime;
            int remainingBefore = remainingTimes[processIndex];
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
            executionRows.append("@ROW|").append(startTime).append('|').append(currentTime).append("|P")
                .append(processIndex + 1).append('|').append(timeQuantum).append('|').append(remainingBefore).append('|')
                .append(executionTime).append('|').append(remainingTimes[processIndex]).append('|')
                .append(memoryDuringExecution).append(" / ").append(totalMemory).append('\n');
            executionCalculations.append("P").append(processIndex + 1).append(": CPU time slice = min(Time quantum, remaining burst) -> min(")
                .append(timeQuantum).append(", ").append(remainingBefore).append(") = ").append(executionTime)
                .append("; remaining burst = ").append(remainingBefore).append(" - ").append(executionTime).append(" = ")
                .append(remainingTimes[processIndex]).append('\n');
            if (remainingTimes[processIndex] == 0) {
                memoryInUse -= memoryRequirements[processIndex];
                completedProcesses++;
                executionCalculations.append("Memory released = ").append(memoryDuringExecution).append(" - ")
                    .append(memoryRequirements[processIndex]).append(" = ").append(memoryInUse).append('\n');
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
        result.append("@TABLE|CPU TIME SLICES|Start|End|Process|Quantum|Remaining before|Ran|Remaining after|Memory in use\n")
            .append(executionRows).append("@ENDTABLE\n\n").append(executionCalculations)
            .append("CPU utilization = busy time / elapsed time -> ").append(busyTime).append(" / ").append(currentTime).append(" = ").append(String.format(Locale.ROOT, "%.2f%%", cpuUtilization)).append('\n')
            .append("Average memory utilization = memory-time / (total memory x elapsed time) -> ").append(memoryTime).append(" / (").append(totalMemory).append(" x ").append(currentTime).append(") = ").append(String.format(Locale.ROOT, "%.2f%%", averageMemoryUtilization)).append('\n')
            .append("Peak memory utilization = peak memory / total memory -> ").append(peakMemoryUse).append(" / ").append(totalMemory).append(" = ").append(String.format(Locale.ROOT, "%.2f%%", peakMemoryUtilization)).append("\n\nFINAL SUMMARY\n")
            .append("All processes completed = ").append(completedProcesses).append(" / ").append(processCount).append("\n")
            .append("Total elapsed time = ").append(currentTime).append("\n")
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

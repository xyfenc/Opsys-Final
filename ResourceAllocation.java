import java.util.ArrayList;
import java.util.Arrays;

public final class ResourceAllocation {
    private ResourceAllocation() {}

    public static String analyze(boolean safetyCheck, int processCount, int resourceCount, int[][] allocationMatrix, int[][] requestOrMaximumMatrix, int[] availableResources) throws Exception {
        if (processCount < 1 || resourceCount < 1) throw new Exception("Process and resource counts must be positive.");
        if (allocationMatrix.length != processCount || requestOrMaximumMatrix.length != processCount || availableResources.length != resourceCount) throw new Exception("Matrix dimensions do not match.");
        int[] remainingResources = availableResources.clone(); boolean[] processFinished = new boolean[processCount];
        ArrayList<Integer> sequence = new ArrayList<>(); boolean madeProgress = true;
        StringBuilder trace = new StringBuilder("CALCULATIONS\n");
        while (madeProgress) {
            madeProgress = false;
            for (int processIndex = 0; processIndex < processCount; processIndex++) if (!processFinished[processIndex]) {
                boolean canFinish = true;
                int[] workBeforeCheck = remainingResources.clone();
                int[] neededResources = new int[resourceCount];
                trace.append("P").append(processIndex).append(" check: ");
                for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) {
                    int allocation = allocationMatrix[processIndex][resourceIndex];
                    int requested = requestOrMaximumMatrix[processIndex][resourceIndex];
                    if (allocation < 0 || requested < 0 || availableResources[resourceIndex] < 0) throw new Exception("Resource values must be non-negative.");
                    if (safetyCheck && requested < allocation) throw new Exception("Maximum demand cannot be less than the current allocation.");
                    int required = safetyCheck ? requested - allocation : requested;
                    neededResources[resourceIndex] = required;
                    if (resourceIndex > 0) trace.append("; ");
                    if (safetyCheck) trace.append("Need = Max - Allocation -> ").append(requested).append(" - ").append(allocation).append(" = ").append(required);
                    else trace.append("Request <= Work -> ").append(required).append(" <= ").append(remainingResources[resourceIndex]);
                    if (required > remainingResources[resourceIndex]) canFinish = false;
                }
                trace.append(safetyCheck ? "; Need <= Work -> " : "; Request <= Work -> ")
                    .append(Arrays.toString(neededResources)).append(" <= ").append(Arrays.toString(workBeforeCheck))
                    .append(" = ").append(canFinish);
                if (canFinish) {
                    for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) remainingResources[resourceIndex] += allocationMatrix[processIndex][resourceIndex];
                    trace.append("; Work = Work + Allocation -> ").append(Arrays.toString(workBeforeCheck))
                        .append(" + ").append(Arrays.toString(allocationMatrix[processIndex])).append(" = ")
                        .append(Arrays.toString(remainingResources));
                    processFinished[processIndex] = true; sequence.add(processIndex); madeProgress = true;
                } else trace.append("; cannot finish with current Work");
                trace.append('\n');
            }
        }
        boolean hasBlockedProcess = false; for (boolean finished : processFinished) if (!finished) hasBlockedProcess = true;
        StringBuilder result = new StringBuilder(safetyCheck ? "BANKER'S ALGORITHM RESULT\n\n" : "DEADLOCK DETECTION RESULT\n\n");
        result.append(safetyCheck ? "@TABLE|BANKER RESOURCE TABLE|Process" : "@TABLE|DEADLOCK RESOURCE TABLE|Process");
        for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) result.append("|Alloc R").append(resourceIndex);
        for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) result.append(safetyCheck ? "|Max R" : "|Request R").append(resourceIndex);
        if (safetyCheck) for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) result.append("|Need R").append(resourceIndex);
        result.append('\n');
        for (int processIndex = 0; processIndex < processCount; processIndex++) {
            result.append("@ROW|P").append(processIndex);
            for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) result.append('|').append(allocationMatrix[processIndex][resourceIndex]);
            for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) result.append('|').append(requestOrMaximumMatrix[processIndex][resourceIndex]);
            if (safetyCheck) for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) result.append('|')
                .append(requestOrMaximumMatrix[processIndex][resourceIndex] - allocationMatrix[processIndex][resourceIndex]);
            result.append('\n');
        }
        result.append("@ENDTABLE\n@TABLE|AVAILABLE RESOURCES|Resource|Available\n");
        for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) result.append("@ROW|R").append(resourceIndex).append('|').append(availableResources[resourceIndex]).append('\n');
        result.append("@ENDTABLE\n\n");
        if (safetyCheck) result.append(hasBlockedProcess ? "SYSTEM IS NOT SAFE (deadlock possible)" : "SYSTEM IS IN A SAFE STATE\nSafe sequence: " + sequence.stream().map(i -> "P" + i).reduce((a, b) -> a + " -> " + b).orElse(""));
        else result.append(hasBlockedProcess ? "DEADLOCK DETECTED\nDeadlocked processes: " : "NO DEADLOCK - all processes can complete");
        if (!safetyCheck && hasBlockedProcess) for (int processIndex = 0; processIndex < processCount; processIndex++) if (!processFinished[processIndex]) result.append("P").append(processIndex).append(" ");
        result.append("\n\n").append(trace);
        return result.toString();
    }
}

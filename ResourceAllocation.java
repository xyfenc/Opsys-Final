import java.util.ArrayList;

public final class ResourceAllocation {
    private ResourceAllocation() {}

    public static String analyze(boolean safetyCheck, int processCount, int resourceCount, int[][] allocationMatrix, int[][] requestOrMaximumMatrix, int[] availableResources) throws Exception {
        if (allocationMatrix.length != processCount || requestOrMaximumMatrix.length != processCount || availableResources.length != resourceCount) throw new Exception("Matrix dimensions do not match.");
        int[] remainingResources = availableResources.clone(); boolean[] processFinished = new boolean[processCount];
        ArrayList<Integer> sequence = new ArrayList<>(); boolean madeProgress = true;
        StringBuilder calculation = new StringBuilder("CALCULATION WALKTHROUGH\nInitial available = ")
            .append(java.util.Arrays.toString(remainingResources)).append("\n");
        while (madeProgress) {
            madeProgress = false;
            for (int processIndex = 0; processIndex < processCount; processIndex++) if (!processFinished[processIndex]) {
                boolean canFinish = true;
                int[] requiredResources = new int[resourceCount];
                for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) {
                    int required = safetyCheck ? Math.max(0, requestOrMaximumMatrix[processIndex][resourceIndex] - allocationMatrix[processIndex][resourceIndex]) : requestOrMaximumMatrix[processIndex][resourceIndex];
                    requiredResources[resourceIndex] = required;
                    if (required > remainingResources[resourceIndex]) canFinish = false;
                }
                calculation.append("Check P").append(processIndex).append(": need ")
                    .append(java.util.Arrays.toString(requiredResources)).append(" <= available ")
                    .append(java.util.Arrays.toString(remainingResources)).append(" -> ")
                    .append(canFinish ? "can finish" : "wait").append("\n");
                if (canFinish) {
                    for (int resourceIndex = 0; resourceIndex < resourceCount; resourceIndex++) remainingResources[resourceIndex] += allocationMatrix[processIndex][resourceIndex];
                    calculation.append("P").append(processIndex).append(" finishes and releases its allocation; available = ")
                        .append(java.util.Arrays.toString(remainingResources)).append("\n");
                    processFinished[processIndex] = true; sequence.add(processIndex); madeProgress = true;
                }
            }
        }
        boolean hasBlockedProcess = false; for (boolean finished : processFinished) if (!finished) hasBlockedProcess = true;
        StringBuilder result = new StringBuilder(safetyCheck ? "BANKER'S ALGORITHM RESULT\n\n" : "DEADLOCK DETECTION RESULT\n\n");
        result.append(calculation).append("\n");
        if (safetyCheck) result.append(hasBlockedProcess ? "SYSTEM IS NOT SAFE (deadlock possible)" : "SYSTEM IS IN A SAFE STATE\nSafe sequence: " + sequence.stream().map(i -> "P" + i).reduce((a, b) -> a + " -> " + b).orElse(""));
        else result.append(hasBlockedProcess ? "DEADLOCK DETECTED\nDeadlocked processes: " : "NO DEADLOCK - all processes can complete");
        if (!safetyCheck && hasBlockedProcess) for (int processIndex = 0; processIndex < processCount; processIndex++) if (!processFinished[processIndex]) result.append("P").append(processIndex).append(" ");
        return result.toString();
    }
}

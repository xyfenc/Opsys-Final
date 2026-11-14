import java.util.ArrayDeque;

public final class Multiprogramming {
    private Multiprogramming() {}

    public static String simulate(int totalMemory, int timeQuantum, int[] burstTimes, int[] memoryRequirements) throws Exception {
        return "MULTIPROGRAMMING RESULT\n\nTODO: memory admission and round-robin scheduling";
    }

    private static boolean hasIncompleteProcesses(boolean[] finished) {
        for (boolean processFinished : finished) if (!processFinished) return true;
        return false;
    }
}

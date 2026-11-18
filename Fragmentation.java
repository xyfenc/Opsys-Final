public final class Fragmentation {
    private Fragmentation() {}

    public static String allocate(int strategyChoice, int[] memoryBlocks, int[] processSizes) {
       boolean[] blockUsed = new boolean[memoryBlocks.length]; int internalFragmentation = 0, unallocatedProcesses = 0;
        StringBuilder result = new StringBuilder("FRAGMENTATION RESULT\n\n");

        for (int processIndex = 0; processIndex < processSizes.length; processIndex++) {
            int selectedBlock = -1;

            for (int blockIndex = 0; blockIndex < memoryBlocks.length; blockIndex++)
                if (!blockUsed[blockIndex] && memoryBlocks[blockIndex] >= processSizes[processIndex] &&
                    (selectedBlock < 0 || strategyChoice == 1 && memoryBlocks[blockIndex] < memoryBlocks[selectedBlock] || strategyChoice == 2 && memoryBlocks[blockIndex] > memoryBlocks[selectedBlock])) selectedBlock = blockIndex;
                    
            if (selectedBlock < 0) { unallocatedProcesses++; result.append("P").append(processIndex).append(" -> NOT ALLOCATED\n"); }
            else { blockUsed[selectedBlock] = true; int waste = memoryBlocks[selectedBlock] - processSizes[processIndex]; internalFragmentation += waste; result.append("P").append(processIndex).append(" -> Block ").append(selectedBlock).append(" (waste ").append(waste).append(")\n"); }
        }
            return result.append("\nTotal internal fragmentation = ").append(internalFragmentation).append("\nProcesses not allocated = ").append(unallocatedProcesses).toString();
    }
}
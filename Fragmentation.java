public final class Fragmentation {
    private Fragmentation() {}

    public static String allocate(int strategyChoice, int[] memoryBlocks, int[] processSizes) throws Exception {
        if (strategyChoice < 0 || strategyChoice > 2) throw new Exception("Select a valid allocation strategy.");
        if (memoryBlocks == null || memoryBlocks.length == 0) throw new Exception("Enter at least one memory block.");
        if (processSizes == null || processSizes.length == 0) throw new Exception("Enter at least one process size.");
        for (int blockSize : memoryBlocks)
            if (blockSize <= 0) throw new Exception("Memory block sizes must be positive integers.");
        for (int processSize : processSizes)
            if (processSize <= 0) throw new Exception("Process sizes must be positive integers.");

        String[] strategies = {"First Fit", "Best Fit", "Worst Fit"};
        boolean[] blockUsed = new boolean[memoryBlocks.length];
        int[] allocatedBlocks = new int[processSizes.length];
        java.util.Arrays.fill(allocatedBlocks, -1);
        String[] failureReasons = new String[processSizes.length];
        long internalFragmentation = 0;
        int unallocatedCount = 0;

        for (int processIndex = 0; processIndex < processSizes.length; processIndex++) {
            int processSize = processSizes[processIndex];
            int selectedBlock = -1;
            for (int blockIndex = 0; blockIndex < memoryBlocks.length; blockIndex++) {
                if (blockUsed[blockIndex] || memoryBlocks[blockIndex] < processSize) continue;
                if (selectedBlock < 0 ||
                    strategyChoice == 1 && memoryBlocks[blockIndex] < memoryBlocks[selectedBlock] ||
                    strategyChoice == 2 && memoryBlocks[blockIndex] > memoryBlocks[selectedBlock]) {
                    selectedBlock = blockIndex;
                }
            }

            if (selectedBlock < 0) {
                unallocatedCount++;
                long freeMemory = 0;
                int largestFreeBlock = 0;
                for (int blockIndex = 0; blockIndex < memoryBlocks.length; blockIndex++) {
                    if (!blockUsed[blockIndex]) {
                        freeMemory += memoryBlocks[blockIndex];
                        largestFreeBlock = Math.max(largestFreeBlock, memoryBlocks[blockIndex]);
                    }
                }
                failureReasons[processIndex] = freeMemory >= processSize
                    ? "external fragmentation: " + freeMemory + " total free units, but the largest free block is " + largestFreeBlock
                    : "insufficient free memory: " + freeMemory + " total free units remain";
            } else {
                blockUsed[selectedBlock] = true;
                allocatedBlocks[processIndex] = selectedBlock;
                internalFragmentation += (long) memoryBlocks[selectedBlock] - processSize;
            }
        }

        StringBuilder result = new StringBuilder("FRAGMENTATION RESULT\n\nMEMORY CHECK\n");
        result.append("Allocation strategy: ").append(strategies[strategyChoice]).append("\n\nAllocations:\n");
        for (int processIndex = 0; processIndex < processSizes.length; processIndex++) {
            int blockIndex = allocatedBlocks[processIndex];
            if (blockIndex < 0) {
                result.append("P").append(processIndex + 1).append(" (").append(processSizes[processIndex])
                    .append(") -> NOT ALLOCATED: ").append(failureReasons[processIndex]).append("\n");
            } else {
                int waste = memoryBlocks[blockIndex] - processSizes[processIndex];
                result.append("P").append(processIndex + 1).append(" (").append(processSizes[processIndex])
                    .append(") -> Block ").append(blockIndex + 1).append(" (size ")
                    .append(memoryBlocks[blockIndex]).append(", internal waste ").append(waste).append(")\n");
                result.append("  Calculation: ").append(memoryBlocks[blockIndex]).append(" - ")
                    .append(processSizes[processIndex]).append(" = ").append(waste).append(" free units.\n");
            }
        }

        result.append("\nRemaining free blocks:\n");
        boolean hasFreeBlocks = false;
        for (int blockIndex = 0; blockIndex < memoryBlocks.length; blockIndex++) {
            if (!blockUsed[blockIndex]) {
                result.append("Block ").append(blockIndex + 1).append(" (size ").append(memoryBlocks[blockIndex]).append(")\n");
                hasFreeBlocks = true;
            }
        }
        if (!hasFreeBlocks) result.append("None\n");
        return result.append("\nTotal internal fragmentation = sum of block waste = ").append(internalFragmentation)
            .append("\nProcesses not allocated = ").append(unallocatedCount).toString();
    }
}

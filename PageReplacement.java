import java.util.Arrays;

public final class PageReplacement {
    private PageReplacement() {}

    public static String simulate(int algorithmChoice, int frameCount, int[] references) {
        return "PAGE REPLACEMENT RESULT\n\nTODO: FIFO, LRU, and Optimal page replacement.";
     for (int referenceIndex = 0; referenceIndex < references.length; referenceIndex++) {
            int page = references[referenceIndex], replacementFrame = -1;
            for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) if (frames[frameIndex] == page) replacementFrame = frameIndex;
            String status = "HIT";
            if (replacementFrame < 0) {
                faultCount++; status = "FAULT";
                for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) if (frames[frameIndex] < 0) { replacementFrame = frameIndex; break; }
                if (replacementFrame < 0) {
                    if (algorithmChoice == 0) replacementFrame = fifoPointer++ % frameCount;
                    else if (algorithmChoice == 1) {
                        replacementFrame = 0;
                        for (int frameIndex = 1; frameIndex < frameCount; frameIndex++) if (lastUsedAt[frameIndex] < lastUsedAt[replacementFrame]) replacementFrame = frameIndex;
                    } else {
                        int farthestNextUse = -1;
                        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
                            int nextUse = references.length;
                            for (int futureIndex = referenceIndex + 1; futureIndex < references.length; futureIndex++) if (references[futureIndex] == frames[frameIndex]) { nextUse = futureIndex; break; }
                            if (nextUse > farthestNextUse) { farthestNextUse = nextUse; replacementFrame = frameIndex; }
                        }
                    }
                }
            } else hitCount++;
            frames[replacementFrame] = page; lastUsedAt[replacementFrame] = referenceIndex;
            result.append(String.format("%-10d %-7s %s%n", page, status, Arrays.toString(frames)));
        }
        return result.append(String.format("%nPage faults = %d%nPage hits   = %d%nHit ratio   = %.2f%%", faultCount, hitCount, hitCount * 100.0 / references.length)).toString();
    }
}
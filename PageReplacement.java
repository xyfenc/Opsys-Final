import java.util.Arrays;

public final class PageReplacement {
    private PageReplacement() {}

    public static String simulate(int algorithmChoice, int frameCount, int[] references) {
        int[] frames = new int[frameCount], lastUsedAt = new int[frameCount];
        Arrays.fill(frames, -1); Arrays.fill(lastUsedAt, -1);
        int fifoPointer = 0, hitCount = 0, faultCount = 0;
        StringBuilder result = new StringBuilder("PAGE REPLACEMENT RESULT\n\n");
        result.append("PAGE CHECK\n");
        result.append("For each reference, search the frames first: present = HIT; absent = FAULT and replace according to the selected policy.\n\n");
        result.append("Reference  Status  Frames\n");
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
            result.append("Step ").append(referenceIndex + 1).append(": reference ").append(page)
                .append(status.equals("HIT") ? " is already in a frame -> HIT." : " is absent -> FAULT; place it in frame ")
                .append(replacementFrame).append("\n");
            result.append(String.format("%-10d %-7s %s%n", page, status, Arrays.toString(frames)));
        }
        return result.append(String.format("%nPage faults = %d%nPage hits   = %d%nHit ratio = (%d / %d) * 100 = %.2f%%",
            faultCount, hitCount, hitCount, references.length, hitCount * 100.0 / references.length)).toString();
    }
}

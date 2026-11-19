import java.util.Arrays;

public final class PageReplacement {
    private PageReplacement() {}

    public static String simulate(int algorithmChoice, int frameCount, int[] references) {
        if (frameCount <= 0) throw new IllegalArgumentException("Frame count must be positive.");
        if (references == null || references.length == 0) throw new IllegalArgumentException("Enter at least one page reference.");
        int[] frames = new int[frameCount], lastUsedAt = new int[frameCount];
        Arrays.fill(frames, -1); Arrays.fill(lastUsedAt, -1);
        int fifoPointer = 0, hitCount = 0, faultCount = 0;
        StringBuilder result = new StringBuilder("PAGE REPLACEMENT RESULT\n\n@TABLE|PAGE FRAME TRACE|Step|Page|Status");
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) result.append("|Frame ").append(frameIndex + 1);
        result.append('\n');
        StringBuilder calculations = new StringBuilder("\nCALCULATIONS\n");
        for (int referenceIndex = 0; referenceIndex < references.length; referenceIndex++) {
            int page = references[referenceIndex], replacementFrame = -1;
            for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) if (frames[frameIndex] == page) replacementFrame = frameIndex;
            int[] framesBefore = frames.clone();
            int hitsBefore = hitCount, faultsBefore = faultCount;
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
            int replacedPage = frames[replacementFrame];
            frames[replacementFrame] = page; lastUsedAt[replacementFrame] = referenceIndex;
            result.append("@ROW|").append(referenceIndex + 1).append('|').append(page).append('|').append(status);
            for (int frame : frames) result.append('|').append(frame < 0 ? "Empty" : frame);
            result.append('\n');
            calculations.append("Reference check: ").append(page).append(" in ").append(Arrays.toString(framesBefore)).append(" -> ").append(status);
            if (status.equals("HIT")) calculations.append("; hits = ").append(hitsBefore).append(" + 1 = ").append(hitCount);
            else {
                calculations.append("; faults = ").append(faultsBefore).append(" + 1 = ").append(faultCount)
                    .append(replacedPage < 0 ? "; load into empty frame " : "; replace ").append(replacedPage < 0 ? replacementFrame + 1 : String.valueOf(replacedPage));
            }
            calculations.append('\n');
        }
        result.append("@ENDTABLE").append(calculations)
            .append("Page faults = total references - hits -> ").append(references.length).append(" - ").append(hitCount).append(" = ").append(faultCount).append('\n')
            .append("Hit ratio = hits / total references -> ").append(hitCount).append(" / ").append(references.length).append(" = ")
            .append(String.format("%.2f%%", hitCount * 100.0 / references.length));
        return result.append(String.format("%n%nPage faults = %d%nPage hits = %d%nHit ratio = %.2f%%", faultCount, hitCount, hitCount * 100.0 / references.length)).toString();
    }
}

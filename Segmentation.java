public final class Segmentation {
    private Segmentation() {}

    public static String translate(int[] bases, int[] limits, int segmentNumber, int offset) throws Exception {
        if (segmentNumber < 0 || segmentNumber >= bases.length || bases.length != limits.length) throw new Exception("Invalid segment data.");
        if (offset < 0) throw new Exception("Offset must be non-negative.");
        boolean valid = offset < limits[segmentNumber];
        String result = "SEGMENTATION RESULT\n\n@TABLE|SEGMENT TRANSLATION|Segment|Base|Limit|Offset|Physical address|Status\n"
            + "@ROW|" + segmentNumber + "|" + bases[segmentNumber] + "|" + limits[segmentNumber] + "|" + offset + "|"
            + (valid ? bases[segmentNumber] + offset : "Not available") + "|" + (valid ? "VALID" : "OUT OF RANGE")
            + "\n@ENDTABLE\n\nCALCULATIONS\nOffset < Limit -> " + offset + " < " + limits[segmentNumber] + " = " + valid;
        if (valid) result += "\nPhysical address = Base + Offset -> " + bases[segmentNumber] + " + " + offset + " = " + (bases[segmentNumber] + offset);
        else result += "\nOffset is outside the segment; translation is rejected.";
        return result;
    }
}

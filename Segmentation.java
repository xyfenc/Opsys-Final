public final class Segmentation {
    private Segmentation() {}

    public static String translate(int[] bases, int[] limits, int segmentNumber, int offset) throws Exception {
        if (segmentNumber < 0 || segmentNumber >= bases.length || bases.length != limits.length) throw new Exception("Invalid segment data.");
        return "SEGMENTATION RESULT\n\nSegment            : " + segmentNumber + "\nBase               : " + bases[segmentNumber] + "\nLimit              : " + limits[segmentNumber] + "\nOffset             : " + offset +
            "\n\nCALCULATION WALKTHROUGH\nCheck: offset " + offset + (offset >= limits[segmentNumber] ? " >= " : " < ") + "limit " + limits[segmentNumber] +
            (offset >= limits[segmentNumber] ? "\n\nTRAP: segmentation fault." : "\nPhysical address   : " + (bases[segmentNumber] + offset));
    }
}

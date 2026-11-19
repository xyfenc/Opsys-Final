public final class Paging {
    private Paging() {}

    public static String translate(int pageSize, int pageCount, int logicalAddress, int[] frameNumbers) throws Exception {
        if (pageSize <= 0 || pageCount <= 0) throw new Exception("Page size and logical page count must be positive.");
        if (logicalAddress < 0) throw new Exception("Logical address must be non-negative.");
        // ---- Step 1: split the logical address into (page number, offset inside that page) 
//logical address is conceptually:  [ page number | offset within page 
int pageNumber       = logicalAddress / pageSize;  // which page the address lives in
int offsetWithinPage = logicalAddress % pageSize;  // byte position inside that page

//S: validate the page table and the derived page number
boolean pageTableHasOneEntryPerPage = (frameNumbers.length == pageCount);
boolean pageNumberOutOfRange        = (pageNumber < 0 || pageNumber >= pageCount);

if (!pageTableHasOneEntryPerPage || pageNumberOutOfRange) {
    throw new Exception("Invalid page table or address.");
}

//S3: page table lookup -> which physical frame holds this page
int frameNumber      = frameNumbers[pageNumber];   // -1 or any negative means "not resident"
boolean pageNotInMemory = (frameNumber < 0);

//S4: output
StringBuilder result = new StringBuilder("PAGING RESULT\n\n");
result.append("@TABLE|ADDRESS TRANSLATION|Logical address|Page size|Page number|Frame number|Offset|Physical address|Status\n");
result.append("@ROW|").append(logicalAddress).append('|').append(pageSize).append('|').append(pageNumber).append('|')
    .append(frameNumber < 0 ? "Not present" : frameNumber).append('|').append(offsetWithinPage).append('|');
result.append(frameNumber < 0 ? "Not available|PAGE FAULT\n@ENDTABLE\n\n" : (frameNumber * pageSize + offsetWithinPage) + "|VALID\n@ENDTABLE\n\n")
    .append("CALCULATIONS\n")
    .append("Page number = Logical address / Page size -> ").append(logicalAddress).append(" / ").append(pageSize).append(" = ").append(pageNumber).append('\n')
    .append("Offset = Logical address % Page size -> ").append(logicalAddress).append(" % ").append(pageSize).append(" = ").append(offsetWithinPage).append('\n');

if (pageNotInMemory) {
    result.append("PAGE FAULT: page is not in memory; no physical address is available.");
} else {
    // Physical address = start of the frame + the offset inside that frame
    int physicalAddress = frameNumber * pageSize + offsetWithinPage;
    result.append("Physical address = (Frame number x Page size) + Offset -> (").append(frameNumber).append(" x ").append(pageSize).append(") + ").append(offsetWithinPage).append(" = ").append(physicalAddress);
}

return result.toString();
    }
}
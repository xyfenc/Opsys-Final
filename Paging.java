public final class Paging {
    private Paging() {}

    public static String translate(int pageSize, int pageCount, int logicalAddress, int[] frameNumbers) throws Exception {
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
StringBuilder result = new StringBuilder();
result.append("PAGING RESULT\n\n");
result.append("Page number       : ").append(pageNumber).append("\n");
result.append("Offset             : ").append(offsetWithinPage).append("\n");
result.append("Frame number       : ").append(frameNumber);
result.append("\n\nADDRESS CHECK\n");
result.append("page number = logical address / page size = ").append(logicalAddress).append(" / ").append(pageSize).append(" = ").append(pageNumber).append("\n");
result.append("offset = logical address % page size = ").append(logicalAddress).append(" % ").append(pageSize).append(" = ").append(offsetWithinPage).append("\n");
result.append("page table[").append(pageNumber).append("] = frame ").append(frameNumber).append("\n");

if (pageNotInMemory) {
    result.append("\n\nPAGE FAULT: page is not in memory.");
} else {
    // Physical address = start of the frame + the offset inside that frame
    int physicalAddress = frameNumber * pageSize + offsetWithinPage;
    result.append("\nPhysical address   : ").append(frameNumber).append(" * ").append(pageSize)
        .append(" + ").append(offsetWithinPage).append(" = ").append(physicalAddress);
}

return result.toString();
    }
}
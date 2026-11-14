public final class Paging {
    private Paging() {}

    public static String translate(int pageSize, int pageCount, int logicalAddress, int[] frameNumbers) throws Exception {
        int pageNumber = logicalAddress / pageSize, offset = logicalAddress % pageSize;
        if (frameNumbers.length != pageCount || pageNumber < 0 || pageNumber >= pageCount) throw new Exception("Invalid page table or address.");
        return "PAGING RESULT\n\nPage number       : " + pageNumber + "\nOffset             : " + offset + "\nFrame number       : " + frameNumbers[pageNumber] +
            (frameNumbers[pageNumber] < 0 ? "\n\nPAGE FAULT: page is not in memory." : "\nPhysical address   : " + (frameNumbers[pageNumber] * pageSize + offset));
    }
}

package heap;

import global.PageId;

/**
 * A heap file directory page; contains DirEntry records.
 */
class DirPage extends HFPage {

  /** The size of a directory entry. */
  protected static final int ENTRY_SIZE = 8;

  /** Relative offset of a page id from an index. */
  protected static final int IX_PAGE_ID = 0;

  /** Relative offset of a record count from an index. */
  protected static final int IX_REC_CNT = 4;

  /** Relative offset of a free count from an index. */
  protected static final int IX_FREE_CNT = 6;

  // --------------------------------------------------------------------------

  /** The size of the footer data (in bytes). */
  protected static final int FOOTER_SIZE = 2;

  /** Page offest for the total number of entries. */
  protected static final int ENTRY_COUNT = PAGE_SIZE - 2;

  /** Maximum number of entries in a directory page. */
  protected static final int MAX_ENTRIES = (PAGE_SIZE - HEADER_SIZE - FOOTER_SIZE)
      / ENTRY_SIZE;

  // --------------------------------------------------------------------------

  /**
   * Default constructor; creates a directory page with default values.
   */
  public DirPage() {
    super();
    setType(HeapFile.DIR_PAGE);
    setEntryCnt((short) 0);
  }

  /**
   * Gets the number of directory entries on the page.
   */
  public short getEntryCnt() {
    return getShortValue(ENTRY_COUNT);
  }

  /**
   * Sets the number of directory entries on the page.
   */
  public void setEntryCnt(short entryCnt) {
    setShortValue(entryCnt, ENTRY_COUNT);
  }

  /**
   * Gets the PageId at the given index.
   */
  public PageId getPageId(int slotno) {
    return new PageId(getIntValue(HEADER_SIZE + slotno * ENTRY_SIZE
        + IX_PAGE_ID));
  }

  /**
   * Sets the PageId at the given index.
   */
  public void setPageId(int slotno, PageId pageno) {
    setIntValue(pageno.pid, HEADER_SIZE + slotno * ENTRY_SIZE + IX_PAGE_ID);
  }

  /**
   * Gets the record count at the given index.
   */
  public short getRecCnt(int slotno) {
    return getShortValue(HEADER_SIZE + slotno * ENTRY_SIZE + IX_REC_CNT);
  }

  /**
   * Sets the record count at the given index.
   */
  public void setRecCnt(int slotno, short recCnt) {
    setShortValue(recCnt, HEADER_SIZE + slotno * ENTRY_SIZE + IX_REC_CNT);
  }

  /**
   * Gets the free count at the given index.
   */
  public short getFreeCnt(int slotno) {
    return getShortValue(HEADER_SIZE + slotno * ENTRY_SIZE + IX_FREE_CNT);
  }

  /**
   * Sets the free count at the given index.
   */
  public void setFreeCnt(int slotno, short freeCnt) {
    setShortValue(freeCnt, HEADER_SIZE + slotno * ENTRY_SIZE + IX_FREE_CNT);
  }

  /**
   * Logically deletes an entry at the given slot number by shifting any
   * successive entries down.
   */
  public void compact(int slotno) {

    // shift all bytes to the left
    int entryPos = HEADER_SIZE + slotno * ENTRY_SIZE;
    int succLen = PAGE_SIZE - FOOTER_SIZE - entryPos - ENTRY_SIZE;
    System.arraycopy(data, entryPos + ENTRY_SIZE, data, entryPos, succLen);

  } // public void compact(int slotno)

} // class DirPage extends HFPage

package heap;

import global.Page;
import global.PageId;
import global.RID;

/**
 * Heap file data pages are implemented as slotted pages, with the slots at the
 * front and the records in the back, both growing into the free space in the
 * middle of the page. This design assumes that records are kept compacted when
 * deletions are performed. Each slot contains the length and offset of its
 * corresponding record.
 */
class HFPage extends Page {

  /** Offset of the number of slots. */
  protected static final int SLOT_CNT = 0;

  /** Offset of the used space offset. */
  protected static final int USED_PTR = 2;

  /** Offset of the amount of free space. */
  protected static final int FREE_SPACE = 4;

  /** Offset of the arbitrary page type. */
  protected static final int PAGE_TYPE = 6;

  /** Offset of the previous page id. */
  protected static final int PREV_PAGE = 8;

  /** Offset of the next page id. */
  protected static final int NEXT_PAGE = 12;

  /** Offset of the current page id. */
  protected static final int CUR_PAGE = 16;

  // --------------------------------------------------------------------------

  /** Total size of the header fields. */
  protected static final int HEADER_SIZE = 20;

  /** Size of a record slot. */
  protected static final int SLOT_SIZE = 4;

  // --------------------------------------------------------------------------

  /**
   * Default constructor; creates a heap file page with default values.
   */
  public HFPage() {
    super();
    initDefaults();
  }

  /**
   * Constructor that wraps an existing heap file page.
   */
  public HFPage(Page page) {
    super(page.getData());
  }

  /**
   * Initializes the heap file page with default values.
   */
  protected void initDefaults() {

    // initially no slots in use
    setShortValue((short) 0, SLOT_CNT);

    // used offset grows backwards
    setShortValue((short) PAGE_SIZE, USED_PTR);

    // free space doesn't count headers
    setShortValue((short) (PAGE_SIZE - HEADER_SIZE), FREE_SPACE);

    // optional type field may be used by sub classes
    setShortValue((short) 0, PAGE_TYPE);

    // set all page ids to invalid
    setIntValue(INVALID_PAGEID, PREV_PAGE);
    setIntValue(INVALID_PAGEID, NEXT_PAGE);
    setIntValue(INVALID_PAGEID, CUR_PAGE);

  } // protected void initDefaults()

  /**
   * Gets the number of slots on the page.
   */
  public short getSlotCount() {
    return getShortValue(SLOT_CNT);
  }

  /**
   * Gets the amount of free space (in bytes).
   */
  public short getFreeSpace() {
    return getShortValue(FREE_SPACE);
  }

  /**
   * Gets the arbitrary type of the page.
   */
  public short getType() {
    return getShortValue(PAGE_TYPE);
  }

  /**
   * Sets the arbitrary type of the page.
   */
  public void setType(short type) {
    setShortValue(type, PAGE_TYPE);
  }

  /**
   * Gets the previous page's id.
   */
  public PageId getPrevPage() {
    return new PageId(getIntValue(PREV_PAGE));
  }

  /**
   * Sets the previous page's id.
   */
  public void setPrevPage(PageId pageno) {
    setIntValue(pageno.pid, PREV_PAGE);
  }

  /**
   * Gets the next page's id.
   */
  public PageId getNextPage() {
    return new PageId(getIntValue(NEXT_PAGE));
  }

  /**
   * Sets the next page's id.
   */
  public void setNextPage(PageId pageno) {
    setIntValue(pageno.pid, NEXT_PAGE);
  }

  /**
   * Gets the current page's id.
   */
  public PageId getCurPage() {
    return new PageId(getIntValue(CUR_PAGE));
  }

  /**
   * Sets the current page's id.
   */
  public void setCurPage(PageId pageno) {
    setIntValue(pageno.pid, CUR_PAGE);
  }

  /**
   * Gets the length of the record referenced by the given slot.
   */
  public short getSlotLength(int slotno) {
    return getShortValue(HEADER_SIZE + slotno * SLOT_SIZE);
  }

  /**
   * Gets the offset of the record referenced by the given slot.
   */
  public short getSlotOffset(int slotno) {
    return getShortValue(HEADER_SIZE + slotno * SLOT_SIZE + 2);
  }

  /**
   * Inserts a new record into the page.
   * 
   * @return RID of new record, or null if insufficient space
   */
  public RID insertRecord(byte[] record) {

    // first check for sufficient space
    short recLength = (short) record.length;
    int spaceNeeded = recLength + SLOT_SIZE;
    short freeSpace = getShortValue(FREE_SPACE);
    if (spaceNeeded > freeSpace)
      return null;

    // linear search for an empty slot
    short slotCnt = getShortValue(SLOT_CNT);
    short i, length;
    for (i = 0; i < slotCnt; i++) {
      length = getSlotLength(i);
      if (length == EMPTY_SLOT)
        break;
    }

    // if using a new slot
    if (i == slotCnt) {

      // adjust the free space
      freeSpace -= spaceNeeded;
      setShortValue(freeSpace, FREE_SPACE);

      // adjust the slot count
      slotCnt++;
      setShortValue(slotCnt, SLOT_CNT);

    } else {

      // otherwise, reusing an existing slot
      freeSpace -= recLength;
      setShortValue(freeSpace, FREE_SPACE);

    } // else

    // update the used space offset
    short usedPtr = getShortValue(USED_PTR);
    usedPtr -= recLength;
    setShortValue(usedPtr, USED_PTR);

    // update the slot, copy the record, and return the RID
    int slotpos = HEADER_SIZE + i * SLOT_SIZE;
    setShortValue(recLength, slotpos);
    setShortValue(usedPtr, slotpos + 2);
    System.arraycopy(record, 0, data, usedPtr, recLength);
    return new RID(new PageId(getIntValue(CUR_PAGE)), i);

  } // public RID insertRecord(byte[] record)

  /**
   * Selects a record from the page.
   * 
   * @throws IllegalArgumentException if the rid is invalid
   */
  public byte[] selectRecord(RID rid) {

    // get and validate the record information
    short length = checkRID(rid);
    short offset = getSlotOffset(rid.slotno);

    // finally, get and return the record
    byte[] record = new byte[length];
    System.arraycopy(data, offset, record, 0, length);
    return record;

  } // public byte[] selectRecord(RID rid)

  /**
   * Updates a record on the page.
   * 
   * @throws IllegalArgumentException if the rid or record size is invalid
   */
  public void updateRecord(RID rid, byte[] record) {

    // get and validate the record information
    short length = checkRID(rid);
    if (record.length != length)
      throw new IllegalArgumentException("Invalid record size");

    // finally, update the record in place
    short offset = getSlotOffset(rid.slotno);
    System.arraycopy(record, 0, data, offset, length);

  } // public void updateRecord(RID rid, byte[] record)

  /**
   * Deletes a record from the page, compacting the records space. The slot
   * directory cannot be compacted because that would alter existing RIDs.
   * 
   * @throws IllegalArgumentException if the rid is invalid
   */
  public void deleteRecord(RID rid) {

    // get and validate the record information
    short length = checkRID(rid);
    short offset = getSlotOffset(rid.slotno);

    // calculate the compacting values
    short usedPtr = getShortValue(USED_PTR);
    short newSpot = (short) (usedPtr + length);
    short size = (short) (offset - usedPtr);

    // shift all bytes to the right
    System.arraycopy(data, usedPtr, data, newSpot, size);

    // adjust offsets of all valid slots that refer
    // to the left of the record being removed
    short slotCnt = getShortValue(SLOT_CNT);
    for (int i = 0, n = HEADER_SIZE; i < slotCnt; i++, n += SLOT_SIZE) {
      if (getSlotLength(i) != EMPTY_SLOT) {
        short chkoffset = getSlotOffset(i);
        if (chkoffset < offset) {
          chkoffset += length;
          setShortValue(chkoffset, n + 2);
        }
      }
    }

    // move the used space offset forward
    setShortValue(newSpot, USED_PTR);

    // increase freespace by size of hole
    short freeSpace = getShortValue(FREE_SPACE);
    freeSpace += length;
    setShortValue(freeSpace, FREE_SPACE);

    // mark the slot as empty
    int slotpos = HEADER_SIZE + rid.slotno * SLOT_SIZE;
    setShortValue((short) EMPTY_SLOT, slotpos);
    setShortValue((short) 0, slotpos + 2);

  } // public void deleteRecord(RID rid)

  /**
   * Gets the RID of the first record on the page, or null if none.
   */
  public RID firstRecord() {

    // find the first non-empty slot
    short slotCnt = getShortValue(SLOT_CNT);
    int i = 0;
    for (; i < slotCnt; i++) {
      short length = getSlotLength(i);
      if (length != EMPTY_SLOT)
        break;
    }

    // if all slots are empty, there are no records
    if (i == slotCnt)
      return null;

    // otherwise, found a non-empty slot
    return new RID(new PageId(getIntValue(CUR_PAGE)), i);

  } // public RID firstRecord()

  /**
   * Returns true if the iteration has more elements.
   * 
   * @throws IllegalArgumentException if the rid is invalid
   */
  public boolean hasNext(RID curRid) {

    // validate the record id
    int curPid = getIntValue(CUR_PAGE);
    short slotCnt = getShortValue(SLOT_CNT);
    if ((curRid.pageno.pid != curPid) || (curRid.slotno < 0)
        || (curRid.slotno > slotCnt))
      throw new IllegalArgumentException("Invalid RID");

    // find the next non-empty slot
    int i = curRid.slotno + 1;
    for (; i < slotCnt; i++) {
      short length = getSlotLength(i);
      if (length != EMPTY_SLOT)
        break;
    }

    // if remaining slots were empty, there are no more records
    return (i != slotCnt);

  } // public boolean hasNext(RID curRid)

  /**
   * Gets the next RID after the given one, or null if no more.
   * 
   * @throws IllegalArgumentException if the rid is invalid
   */
  public RID nextRecord(RID curRid) {

    // validate the record id
    int curPid = getIntValue(CUR_PAGE);
    short slotCnt = getShortValue(SLOT_CNT);
    if ((curRid.pageno.pid != curPid) || (curRid.slotno < 0)
        || (curRid.slotno > slotCnt))
      throw new IllegalArgumentException("Invalid RID");

    // find the next non-empty slot
    int i = curRid.slotno + 1;
    for (; i < slotCnt; i++) {
      short length = getSlotLength(i);
      if (length != EMPTY_SLOT)
        break;
    }

    // if remaining slots were empty, there are no more records
    if (i == slotCnt)
      return null;

    // otherwise, found a non-empty slot
    return new RID(new PageId(getIntValue(CUR_PAGE)), i);

  } // public RID nextRecord(RID curRid)

  /**
   * Prints the contents of a heap file page.
   */
  public void print() {

    short slotCnt = getShortValue(SLOT_CNT);

    System.out.println("HFPage:");
    System.out.println("-------");
    System.out.println("  curPage   = " + getIntValue(CUR_PAGE));
    System.out.println("  prevPage  = " + getIntValue(PREV_PAGE));
    System.out.println("  nextPage  = " + getIntValue(NEXT_PAGE));
    System.out.println("  slotCnt   = " + slotCnt);
    System.out.println("  usedPtr   = " + getShortValue(USED_PTR));
    System.out.println("  freeSpace = " + getShortValue(FREE_SPACE));
    System.out.println("  pageType  = " + getShortValue(PAGE_TYPE));
    System.out.println("-------");

    for (int i = 0, n = HEADER_SIZE; i < slotCnt; i++, n += SLOT_SIZE) {
      System.out.println("slot #" + i + " offset = " + getShortValue(n));
      System.out.println("slot #" + i + " length = " + getShortValue(n + 2));
    }

  } // public void print()

  /**
   * Validates a record id exists on this page.
   * 
   * @return the record length (if valid)
   * @throws IllegalArgumentException if the slot is empty or the RID is invalid
   */
  protected short checkRID(RID rid) {

    // validate the record id
    int curPid = getIntValue(CUR_PAGE);
    short slotCnt = getShortValue(SLOT_CNT);
    if ((rid.pageno.pid != curPid) || (rid.slotno < 0)
        || (rid.slotno > slotCnt))
      throw new IllegalArgumentException("Invalid RID");

    // validate the record itself
    short recLen = getSlotLength(rid.slotno);
    if (recLen == EMPTY_SLOT)
      throw new IllegalArgumentException("Empty slot");
    return recLen;

  } // protected short checkRID(RID rid)

} // class HFPage extends Page

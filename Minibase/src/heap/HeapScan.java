package heap;

import global.GlobalConst;
import global.Minibase;
import global.PageId;
import global.RID;

/**
 * A HeapScan object is created only through the function openScan() in the
 * HeapFile class. It supports the getNext interface which will simply retrieve
 * the next record in the file.
 */
public class HeapScan implements GlobalConst {

  /** Currently pinned directory page (outer loop). */
  protected DirPage dirPage;

  /** Number of entries on the directory page. */
  protected int count;

  /** Index of the current entry on the directory page. */
  protected int index;

  /** Currently pinned data page (inner loop). */
  protected DataPage dataPage;

  /** RID of the current record on the data page. */
  protected RID curRid;

  // --------------------------------------------------------------------------

  /**
   * Constructs a file scan by pinning the directoy header page and initializing
   * iterator fields.
   */
  protected HeapScan(HeapFile hf) {

    // pin the head page and get the count
    dirPage = new DirPage();
    Minibase.BufferManager.pinPage(hf.headId, dirPage, PIN_DISKIO);
    count = dirPage.getEntryCnt();

    // initialize other data fields
    index = -1;
    dataPage = null;
    curRid = null;

  } // protected HeapScan(HeapFile hf)

  /**
   * Called by the garbage collector when there are no more references to the
   * object; closes the scan if it's still open.
   */
  protected void finalize() throws Throwable {

    // close the scan, if open
    if (dirPage != null) {
      close();
    }

  } // protected void finalize() throws Throwable

  /**
   * Closes the file scan, releasing any pinned pages.
   */
  public void close() {

    // unpin the pages where applicable
    if (dataPage != null) {
      Minibase.BufferManager.unpinPage(dataPage.getCurPage(), UNPIN_CLEAN);
      dataPage = null;
    }
    if (dirPage != null) {
      Minibase.BufferManager.unpinPage(dirPage.getCurPage(), UNPIN_CLEAN);
      dirPage = null;
    }

    // invalidate the other fields
    count = -1;
    index = -1;
    curRid = null;

  } // public void close()

  /**
   * Returns true if there are more records to scan, false otherwise.
   */
  public boolean hasNext() {

    // if iterating on a data page
    if (curRid != null) {
      if (dataPage.nextRecord(curRid) != null) {
        return true;
      }
    }

    // if more data page
    if (index < count - 1) {
      return true;
    }

    // if more dir pages
    if (dirPage.getNextPage().pid != INVALID_PAGEID) {
      return true;
    }

    // none of the above
    return false;

  } // public boolean hasNext()

  /**
   * Gets the next record in the file scan.
   * 
   * @param rid output parameter that identifies the returned record
   * @throws IllegalStateException if the scan has no more elements
   */
  public byte[] getNext(RID rid) {

    // base case: iterate within the data page
    if (curRid != null) {

      // get the next record id
      curRid = dataPage.nextRecord(curRid);
      if (curRid != null) {

        // return both the RID and the record
        rid.copyRID(curRid);
        return dataPage.selectRecord(rid);

      } else {

        // all done with the current data page
        Minibase.BufferManager.unpinPage(dataPage.getCurPage(), UNPIN_CLEAN);

      } // else

    } // if current

    // move on to the next data page
    if (index < count - 1) {

      // minor optimization
      if (dataPage == null) {
        dataPage = new DataPage();
      }

      // pin the next data page
      index++;
      Minibase.BufferManager.pinPage(dirPage.getPageId(index), dataPage,
          PIN_DISKIO);

      // reset the counter and get the first record
      curRid = dataPage.firstRecord();
      if (curRid != null) {
        rid.copyRID(curRid);
        return dataPage.selectRecord(rid);
      }

    } // if more entries

    // move on to the next directory page
    if (dirPage.getNextPage().pid != INVALID_PAGEID) {

      // unpin the current dir page, pin the next dir page
      PageId nextId = dirPage.getNextPage();
      Minibase.BufferManager.unpinPage(dirPage.getCurPage(), UNPIN_CLEAN);
      Minibase.BufferManager.pinPage(nextId, dirPage, PIN_DISKIO);

      // reset the counters and try again
      count = dirPage.getEntryCnt();
      index = -1;
      curRid = null;
      return getNext(rid);

    } // if more dir pages

    // otherwise, no more records
    throw new IllegalStateException("No more elements");

  } // public byte[] getNext(RID rid)

} // public class HeapScan implements GlobalConst

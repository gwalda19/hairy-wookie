package heap;

import global.GlobalConst;
import global.Minibase;
import global.PageId;
import global.RID;

/**
 * <h3>Minibase Heap Files</h3>
 * A heap file is an unordered set of records, stored on a set of pages. This
 * class provides basic support for inserting, selecting, updating, and deleting
 * records. Temporary heap files are used for external sorting and in other
 * relational operators. A sequential scan of a heap file (via the Scan class)
 * is the most basic access method.
 */
public class HeapFile implements GlobalConst {

  static final short DATA_PAGE = 11;
  static final short DIR_PAGE = 12;
  String fileName;
  PageId headId;
  Boolean isTemp;

  /**
   * If the given name already denotes a file, this opens it; otherwise, this
   * creates a new empty file. A null name produces a temporary heap file which
   * requires no DB entry.
   */
  public HeapFile(String name) {
    
    if (name == null)
    {
      // Construct a temporary heapfile.
      // Set heapfile name to an empty string so the toString() method works.
      isTemp = true;
      fileName = "";
      headId = null;
    }
    else
    {
      // Construct a heapfile.
      // Store heapfile name and attempt to get the pageid of the head dir
      // associated with it from the disk.
      isTemp = false;
      fileName = name;
      headId = Minibase.DiskManager.get_file_entry(fileName);
    }
    
    if (headId == null)
    {
      // We are either a temp heapfile or a previously undefined/new
      // one.  So, create the head dir and initialize it.
      DirPage dirPage = new DirPage();
      headId = Minibase.BufferManager.newPage(dirPage, 1);
      dirPage.setCurPage(headId);
      Minibase.BufferManager.unpinPage(headId, UNPIN_DIRTY);
      if (!isTemp)
      {
        // Not temp, so save the head dir pageid to disk for 
        // any future references.
        Minibase.DiskManager.add_file_entry(fileName, headId);
      }
    }
  }

  /**
   * Called by the garbage collector when there are no more references to the
   * object; deletes the heap file if it's temporary.
   */
  protected void finalize() throws Throwable {
    
    if (isTemp)
    {
      deleteFile();
    }
  }

  /**
   * Deletes the heap file from the database, freeing all of its pages.
   */
  public void deleteFile() {
    
    // Start at the head dir.
    PageId dirId = new PageId(headId.pid);
    DirPage dirPage = new DirPage();
    
    do
    {
      // Pin current dir page and get the next dir page.
      PageId curPageId = new PageId(dirId.pid);
      Minibase.BufferManager.pinPage(curPageId, dirPage, PIN_DISKIO);
      dirId = dirPage.getNextPage();

      // Go thru each directory entry on the dir page.
      for (short i=0; i < dirPage.getEntryCnt(); i++)
      {
        // Get the data pageid and free it.
        PageId dataId = dirPage.getPageId(i);
        Minibase.BufferManager.freePage(dataId);
      }
      
      // Unpin and free the current dir page.
      Minibase.BufferManager.unpinPage(curPageId, UNPIN_CLEAN);
      Minibase.BufferManager.freePage(curPageId);
    } while (dirId.pid != INVALID_PAGEID);

    if (!isTemp)
    {
      // Not temp, so delete the heapfile entry from the disk.
      Minibase.DiskManager.delete_file_entry(fileName);
    }
  }

  /**
   * Inserts a new record into the file and returns its RID.
   * 
   * @throws IllegalArgumentException if the record is too large
   */
  public RID insertRecord(byte[] record) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Reads a record from the file, given its id.
   * 
   * @throws IllegalArgumentException if the rid is invalid
   */
  public byte[] selectRecord(RID rid) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Updates the specified record in the heap file.
   * 
   * @throws IllegalArgumentException if the rid or new record is invalid
   */
  public void updateRecord(RID rid, byte[] newRecord) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Deletes the specified record from the heap file.
   * 
   * @throws IllegalArgumentException if the rid is invalid
   */
  public void deleteRecord(RID rid) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Gets the number of records in the file.
   */
  public int getRecCnt() {
	int count = 0;
	DirPage dirPage = new DirPage();
	PageId dirId = new PageId(headId.pid);
	
	//go through each dirPage in the heap file
	do
    {
      // Pin current dir page and get the next dir page.
      PageId curPageId = new PageId(dirId.pid);
      Minibase.BufferManager.pinPage(curPageId, dirPage, PIN_DISKIO);
      dirId = dirPage.getNextPage();

  	  // Go thru each directory entry on the dir page.
      for (short i=0; i < dirPage.getEntryCnt(); i++)
      {
        count = count + dirPage.getRecCnt(i);
      }
      
      // Unpin and free the current dir page.
      Minibase.BufferManager.unpinPage(curPageId, UNPIN_CLEAN);
    } while (dirId.pid != INVALID_PAGEID);

    return count;
  }

  /**
   * Initiates a sequential scan of the heap file.
   */
  public HeapScan openScan() {
    return new HeapScan(this);
  }

  /**
   * Returns the name of the heap file.
   */
  public String toString() {
    return fileName;
  }

} // public class HeapFile implements GlobalConst

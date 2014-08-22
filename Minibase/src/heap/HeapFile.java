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
  public RID insertRecord(byte[] record) throws IllegalArgumentException {
    
    if (record.length > (PAGE_SIZE - DataPage.HEADER_SIZE - DataPage.SLOT_SIZE))
    {
      // If the record size is too big then we can't fit it.  The max length
      // is 1000 bytes (1024 - 20 - 4) for a record on a data page.
      throw new IllegalArgumentException();
    }
    
    // Start at the head dir.
    PageId dirId = new PageId(headId.pid);
    DirPage dirPage = new DirPage();
    PageId curPageId;
    RID rid = null;
    
    do
    {
      // Pin current dir page and get the next dir page.
      curPageId = new PageId(dirId.pid);
      Minibase.BufferManager.pinPage(curPageId, dirPage, PIN_DISKIO);
      dirId = dirPage.getNextPage();

      // Go thru each directory entry on the dir page.
      for (short i=0; i < dirPage.getEntryCnt(); i++)
      {
        // We need entry for a data page that can store the record plus
        // the room for the slot.
        if (dirPage.getFreeCnt(i) >= (record.length + DataPage.SLOT_SIZE))
        {
          // We found one, so pin the data page and insert the record in it.
          PageId dataId = dirPage.getPageId(i);
          DataPage dataPage = new DataPage();
          Minibase.BufferManager.pinPage(dataId, dataPage, PIN_DISKIO);
          rid = dataPage.insertRecord(record);

          // Record the new record count and new free space count into this
          // directory entry and then unpin the data page.
          dirPage.setRecCnt(i, dataPage.getSlotCount());
          dirPage.setFreeCnt(i, dataPage.getFreeSpace());
          Minibase.BufferManager.unpinPage(dataId, UNPIN_DIRTY);
          
          // We're done our search and have a rid, so we can get out.
          break;
        }
      }

      if (rid != null)
      {
        // We inserted the record and updated the directory entry so
        // we need to unpin, write the changes and get out.
        Minibase.BufferManager.unpinPage(curPageId, UNPIN_DIRTY);
        break;
      }
      
      // Still haven't found what we're looking for.
      Minibase.BufferManager.unpinPage(curPageId, UNPIN_CLEAN);
    } while (dirId.pid != INVALID_PAGEID);
    
    if (rid == null)
    {
      // If we got here then we went thru all the dir pages and couldn't find a
      // data page that could hold the record. In this case we need to create a
      // new data page to hold it.
      DataPage dataPage = new DataPage();
      PageId dataId = Minibase.BufferManager.newPage(dataPage, 1);
      dataPage.setCurPage(dataId);
      rid = dataPage.insertRecord(record);
      short slotCount = dataPage.getSlotCount();
      short freeSpace = dataPage.getFreeSpace();
      Minibase.BufferManager.unpinPage(dataId, UNPIN_DIRTY);
      
      // We now need to find a dir page to hold the entry for the new data page
      // we created.  At this point we have the pageid of the last dir page 
      // (in curPageId) from the previous search.  But we cannot just insert it
      // there as there may have been deletes on previous dir pages. Or this
      // last dir page may have max entries.  So, unfortunately we need to
      // search again from the start.
      dirId = new PageId(headId.pid);
      boolean addedEntry = false;
      
      do
      {
        // Pin current dir page and get the next dir page.
        curPageId = new PageId(dirId.pid);
        Minibase.BufferManager.pinPage(curPageId, dirPage, PIN_DISKIO);
        dirId = dirPage.getNextPage();

        short entryCnt = dirPage.getEntryCnt();
        if (entryCnt < DirPage.MAX_ENTRIES)
        {
          // There's room for an entry on this dir page.  So enter it, unpin 
          // dir page and get out.
          dirPage.setPageId(entryCnt, dataId);
          dirPage.setRecCnt(entryCnt, slotCount);
          dirPage.setFreeCnt(entryCnt, freeSpace);
          dirPage.setEntryCnt(++entryCnt);
          Minibase.BufferManager.unpinPage(curPageId, UNPIN_DIRTY);
          addedEntry = true;
          break;
        }
      
        // Still haven't found what we're looking for.
        Minibase.BufferManager.unpinPage(curPageId, UNPIN_CLEAN);
      } while (dirId.pid != INVALID_PAGEID);
      
      if (!addedEntry)
      {
        // If we got here then every dir page already has max entries.  In this
        // case we need to add a new dir page.  We already know that curPageId
        // referes to the last dir page from the previous search, so pin it.
        Minibase.BufferManager.pinPage(curPageId, dirPage, PIN_DISKIO);
        
        // Create the new dir page and record the entry
        DirPage newDirPage = new DirPage();
        PageId newDirId = Minibase.BufferManager.newPage(newDirPage, 1);
        newDirPage.setCurPage(newDirId);
        newDirPage.setPageId(0, dataId);
        newDirPage.setRecCnt(0, slotCount);
        newDirPage.setFreeCnt(0, freeSpace);
        newDirPage.setEntryCnt((short)1);

        // Set the old last dir page to point to the new last dir page 
        // and vice-versa.
        dirPage.setNextPage(newDirId);
        newDirPage.setPrevPage(curPageId);
        
        // Unpin both dir pages now that they are modified.
        Minibase.BufferManager.unpinPage(newDirId, UNPIN_DIRTY);
        Minibase.BufferManager.unpinPage(curPageId, UNPIN_DIRTY);
      }
    }
    
    return rid;
  }

  /**
   * Reads a record from the file, given its id.
   * 
   * @throws IllegalArgumentException if the rid is invalid
   */
  public byte[] selectRecord(RID rid) throws IllegalArgumentException {

    byte[] record;
    DataPage dataPage = new DataPage();
    Minibase.BufferManager.pinPage(rid.pageno, dataPage, PIN_DISKIO);
    
    try
    {
      record = dataPage.selectRecord(rid);
    }
    catch (Exception e)
    {
      // Invalid rid, so unpin and throw exception.
      Minibase.BufferManager.unpinPage(rid.pageno, UNPIN_CLEAN);
      throw new IllegalArgumentException();            
    }
    
    // Valid rid, so unpin and return the record.
    Minibase.BufferManager.unpinPage(rid.pageno, UNPIN_CLEAN);
    return record;
  }

  /**
   * Updates the specified record in the heap file.
   * 
   * @throws IllegalArgumentException if the rid or new record is invalid
   */
  public void updateRecord(RID rid, byte[] newRecord) throws IllegalArgumentException {
 
    // check for null parameters
    if (rid == null || newRecord == null)
    {
      throw new IllegalArgumentException();
    }

    DataPage page = new DataPage();
    Minibase.BufferManager.pinPage(rid.pageno, page, PIN_DISKIO);
    try
    {
      page.updateRecord(rid, newRecord);
      Minibase.BufferManager.unpinPage(rid.pageno, UNPIN_DIRTY);
    }
    catch(IllegalArgumentException exception)
    {
      Minibase.BufferManager.unpinPage(rid.pageno, UNPIN_CLEAN);
      throw exception;
    }
  }

  /**
   * Deletes the specified record from the heap file.
   * 
   * @throws IllegalArgumentException if the rid is invalid
   */
  public void deleteRecord(RID rid) throws IllegalArgumentException {
	  
	  //check for invalid null rid
	  if (rid == null) throw new IllegalArgumentException();
	  
	  //pin datapage w/record to be deleted
	  DataPage dataPage = new DataPage();
	  Minibase.BufferManager.pinPage(rid.pageno, dataPage, PIN_DISKIO);
  
	  //get length of record being deleted
	  short recordLength = dataPage.getSlotLength(rid.slotno);

	  //delete record & compact record space
	  try
	  {
		  dataPage.deleteRecord(rid);
		  Minibase.BufferManager.unpinPage(rid.pageno, UNPIN_DIRTY);
	  }
	  catch(IllegalArgumentException exception)
	  {
		  Minibase.BufferManager.unpinPage(rid.pageno, UNPIN_CLEAN);
		  throw exception;
	  }
	  
	  //traverse dirpages until you find the one with entry referencing deleted datapage
	  DirPage dirPage = new DirPage();
	  PageId dirId = new PageId(headId.pid);

	  //go through each dirPage in the heap file
	  do
	  {
		  // Pin current dir page and get the next dir page.
		  PageId curPageId = new PageId(dirId.pid);
		  Minibase.BufferManager.pinPage(curPageId, dirPage, PIN_DISKIO);
		  dirId = dirPage.getNextPage();

		  // Go thru each directory entry on the dir page till we find our rid pageid
		  for (short i=0; i < dirPage.getEntryCnt(); i++)
		  {
			  if (dirPage.getPageId(i).pid == rid.pageno.pid)
			  {//we found the dir entry with the page id of our record
				  //decrement record count in directory entry
				  short newRecCnt = dirPage.getRecCnt(i);
				  newRecCnt--;
				  dirPage.setRecCnt(i, newRecCnt);
				  
				  //update free space count in directory entry
				  short newFreeCnt = dirPage.getFreeCnt(i);
				  newFreeCnt += recordLength;
				  dirPage.setFreeCnt(i, newFreeCnt);
				  
				  //handle if no records left on datapage (newRecCnt < 1)
				  if (newRecCnt < 1)
				  {//need to remove empty datapage
					  //delete entry in dirpage & compact down
					  dirPage.compact(i);
					  
					  //unpin datapage & mark dirty for disk io
					  Minibase.BufferManager.unpinPage(rid.pageno, UNPIN_DIRTY);
					  
					  //delete empty datapage from memory
					  Minibase.BufferManager.freePage(rid.pageno);
					  
					  //now that we've deleted the datapage, check if
					  //we need to delete an empty dirpage
					  short newEntryCnt = dirPage.getEntryCnt();
					  
					  if (newEntryCnt < 1)
					  {//need to remove empty dirpage 
						  //check if head dirpage, if so dont delete
						  if (curPageId.pid == headId.pid)
						  {
							  // Unpin the head dir page.
							  Minibase.BufferManager.unpinPage(curPageId, UNPIN_CLEAN);
							  break;
						  }
						  else
						  {//not head dirpage, so delete after fixing prev/next links
							  //pin parent dirpage
							  DirPage parentDirPage = new DirPage();
							  Minibase.BufferManager.pinPage(dirPage.getPrevPage(), parentDirPage, PIN_DISKIO);
							  //set nextpage of parent to nextpage of child of current dirpage
							  parentDirPage.setNextPage(dirPage.getNextPage());
							  
							  if(dirPage.getNextPage().pid != INVALID_PAGEID)
							  {
								//pin child dirpage
								DirPage childDirPage = new DirPage();
								Minibase.BufferManager.pinPage(dirPage.getNextPage(), childDirPage, PIN_DISKIO);
								
								//set prevpage of child to prevpage of current dirpage
								childDirPage.setPrevPage(dirPage.getPrevPage());
								
								//unpin child page
								Minibase.BufferManager.unpinPage(dirPage.getNextPage(), UNPIN_DIRTY);
							  }
							  //unpin parent page
							  Minibase.BufferManager.unpinPage(dirPage.getPrevPage(), UNPIN_DIRTY);
							  
						  }
						  
						  //unpin & free empty dirpage
						  Minibase.BufferManager.unpinPage(curPageId, UNPIN_DIRTY);
						  Minibase.BufferManager.freePage(curPageId);
						  break;
					  }
				  }
			  }
				  
		  }
      
		  // Unpin the current dir page.
		  Minibase.BufferManager.unpinPage(curPageId, UNPIN_CLEAN);
	  } while (dirId.pid != INVALID_PAGEID);




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

package bufmgr;

import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;

import java.util.HashMap;
import java.util.Map;

/**
 * <h3>Minibase Buffer Manager</h3>
 * The buffer manager manages an array of main memory pages.  The array is
 * called the buffer pool, each page is called a frame.  
 * It provides the following services:
 * <ol>
 * <li>Pinning and unpinning disk pages to/from frames
 * <li>Allocating and deallocating runs of disk pages and coordinating this with
 * the buffer pool
 * <li>Flushing pages from the buffer pool
 * <li>Getting relevant data
 * </ol>
 * The buffer manager is used by access methods, heap files, and
 * relational operators.
 */
public class BufMgr implements GlobalConst {

  // This is the array of frames (it contains pages)
  Page[] bufpool;
  
  // This is the array of descripters for the frames
  FrameDesc[] frametab;
  
  // Instead of having to search thru the hashmap looking for the
  // page that the frame mapped to previously (and removing it) every
  // single time we add a new mapping, we are keep two hashmaps to do
  // this much more efficiently.
  HashMap<Integer, Integer> Page2Frame;
  HashMap<Integer, Integer> Frame2Page;
  
  Replacer replacer;
    
  /**
   * Constructs a buffer manager by initializing member data.  
   * 
   * @param numframes number of frames in the buffer pool
   */
  public BufMgr(int numframes) {

    // Initialize everything
    bufpool = new Page[numframes];
    frametab = new FrameDesc[numframes];
    for (int i=0; i<frametab.length; i++)
    {
      bufpool[i] = new Page();
      frametab[i] = new FrameDesc();
    }

    replacer = new Clock(this); 
    Page2Frame = new HashMap<>();
    Frame2Page = new HashMap<>();
  } // public BufMgr(int numframes)

  /**
   * The result of this call is that disk page number pageno should reside in
   * a frame in the buffer pool and have an additional pin assigned to it, 
   * and mempage should refer to the contents of that frame. <br><br>
   * 
   * If disk page pageno is already in the buffer pool, this simply increments 
   * the pin count.  Otherwise, this<br> 
   * <pre>
   * 	uses the replacement policy to select a frame to replace
   * 	writes the frame's contents to disk if valid and dirty
   * 	if (contents == PIN_DISKIO)
   * 		read disk page pageno into chosen frame
   * 	else (contents == PIN_MEMCPY)
   * 		copy mempage into chosen frame
   * 	[omitted from the above is maintenance of the frame table and hash map]
   * </pre>		
   * @param pageno identifies the page to pin
   * @param mempage An output parameter referring to the chosen frame.  If
   * contents==PIN_MEMCPY it is also an input parameter which is copied into
   * the chosen frame, see the contents parameter. 
   * @param contents Describes how the contents of the frame are determined.<br>  
   * If PIN_DISKIO, read the page from disk into the frame.<br>  
   * If PIN_MEMCPY, copy mempage into the frame.<br>  
   * If PIN_NOOP, copy nothing into the frame - the frame contents are irrelevant.<br>
   * Note: In the cases of PIN_MEMCPY and PIN_NOOP, disk I/O is avoided.
   * @throws IllegalArgumentException if PIN_MEMCPY and the page is pinned.
   * @throws IllegalStateException if all pages are pinned (i.e. pool is full)
   */
  public void pinPage(PageId pageno, Page mempage, int contents) { 
  
    // See if the page already is mapped into a frame
    Integer FrameNum = Page2Frame.get(pageno.pid);
    if (FrameNum == null)
    {
      // There is no mapping for this page, go find a frame it can
      // live in.
      int framenum = replacer.pickVictim();
      if (framenum != -1)
      {
        // Found a frame for the page to live in
        if ((frametab[framenum].valid) && (frametab[framenum].dirty))
        {
          // The frame had a page in it that became dirty,
          // so write it out to the disk before using the frame.
          Minibase.DiskManager.write_page(frametab[framenum].pageno, bufpool[framenum]);
          frametab[framenum].dirty = false;
        }

        switch (contents)
        {
          case PIN_DISKIO:
          {
            // Get the page from disk first, copy it into the frame in the
            // buffer pool, set mempage to refer to it, update the frame
            // descripters and update the hashmap.
            Page diskpage = new Page();
            Minibase.DiskManager.read_page(pageno, diskpage);
            bufpool[framenum].copyPage(diskpage);
            mempage.setPage(bufpool[framenum]);
            frametab[framenum].pin_count++;  
            frametab[framenum].valid = true; 
            frametab[framenum].dirty = false; 
            frametab[framenum].refbit = false; 
            frametab[framenum].pageno = new PageId(pageno.pid); 
            addToHashMap(pageno.pid, framenum);
            break;        
          }
          case PIN_MEMCPY:
          {
            // Copy page in mempage into the frame in the buffer pool,
            // set mempage to refer to it, update the frame descripters
            // and update the hashmap.
            bufpool[framenum].copyPage(mempage);  
            mempage.setPage(bufpool[framenum]);
            frametab[framenum].pin_count++;  
            frametab[framenum].valid = true;  
            frametab[framenum].dirty = false; 
            frametab[framenum].refbit = false; 
            frametab[framenum].pageno = new PageId(pageno.pid); 
            addToHashMap(pageno.pid, framenum);
            break;        
          }
          case PIN_NOOP:
          {
            // This currently does nothing and is not called
            break;      
          }
          default:
          {
            // Received an invalid operation
            throw new IllegalArgumentException();
          }
        }
      }
      else
      {
        // Buffer pool is completely full and there are no slots that
        // can be reclaimed.  Very bad news.
        throw new IllegalStateException(); 
      }
    }
    else
    {
      // The page is already mapped to a frame.  Pin it and set
      // mempage to refer to it.
      frametab[FrameNum].pin_count++;  
      mempage.setPage(bufpool[FrameNum]);
    }
  } // public void pinPage(PageId pageno, Page page, int contents)
  
  private void addToHashMap(Integer Page, Integer Frame)
  {
    Integer oldPage = Frame2Page.get(Frame);
    if (oldPage != null)
    {
      // This frame was mapped to a previous page
      // so remove old entries from both hashmaps. 
      Page2Frame.remove(oldPage);
      Frame2Page.remove(Frame);
    }
    
    // Update (add to) both hashmaps
    Page2Frame.put(Page, Frame);
    Frame2Page.put(Frame, Page);
  }
  
  /**
   * Unpins a disk page from the buffer pool, decreasing its pin count.
   * 
   * @param pageno identifies the page to unpin
   * @param dirty UNPIN_DIRTY if the page was modified, UNPIN_CLEAN otherwise
   * @throws IllegalArgumentException if the page is not in the buffer pool
   *  or not pinned
   */
  public void unpinPage(PageId pageno, boolean dirty) {
      
    Integer FrameNum = Page2Frame.get(pageno.pid);
    if ((FrameNum == null) || frametab[FrameNum].pin_count == 0)
    {
      // We were told to unpin a page that was either not in the 
      // buffer pool or not even pinned.
      throw new IllegalArgumentException();      
    }
    else
    {
      if (dirty)
      {
        // Once your unpinned dirty you stay dirty until your written
        // out to disk the next time the frame is pinned.
        frametab[FrameNum].dirty = dirty;
      }
      
      // Update the pin count.
      frametab[FrameNum].pin_count--;
      if (frametab[FrameNum].pin_count == 0)
      {
        // When all the pins are removed set the reference bit.
        frametab[FrameNum].refbit = true;
      }
    }
  } // public void unpinPage(PageId pageno, boolean dirty)
  
  /**
   * Allocates a run of new disk pages and pins the first one in the buffer pool.
   * The pin will be made using PIN_MEMCPY.  Watch out for disk page leaks.
   * 
   * @param firstpg input and output: holds the contents of the first allocated page
   * and refers to the frame where it resides
   * @param run_size input: number of pages to allocate
   * @return page id of the first allocated page
   * @throws IllegalArgumentException if firstpg is already pinned
   * @throws IllegalStateException if all pages are pinned (i.e. pool exceeded)
   */
  public PageId newPage(Page firstpg, int run_size) {

    if (getNumUnpinned() == 0)
    {
      // Buffer pool is already full with unpinned pages
      throw new IllegalStateException();   
    }
    else
    {
      // Allocate the disk pages, return id of first page
      PageId pageno = Minibase.DiskManager.allocate_page(run_size);
      
      Integer FrameNum = Page2Frame.get(pageno.pid);
      if ((FrameNum != null) && (frametab[FrameNum].pin_count > 0))
      {
        // The first page is already mapped into the buffer pool and pinned
        throw new IllegalArgumentException(); 
      }
      else
      {
        // Pin the first page and return its page id
        pinPage(pageno, firstpg, PIN_MEMCPY);
        return pageno;
      }
    }
  } // public PageId newPage(Page firstpg, int run_size)

  /**
   * Deallocates a single page from disk, freeing it from the pool if needed.
   * 
   * @param pageno identifies the page to remove
   * @throws IllegalArgumentException if the page is pinned
   */
  public void freePage(PageId pageno) {

    Integer FrameNum = Page2Frame.get(pageno.pid);
    if ((FrameNum != null) && (frametab[FrameNum].pin_count > 0))
    {
      // The page is mapped into the buffer pool and pinned,
      // so we can't free it.
      throw new IllegalArgumentException();
    }
    else
    {
      // Deallocate the requested disk page
      Minibase.DiskManager.deallocate_page(pageno);
    }
  } // public void freePage(PageId firstid)

  /**
   * Write all valid and dirty frames to disk.
   * Note flushing involves only writing, not unpinning or freeing
   * or the like.
   * 
   */
  public void flushAllPages() {

    for (int i=0; i<frametab.length; i++)
    {
      if ((frametab[i].valid) && (frametab[i].dirty))
      {
        // Only flush frames that have valid pages that are dirty
        flushPage(frametab[i].pageno);   
        frametab[i].dirty = false;
      }
    }
  } // public void flushAllFrames()

  /**
   * Write a page in the buffer pool to disk, if dirty.
   * 
   * @param pageno identifies the page to flush
   * @throws IllegalArgumentException if the page is not in the buffer pool
   */
  public void flushPage(PageId pageno) {
    
    Integer FrameNum = Page2Frame.get(pageno.pid);
    if (FrameNum != null)
    {
      // Write the page to disk
      Minibase.DiskManager.write_page(pageno, bufpool[FrameNum]);
    }
    else
    {
       // The page is not even in the buffer pool
       throw new IllegalArgumentException();   
    }
  }

   /**
   * Gets the total number of buffer frames.
   * @return total number of buffer frames
   */
  public int getNumBuffers() {
    
    return frametab.length;
  }

  /**
   * Gets the total number of unpinned buffer frames.
   * @return total number of unpinned buffer frames
   */
  public int getNumUnpinned() {
    
    int unpinned_count = 0;
    for (int i=0; i<frametab.length; i++)
    {
       if (frametab[i].pin_count == 0)
       {
         unpinned_count++;
       }
    }
    
    return unpinned_count;
  }

} // public class BufMgr implements GlobalConst
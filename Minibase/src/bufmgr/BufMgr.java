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

  Page[] buffer_pool;
  FrameDesc[] frametab;
  HashMap<Integer, Integer> hm; 
  Clock replPolicy;
    
  /**
   * Constructs a buffer manager by initializing member data.  
   * 
   * @param numframes number of frames in the buffer pool
   */
  public BufMgr(int numframes) {

    buffer_pool = new Page[numframes];
    frametab = new FrameDesc[numframes];
    for (int i=0; i<frametab.length; i++)
    {
      buffer_pool[i] = new Page();
      frametab[i] = new FrameDesc();
    }

    replPolicy = new Clock(this); 
    hm = new HashMap<>();
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
  
    //System.out.println("\nPinning PageId: " + pageno);
  
    Integer FrameNum = hm.get(pageno.pid);
    if (FrameNum == null)
    {
      //System.out.println("Not in Buffer Pool");
      int framenum = replPolicy.pickVictim();
      if (framenum != -1)
      {
        //System.out.println("Putting in Frame: " + framenum);
        if ((frametab[framenum].valid) && (frametab[framenum].dirty))
        {
          //System.out.println("Frame dirty, writing to disk");
          Minibase.DiskManager.write_page(frametab[framenum].pageno, buffer_pool[framenum]);
        }

        switch (contents)
        {
          case PIN_DISKIO:
          {
            //System.out.println("PIN_DISKIO");
            Page diskpage = new Page();
            Minibase.DiskManager.read_page(pageno, diskpage);
            buffer_pool[framenum].copyPage(diskpage);
            mempage.setPage(buffer_pool[framenum]);
            frametab[framenum].pin_count++;  
            frametab[framenum].valid = true; 
            frametab[framenum].pageno = new PageId(pageno.pid); 
            updateHashMap(hm, pageno.pid, framenum);
            break;        
          }
          case PIN_MEMCPY:
          {
            //System.out.println("PIN_MEMCPY");
            buffer_pool[framenum].copyPage(mempage);  
            mempage.setPage(buffer_pool[framenum]);
            frametab[framenum].pin_count++;  
            frametab[framenum].valid = true;  
            frametab[framenum].pageno = new PageId(pageno.pid); 
            updateHashMap(hm, pageno.pid, framenum);
            break;        
          }
          case PIN_NOOP:
          {
            //System.out.println("PIN_NOOP");
            break;      
          }
          default:
          {
            throw new IllegalArgumentException();
          }
        }
      }
      else
      {
        throw new IllegalStateException(); 
      }
    }
    else
    {
      //System.out.println("Already in Buffer Pool");
      frametab[FrameNum].pin_count++;  
      mempage.setPage(buffer_pool[FrameNum]);
    }
  } // public void pinPage(PageId pageno, Page page, int contents)
  
  private void updateHashMap(HashMap<Integer, Integer> hm, int pid, int framenum) {
    
    for (Map.Entry<Integer, Integer> entry : hm.entrySet()) {
      if (entry.getValue() == framenum)
      {
        hm.remove(entry.getKey());
        break;
      }
    }      

    hm.put(pid, framenum);
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
      
    //System.out.println("\nUnpinning PageId: " + pageno);
    //System.out.println("Dirty is: " + dirty);
    Integer FrameNum = hm.get(pageno.pid);
    if ((FrameNum == null) || frametab[FrameNum].pin_count == 0)
    {
      throw new IllegalArgumentException();      
    }
    else
    {
      frametab[FrameNum].dirty = dirty;
      frametab[FrameNum].pin_count--;
      if (frametab[FrameNum].pin_count == 0)
      {
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
      throw new IllegalStateException();   
    }
    else
    {
      PageId pageno = Minibase.DiskManager.allocate_page(run_size);
      Integer FrameNum = hm.get(pageno.pid);
      if ((FrameNum != null) && (frametab[FrameNum].pin_count > 0))
      {
        throw new IllegalArgumentException(); 
      }
      else
      {
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

    Integer FrameNum = hm.get(pageno.pid);
    if ((FrameNum != null) && (frametab[FrameNum].pin_count > 0))
    {
      throw new IllegalArgumentException();
    }
    else
    {
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
        flushPage(frametab[i].pageno);    
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
    
    //System.out.println("\nFlushing PageId: " + pageno);
    Integer FrameNum = hm.get(pageno.pid);
    if (FrameNum != null)
    {
      if (frametab[FrameNum].dirty)
      {
        Minibase.DiskManager.write_page(pageno, buffer_pool[FrameNum]);
      }
    }
    else
    {
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
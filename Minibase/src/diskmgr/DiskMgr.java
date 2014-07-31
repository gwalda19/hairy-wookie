package diskmgr;

import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * <h3>Minibase Disk Manager</h3>
 * The disk manager is the component of Minibase that takes care of the
 * allocation and deallocation of pages within the database. It also performs
 * reads and writes of pages to and from disk, providing a logical file layer.
 */
public class DiskMgr implements GlobalConst {

  /** Number of actual bits per page. */
  protected static final int BITS_PER_PAGE = PAGE_SIZE * 8;

  // --------------------------------------------------------------------------

  /** Database file name. */
  protected String name;

  /** Database file size, in pages. */
  protected int num_pages;

  /** Actual reference to the Minibase file. */
  protected RandomAccessFile fp;

  /** Number of disk reads since construction. */
  protected int read_cnt;

  /** Number of disk writes since construction. */
  protected int write_cnt;

  // --------------------------------------------------------------------------

  /**
   * Gets the number of disk reads since construction.
   */
  public int getReadCount() {
    return read_cnt;
  }

  /**
   * Gets the number of disk writes since construction.
   */
  public int getWriteCount() {
    return write_cnt;
  }

  /**
   * Gets the number of allocated disk pages.
   */
  public int getAllocCount() { // TODO: maintain and return "protected int alloc_cnt"

    // initialize reused variables
    int count = 0;
    int bit_number = 0;
    PageId pgid = new PageId();
    Page apage = new Page();

    // iterate each page in the space map
    int num_map_pages = (num_pages + BITS_PER_PAGE - 1) / BITS_PER_PAGE;
    for (int i = 0; i < num_map_pages; i++) {

      // pin the space-map page
      pgid.pid = 1 + i;
      Minibase.BufferManager.pinPage(pgid, apage, PIN_DISKIO);

      // how many bits should we examine on this page?
      int num_bits_this_page = num_pages - i * BITS_PER_PAGE;
      if (num_bits_this_page > BITS_PER_PAGE) {
        num_bits_this_page = BITS_PER_PAGE;
      }

      // walk the page looking for 1 bits
      byte[] pagebuf = apage.getData();
      for (int pgptr = 0; num_bits_this_page > 0; pgptr++) { // start forloop02
        for (int mask = 1; mask < 256 && num_bits_this_page > 0; mask = (mask << 1), --num_bits_this_page, ++bit_number) {
          int bit = pagebuf[pgptr] & mask;
          if (bit != 0) {
            count++;
          }
        }
      }

      // unpin the space-map page
      Minibase.BufferManager.unpinPage(pgid, UNPIN_CLEAN);

    } // end of forloop01

    // return the resulting count
    return count;

  } // public int getAllocCount()

  /**
   * Creates and opens a new database with the given file name and specified
   * number of pages.
   */
  public void createDB(String fname, int num_pgs) {

    // save the parameters locally
    name = fname;
    num_pages = (num_pgs > 2) ? num_pgs : 2;

    // overwrite an existing file
    File DBfile = new File(name);
    DBfile.delete();

    // create the database file, num_pages pages long, filled with zeroes
    try {
      fp = new RandomAccessFile(fname, "rw");
      fp.seek((long) (num_pages * PAGE_SIZE - 1));
      fp.writeByte(0);
    } catch (IOException exc) {
      Minibase.haltSystem(exc);
    }

    // create and initialize the first DB page
    PageId pageId = new PageId(FIRST_PAGEID);
    DBFirstPage firstpg = new DBFirstPage();
    Minibase.BufferManager.pinPage(pageId, firstpg, PIN_MEMCPY);
    firstpg.setNumDBPages(num_pages);
    Minibase.BufferManager.unpinPage(pageId, UNPIN_DIRTY);

    // calculate how many pages are needed for the space map; reserve
    // pages 0 and 1 and as many additional pages as are needed
    int num_map_pages = (num_pages + BITS_PER_PAGE - 1) / BITS_PER_PAGE;
    set_bits(pageId, 1 + num_map_pages, 1);

  } // public void createDB(String fname, int num_pgs)

  /**
   * Open the database with the given file name.
   */
  public void openDB(String fname) {

    // save the name and open the database file
    name = fname;
    try {
      fp = new RandomAccessFile(fname, "rw");
    } catch (IOException exc) {
      Minibase.haltSystem(exc);
    }

    // read the first page
    num_pages = 2; // temp default
    PageId pageId = new PageId(FIRST_PAGEID);
    Page apage = new Page();
    Minibase.BufferManager.pinPage(pageId, apage, PIN_DISKIO);

    // get the total number of pages
    DBFirstPage firstpg = new DBFirstPage(apage);
    num_pages = firstpg.getNumDBPages();
    Minibase.BufferManager.unpinPage(pageId, UNPIN_CLEAN);

  } // public void openDB(String fname)

  /**
   * Closes the database file.
   */
  public void closeDB() {
    try {
      Minibase.BufferManager.flushAllPages();
      fp.close();
    } catch (IOException exc) {
      Minibase.haltSystem(exc);
    }
  }

  /**
   * Destroys the database, removing the file that stores it.
   */
  public void destroyDB() {
    closeDB();
    File DBfile = new File(name);
    DBfile.delete();
  }

  /**
   * Allocates a single page (i.e. run size 1) on disk.
   * 
   * @return The new page's id
   * @throws IllegalStateException if the database is full
   */
  public PageId allocate_page() {
    return allocate_page(1);
  }

  /**
   * Allocates a set of pages on disk, given the run size.
   * 
   * @return The new page's id
   * @throws IllegalArgumentException if run_size is invalid
   * @throws IllegalStateException if the database is full
   */
  public PageId allocate_page(int run_size) {

    // validate the run size
    if ((run_size < 1) || (run_size > num_pages)) {
      throw new IllegalArgumentException("Invalid run size; allocate aborted");
    }

    // calculate the run in the space map
    int num_map_pages = (num_pages + BITS_PER_PAGE - 1) / BITS_PER_PAGE;
    int current_run_start = 0;
    int current_run_length = 0;

    // this loop goes over each page in the space map
    PageId pgid = new PageId();
    Page apage = new Page();
    for (int i = 0; i < num_map_pages; ++i) {

      // pin the space-map page
      pgid.pid = i + 1;
      Minibase.BufferManager.pinPage(pgid, apage, PIN_DISKIO);

      // get the num of bits on current page
      int num_bits_this_page = num_pages - i * BITS_PER_PAGE;
      if (num_bits_this_page > BITS_PER_PAGE)
        num_bits_this_page = BITS_PER_PAGE;

      // Walk the page looking for a sequence of 0 bits of the appropriate
      // length. The outer loop steps through the page's bytes, the inner
      // one steps through each byte's bits.
      byte[] pagebuf = apage.getData();
      for (int byteptr = 0; num_bits_this_page > 0
          && current_run_length < run_size; byteptr++) {

        // initialize bit mask
        Byte mask = new Byte(new Integer(1).byteValue());
        byte tmpmask = mask.byteValue();

        // search the page for an empty run
        while (mask.intValue() != 0 && (num_bits_this_page > 0)
            && (current_run_length < run_size)) {

          // if a non-empty page is found
          if ((pagebuf[byteptr] & tmpmask) != 0) {
            current_run_start += current_run_length + 1;
            current_run_length = 0;
          } else {
            current_run_length++;
          }

          // advance to the next page
          tmpmask <<= 1;
          mask = new Byte(tmpmask);
          num_bits_this_page--;

        } // while

      } // inner loop

      // unpin the current space-map page
      Minibase.BufferManager.unpinPage(pgid, UNPIN_CLEAN);

    } // outer loop

    // check for disk full exception
    if (current_run_length < run_size) {
      throw new IllegalStateException("Not enough space left; allocate aborted");
    }

    // update the space map and return the resulting page id
    PageId firstpg = new PageId(current_run_start);
    set_bits(firstpg, run_size, 1);
    return firstpg;

  } // public PageId allocate_page(int run_size)

  /**
   * Deallocates a single page (i.e. run size 1) on disk.
   * 
   * @param pageno identifies the page to deallocate
   * @throws IllegalArgumentException if firstid is invalid
   */
  public void deallocate_page(PageId pageno) {
    deallocate_page(pageno, 1);
  }

  /**
   * Deallocates a set of pages on disk, given the run size.
   * 
   * @param firstid identifies the first page to deallocate
   * @param run_size number of pages to deallocate
   * @throws IllegalArgumentException if firstid or run_size is invalid
   */
  public void deallocate_page(PageId firstid, int run_size) {

    // validate the page id
    if ((firstid.pid < 0) || (firstid.pid >= num_pages)) {
      throw new IllegalArgumentException(
          "Invalid page number; deallocate aborted");
    }

    // validate the run size
    if (run_size < 1) {
      throw new IllegalArgumentException("Invalid run size; deallocate aborted");
    }

    // update the space map
    set_bits(firstid, run_size, 0);

  } // public void deallocate_page(PageId firstid, int run_size)

  /**
   * Reads the contents of the specified page from disk.
   * 
   * @param pageno identifies the page to read
   * @param page output param to hold the contents of the page
   * @throws IllegalArgumentException if pageno is invalid
   */
  public void read_page(PageId pageno, Page page) {

    // validate the page id
    if ((pageno.pid < 0) || (pageno.pid >= num_pages)) {
      throw new IllegalArgumentException("Invalid page number; read aborted");
    }

    // seek to the correct page on disk and read it
    try {
      fp.seek((long) (pageno.pid * PAGE_SIZE));
      fp.read(page.getData());
      read_cnt++;
    } catch (IOException exc) {
      Minibase.haltSystem(exc);
    }

  } // public void read_page(PageId pageno, Page page)

  /**
   * Writes the contents of the given page to disk.
   * 
   * @param pageno identifies the page to write
   * @param page holds the contents of the page
   * @throws IllegalArgumentException if pageno is invalid
   */
  public void write_page(PageId pageno, Page page) {

    // validate the page id
    if ((pageno.pid < 0) || (pageno.pid >= num_pages)) {
      throw new IllegalArgumentException("Invalid page number; write aborted");
    }

    // seek to the correct page on disk and write it
    try {
      fp.seek((long) (pageno.pid * PAGE_SIZE));
      fp.write(page.getData());
      write_cnt++;
    } catch (IOException exc) {
      Minibase.haltSystem(exc);
    }

  } // public void write_page(PageId pageno, Page page)

  /**
   * Adds a file entry to the header page(s); each entry contains the name of
   * the file and the PageId of the file's first page.
   * 
   * @throws IllegalArgumentException if fname or start_pageno is invalid
   */
  public void add_file_entry(String fname, PageId start_pageno) {

    // validate the arguments
    if (fname.length() > NAME_MAXLEN) {
      throw new IllegalArgumentException("Filename too long; add entry aborted");
    }
    if ((start_pageno.pid < 0) || (start_pageno.pid >= num_pages)) {
      throw new IllegalArgumentException(
          "Invalid page number; add entry aborted");
    }

    // does the file already exist?
    if (get_file_entry(fname) != null) {
      throw new IllegalArgumentException(
          "File entry already exists; add entry aborted");
    }

    // search the header pages for the entry slot
    boolean found = false;
    int free_slot = 0;
    DBHeaderPage hpage = new DBHeaderPage();
    PageId hpid = new PageId();
    PageId tmppid = new PageId();
    PageId nexthpid = new PageId(FIRST_PAGEID);
    do {

      // pin the next header page and get its next
      hpid.pid = nexthpid.pid;
      Minibase.BufferManager.pinPage(hpid, hpage, PIN_DISKIO);
      nexthpid = hpage.getNextPage();

      // search the header page for an empty entry
      int entry = 0;
      while (entry < hpage.getNumOfEntries()) {
        hpage.getFileEntry(tmppid, entry);
        if (tmppid.pid == INVALID_PAGEID) {
          break;
        }
        entry++;
      }

      // verify an empty slot was found
      if (entry < hpage.getNumOfEntries()) {
        free_slot = entry;
        found = true;
      } else if (nexthpid.pid != INVALID_PAGEID) {
        // unpin before continuing loop
        Minibase.BufferManager.unpinPage(hpid, UNPIN_CLEAN);
      }

    } while ((nexthpid.pid != INVALID_PAGEID) && (!found));

    // if necessary (and possible), add a new header page
    if (!found) {

      // allocate the new header page
      nexthpid = allocate_page();

      // set the next-page pointer on the previous directory page
      hpage.setNextPage(nexthpid);
      Minibase.BufferManager.unpinPage(hpid, UNPIN_DIRTY);

      // pin the newly-allocated directory page
      hpid.pid = nexthpid.pid;
      Minibase.BufferManager.pinPage(hpid, hpage, PIN_MEMCPY);
      hpage.initDefaults();
      free_slot = 0;

    } // if new header page

    // At this point, "hpid" has the page id of the header page with the free
    // slot; "hpage" has the directory_page pointer; "free_slot" is the entry
    // number in the directory where we're going to put the new file entry.
    hpage.setFileEntry(fname, start_pageno, free_slot);
    Minibase.BufferManager.unpinPage(hpid, UNPIN_DIRTY);

  } // public void add_file_entry(String fname, PageId start_pageno)

  /**
   * Deletes a file entry from the header page(s).
   * 
   * @throws IllegalArgumentException if fname is invalid
   */
  public void delete_file_entry(String fname) {

    // does the file really exist?
    if (get_file_entry(fname) == null) {
      throw new IllegalArgumentException(
          "File entry not found; delete entry aborted");
    }

    // search the header pages for the entry slot
    boolean found = false;
    int slot = 0;
    DBHeaderPage hpage = new DBHeaderPage();
    PageId hpid = new PageId();
    PageId tmppid = new PageId();
    PageId nexthpid = new PageId(0);
    do {

      // pin the next header page and get its next
      hpid.pid = nexthpid.pid;
      Minibase.BufferManager.pinPage(hpid, hpage, PIN_DISKIO);
      nexthpid = hpage.getNextPage();

      // search the header page for an the entry
      int entry = 0;
      String tmpname = null;
      while (entry < hpage.getNumOfEntries()) {
        tmpname = hpage.getFileEntry(tmppid, entry);
        if ((tmppid.pid != INVALID_PAGEID)
            && (tmpname.compareToIgnoreCase(fname) == 0)) {
          break;
        }
        entry++;
      }

      // verify the entry slot was found
      if (entry < hpage.getNumOfEntries()) {
        slot = entry;
        found = true;
      } else {
        // unpin before continuing loop
        Minibase.BufferManager.unpinPage(hpid, UNPIN_CLEAN);
      }

    } while ((nexthpid.pid != INVALID_PAGEID) && (!found));

    // have to delete record at hpnum:slot
    tmppid.pid = INVALID_PAGEID;
    hpage.setFileEntry("\0", tmppid, slot);
    Minibase.BufferManager.unpinPage(hpid, UNPIN_DIRTY);

  } // public void delete_file_entry(String fname)

  /**
   * Looks up the entry for the given file name.
   * 
   * @return PageId of the file's first page, or null if the file doesn't exist
   */
  public PageId get_file_entry(String fname) {

    // search the header pages for the entry slot
    boolean found = false;
    int slot = 0;
    DBHeaderPage hpage = new DBHeaderPage();
    PageId hpid = new PageId();
    PageId tmppid = new PageId();
    PageId nexthpid = new PageId(0);
    do {

      // pin the next header page and get its next
      hpid.pid = nexthpid.pid;
      Minibase.BufferManager.pinPage(hpid, hpage, PIN_DISKIO);
      nexthpid = hpage.getNextPage();

      // search the header page for an the entry
      int entry = 0;
      String tmpname;
      while (entry < hpage.getNumOfEntries()) {
        tmpname = hpage.getFileEntry(tmppid, entry);
        if ((tmppid.pid != INVALID_PAGEID)
            && (tmpname.compareToIgnoreCase(fname) == 0)) {
          break;
        }
        entry++;
      }

      // verify the entry slot was found
      if (entry < hpage.getNumOfEntries()) {
        slot = entry;
        found = true;
      }

      // unpin the page before continuing or exiting loop
      Minibase.BufferManager.unpinPage(hpid, UNPIN_CLEAN);

    } while ((nexthpid.pid != INVALID_PAGEID) && (!found));

    // return null if not found
    if (!found) {
      return null;
    }

    // otherwise, return the first page id
    PageId startpid = new PageId();
    hpage.getFileEntry(startpid, slot);
    return startpid;

  } // public PageId get_file_entry(String fname)

  /**
   * Print out the database's space map, a bitmap showing which pages are
   * currently allocated.
   */
  public void print_space_map() {

    int num_map_pages = (num_pages + BITS_PER_PAGE - 1) / BITS_PER_PAGE;
    int bit_number = 0;

    // this loop goes over each page in the space map
    PageId pgid = new PageId();
    System.out.println("num_map_pages = " + num_map_pages);
    System.out.print("num_pages = " + num_pages);
    for (int i = 0; i < num_map_pages; i++) { // start forloop01

      // pin the space-map page
      pgid.pid = 1 + i; // space map starts at page1
      Page apage = new Page();
      Minibase.BufferManager.pinPage(pgid, apage, PIN_DISKIO);

      // how many bits should we examine on this page?
      int num_bits_this_page = num_pages - i * BITS_PER_PAGE;
      if (num_bits_this_page > BITS_PER_PAGE) {
        num_bits_this_page = BITS_PER_PAGE;
      }
      System.out.println("\n\nnum_bits_this_page = " + num_bits_this_page
          + "\n");
      if (i > 0)
        System.out.print("\t");

      // Walk the page looking for a sequence of 0 bits of the appropriate
      // length. The outer loop steps through the page's bytes, the inner
      // one steps through each byte's bits.
      int pgptr = 0;
      byte[] pagebuf = apage.getData();
      int mask;
      for (; num_bits_this_page > 0; pgptr++) { // start forloop02

        for (mask = 1; mask < 256 && num_bits_this_page > 0; mask = (mask << 1), --num_bits_this_page, ++bit_number) {
          // start forloop03

          int bit = pagebuf[pgptr] & mask;
          if ((bit_number % 10) == 0) {
            if ((bit_number % 50) == 0) {
              if (bit_number > 0) {
                System.out.println("\n");
              }
              System.out.print("\t" + bit_number + ": ");
            } else {
              System.out.print(' ');
            }
          }

          if (bit != 0) {
            System.out.print("1");
          } else {
            System.out.print("0");
          }

        } // end of forloop03

      } // end of forloop02

      Minibase.BufferManager.unpinPage(pgid, UNPIN_CLEAN);

    } // end of forloop01

    System.out.println();

  } // public void print_space_map()

  /**
   * Sets 'run_size' bits in the space map to the given value, starting from
   * 'start_page'.
   */
  protected void set_bits(PageId start_page, int run_size, int bit) {

    // locate the run within the space map
    int first_map_page = start_page.pid / BITS_PER_PAGE + 1;
    int last_map_page = (start_page.pid + run_size - 1) / BITS_PER_PAGE + 1;
    int first_bit_no = start_page.pid % BITS_PER_PAGE;

    // the outer loop goes over all space-map pages we need to touch
    for (PageId pgid = new PageId(first_map_page); pgid.pid <= last_map_page; pgid.pid = pgid.pid + 1, first_bit_no = 0) {
      // Start forloop01

      // pin the space-map page
      Page pg = new Page();
      Minibase.BufferManager.pinPage(pgid, pg, PIN_DISKIO);
      byte[] pgbuf = pg.getData();

      // locate the piece of the run that fits on this page
      int first_byte_no = first_bit_no / 8;
      int first_bit_offset = first_bit_no % 8;
      int last_bit_no = first_bit_no + run_size - 1;

      if (last_bit_no >= BITS_PER_PAGE) {
        last_bit_no = BITS_PER_PAGE - 1;
      }

      int last_byte_no = last_bit_no / 8;

      // this loop actually flips the bits on the current page
      int cur_posi = first_byte_no;
      for (; cur_posi <= last_byte_no; ++cur_posi, first_bit_offset = 0) {
        // start forloop02

        int max_bits_this_byte = 8 - first_bit_offset;
        int num_bits_this_byte = (run_size > max_bits_this_byte ? max_bits_this_byte
            : run_size);

        int imask = 1;
        int temp;
        imask = ((imask << num_bits_this_byte) - 1) << first_bit_offset;
        Integer intmask = new Integer(imask);
        Byte mask = new Byte(intmask.byteValue());
        byte bytemask = mask.byteValue();

        if (bit == 1) {
          temp = (pgbuf[cur_posi] | bytemask);
          intmask = new Integer(temp);
          pgbuf[cur_posi] = intmask.byteValue();
        } else {
          temp = pgbuf[cur_posi] & (255 ^ bytemask);
          intmask = new Integer(temp);
          pgbuf[cur_posi] = intmask.byteValue();
        }
        run_size -= num_bits_this_byte;

      } // end of forloop02

      // unpin the space-map page
      Minibase.BufferManager.unpinPage(pgid, UNPIN_DIRTY);

    } // end of forloop01

  } // protected void set_bits(PageId start_page, int run_size, int bit)

} // public class DiskMgr implements GlobalConst

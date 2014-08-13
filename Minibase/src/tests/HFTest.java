package tests;

import global.Convert;
import global.Minibase;
import global.RID;
import heap.HeapFile;
import heap.HeapScan;

/**
 * Test suite for the heap layer.
 */
class HFTest extends TestDriver {

  /** The display name of the test suite. */
  private static final String TEST_NAME = "heap file tests";

  /**
   * Size of heap file to create in test cases (65 for multiple data pages; 6500
   * for multiple directory pages).
   */
  private static final int FILE_SIZE = 6500;

  /**
   * Test application entry point; runs all tests.
   */
  public static void main(String argv[]) {

    // create a clean Minibase instance
    HFTest hft = new HFTest();
    hft.create_minibase();

    // run all the test cases
    System.out.println("\n" + "Running " + TEST_NAME + "...");
    boolean status = PASS;
    status &= hft.test1();
    status &= hft.test2();
    status &= hft.test3();
    status &= hft.test4();

    // display the final results
    System.out.println();
    if (status != PASS) {
      System.out.println("Error(s) encountered during " + TEST_NAME + ".");
    } else {
      System.out.println("All " + TEST_NAME + " completed successfully!");
    }

  } // public static void main (String argv[])

  /**
   *
   */
  protected boolean test1() {
	//Start saving count of I/Os
	initCounts();
	saveCounts(null);

    System.out.println("\n  Test 1: Insert and scan fixed-size records\n");
    boolean status = PASS;
    RID rid = new RID();
    HeapFile f = null;

    System.out.println("  - Create a heap file\n");
    try {
      f = new HeapFile("file_1");
    } catch (Exception e) {
      status = FAIL;
      System.err.println("*** Could not create heap file\n");
      e.printStackTrace();
    }

    if (status == PASS
        && Minibase.BufferManager.getNumUnpinned() != Minibase.BufferManager
            .getNumBuffers()) {
      System.err.println("*** The heap file has left pages pinned\n");
      status = FAIL;
    }

    if (status == PASS) {
      System.out.println("  - Add " + FILE_SIZE + " records to the file\n");
      for (int i = 0; (i < FILE_SIZE) && (status == PASS); i++) {

        // fixed length record
        DummyRecord rec = new DummyRecord();
        rec.ival = i;
        rec.fval = (float) (i * 2.5);
        rec.name = "record" + i;

        try {
          rid = f.insertRecord(rec.toByteArray());
        } catch (Exception e) {
          status = FAIL;
          System.err.println("*** Error inserting record " + i + "\n");
          e.printStackTrace();
        }

        if (status == PASS
            && Minibase.BufferManager.getNumUnpinned() != Minibase.BufferManager
                .getNumBuffers()) {

          System.err.println("*** Insertion left a page pinned\n");
          status = FAIL;
        }
      }

      try {
        if (f.getRecCnt() != FILE_SIZE) {
          status = FAIL;
          System.err.println("*** File reports " + f.getRecCnt()
              + " records, not " + FILE_SIZE + "\n");
        }
      } catch (Exception e) {
        status = FAIL;
        System.out.println("" + e);
        e.printStackTrace();
      }
    }

    // In general, a sequential scan won't be in the same order as the
    // insertions. However, we're inserting fixed-length records here, and
    // in this case the scan must return the insertion order.

    HeapScan scan = null;

    if (status == PASS) {
      System.out.println("  - Scan the records just inserted\n");

      try {
        scan = f.openScan();
      } catch (Exception e) {
        status = FAIL;
        System.err.println("*** Error opening scan\n");
        e.printStackTrace();
      }

      if (status == PASS
          && Minibase.BufferManager.getNumUnpinned() == Minibase.BufferManager
              .getNumBuffers()) {
        System.err
            .println("*** The heap-file scan has not pinned the first page\n");
        status = FAIL;
      }
    }

    if (status == PASS) {
      int len, i = 0;
      DummyRecord rec = null;
      byte[] tuple = null;

      boolean done = false;
      while (scan.hasNext()) {
        try {
          tuple = scan.getNext(rid);
        } catch (Exception e) {
          status = FAIL;
          e.printStackTrace();
        }

        if (status == PASS && !done) {
          try {
            rec = new DummyRecord(tuple);
          } catch (Exception e) {
            System.err.println("" + e);
            e.printStackTrace();
          }

          len = tuple.length;
          if (len != rec.length()) {
            System.err.println("*** Record " + i + " had unexpected length "
                + len + "\n");
            status = FAIL;
            break;
          } else if (Minibase.BufferManager.getNumUnpinned() == Minibase.BufferManager
              .getNumBuffers()) {
            System.err.println("On record " + i + ":\n");
            System.err.println("*** The heap-file scan has not left its "
                + "page pinned\n");
            status = FAIL;
            break;
          }
          String name = ("record" + i);

          if ((rec.ival != i) || (rec.fval != (float) i * 2.5)
              || (!name.equals(rec.name))) {
            System.err.println("*** Record " + i
                + " differs from what we inserted\n");
            System.err.println("rec.ival: " + rec.ival + " should be " + i
                + "\n");
            System.err.println("rec.fval: " + rec.fval + " should be "
                + (i * 2.5) + "\n");
            System.err.println("rec.name: " + rec.name + " should be " + name
                + "\n");
            status = FAIL;
            break;
          }
        }
        ++i;
      }

      // If it gets here, then the scan should be completed
      if (status == PASS) {
        scan.close();
        if (Minibase.BufferManager.getNumUnpinned() != Minibase.BufferManager
            .getNumBuffers()) {
          System.err.println("*** The heap-file scan has not unpinned "
              + "its page after finishing\n");
          status = FAIL;
        } else if (i != (FILE_SIZE)) {
          status = FAIL;

          System.err.println("*** Scanned " + i + " records instead of "
              + FILE_SIZE + "\n");
        }
      }
    }

    if (status == PASS)
      System.out.println("  Test 1 completed successfully.\n");

    //save I/O counts
    saveCounts("test1");
    saveCounts(null);

    return status;

  } // protected boolean test1()

  /**
   *
   */
  protected boolean test2() {

    System.out.println("\n  Test 2: Delete fixed-size records\n");
    boolean status = PASS;
    HeapScan scan = null;
    RID rid = new RID();
    HeapFile f = null;

    System.out.println("  - Open the same heap file as test 1\n");
    try {
      f = new HeapFile("file_1");
    } catch (Exception e) {
      status = FAIL;
      System.err.println(" Could not open heap file");
      e.printStackTrace();
    }

    if (status == PASS) {
      System.out.println("  - Delete half the records\n");
      try {
        scan = f.openScan();
      } catch (Exception e) {
        status = FAIL;
        System.err.println("*** Error opening scan\n");
        e.printStackTrace();
      }
    }

    if (status == PASS) {
      int i = 0;
      boolean done = false;

      while (scan.hasNext()) {
        try {
          scan.getNext(rid);
        } catch (Exception e) {
          status = FAIL;
          e.printStackTrace();
        }

        if (!done && status == PASS) {
          boolean odd = true;
          if (i % 2 == 1)
            odd = true;
          if (i % 2 == 0)
            odd = false;
          if (odd) { // Delete the odd-numbered ones.
            try {
              f.deleteRecord(rid);
              status = PASS;
            } catch (Exception e) {
              status = FAIL;
              System.err.println("*** Error deleting record " + i + "\n");
              e.printStackTrace();
              break;
            }
          }
        }
        ++i;
      }
    }

    try {
      scan.close(); // destruct scan!!!!!!!!!!!!!!!
    } catch (Exception e) {
    }
    scan = null;

    if (status == PASS
        && Minibase.BufferManager.getNumUnpinned() != Minibase.BufferManager
            .getNumBuffers()) {

      System.out.println("\nt2: in if: Number of unpinned buffers: "
          + Minibase.BufferManager.getNumUnpinned() + "\n");
      System.err.println("t2: in if: getNumbfrs: "
          + Minibase.BufferManager.getNumBuffers() + "\n");

      System.err.println("*** Deletion left a page pinned\n");
      status = FAIL;
    }

    if (status == PASS) {
      System.out.println("  - Scan the remaining records\n");
      try {
        scan = f.openScan();
      } catch (Exception e) {
        status = FAIL;
        System.err.println("*** Error opening scan\n");
        e.printStackTrace();
      }
    }

    if (status == PASS) {
      int i = 0;
      DummyRecord rec = null;
      byte[] tuple = null;
      boolean done = false;

      while (scan.hasNext()) {
        try {
          tuple = scan.getNext(rid);
        } catch (Exception e) {
          status = FAIL;
          e.printStackTrace();
        }

        if (!done && status == PASS) {
          try {
            rec = new DummyRecord(tuple);
          } catch (Exception e) {
            System.err.println("" + e);
            e.printStackTrace();
          }

          if ((rec.ival != i) || (rec.fval != (float) i * 2.5)) {
            System.err.println("*** Record " + i
                + " differs from what we inserted\n");
            System.err.println("rec.ival: " + rec.ival + " should be " + i
                + "\n");
            System.err.println("rec.fval: " + rec.fval + " should be "
                + (i * 2.5) + "\n");
            status = FAIL;
            break;
          }
          i += 2; // Because we deleted the odd ones...
        }
      }
      scan.close();
          //save I/O counts
	      saveCounts("test2");
	      saveCounts(null);

    }

    if (status == PASS)
      System.out.println("  Test 2 completed successfully.\n");
    return status;

  } // protected boolean test2()

  /**
   *
   */
  protected boolean test3() {

    System.out.println("\n  Test 3: Update fixed-size records\n");
    boolean status = PASS;
    HeapScan scan = null;
    RID rid = new RID();
    HeapFile f = null;

    System.out.println("  - Open the same heap file as tests 1 and 2\n");
    try {
      f = new HeapFile("file_1");
    } catch (Exception e) {
      status = FAIL;
      System.err.println("*** Could not create heap file\n");
      e.printStackTrace();
    }

    if (status == PASS) {
      System.out.println("  - Change the records\n");
      try {
        scan = f.openScan();
      } catch (Exception e) {
        status = FAIL;
        System.err.println("*** Error opening scan\n");
        e.printStackTrace();
      }
    }

    if (status == PASS) {

      int i = 0;
      DummyRecord rec = null;
      byte[] tuple = null;
      boolean done = false;

      while (scan.hasNext()) {
        try {
          tuple = scan.getNext(rid);
        } catch (Exception e) {
          status = FAIL;
          e.printStackTrace();
        }

        if (!done && status == PASS) {
          try {
            rec = new DummyRecord(tuple);
          } catch (Exception e) {
            System.err.println("" + e);
            e.printStackTrace();
          }

          rec.fval = (float) 7 * i; // We'll check that i==rec.ival below.

          byte[] newTuple = null;
          try {
            newTuple = rec.toByteArray();
          } catch (Exception e) {
            status = FAIL;
            System.err.println("" + e);
            e.printStackTrace();
          }
          try {
            f.updateRecord(rid, newTuple);
            status = PASS;
          } catch (Exception e) {
            status = FAIL;
            e.printStackTrace();
          }

          if (status != PASS) {
            System.err.println("*** Error updating record " + i + "\n");
            break;
          }
          i += 2; // Recall, we deleted every other record above.
        }
      }
    }

    scan.close();
    scan = null;

    if (status == PASS
        && Minibase.BufferManager.getNumUnpinned() != Minibase.BufferManager
            .getNumBuffers()) {

      System.out.println("t3, Number of unpinned buffers: "
          + Minibase.BufferManager.getNumUnpinned() + "\n");
      System.err.println("t3, getNumbfrs: "
          + Minibase.BufferManager.getNumBuffers() + "\n");

      System.err.println("*** Updating left pages pinned\n");
      status = FAIL;
    }

    if (status == PASS) {
      System.out.println("  - Check that the updates are really there\n");
      try {
        scan = f.openScan();
      } catch (Exception e) {
        status = FAIL;
        e.printStackTrace();
      }
      if (status == FAIL) {
        System.err.println("*** Error opening scan\n");
      }
    }

    if (status == PASS) {
      int i = 0;
      DummyRecord rec = null;
      DummyRecord rec2 = null;
      byte[] tuple = null;
      byte[] tuple2 = null;
      boolean done = false;

      while (scan.hasNext()) {
        try {
          tuple = scan.getNext(rid);
        } catch (Exception e) {
          status = FAIL;
          e.printStackTrace();
        }

        if (!done && status == PASS) {
          try {
            rec = new DummyRecord(tuple);
          } catch (Exception e) {
            System.err.println("" + e);
          }

          // While we're at it, test the getRecord method too.
          try {
            tuple2 = f.selectRecord(rid);
          } catch (Exception e) {
            status = FAIL;
            System.err.println("*** Error getting record " + i + "\n");
            e.printStackTrace();
            break;
          }

          try {
            rec2 = new DummyRecord(tuple2);
          } catch (Exception e) {
            System.err.println("" + e);
            e.printStackTrace();
          }

          if ((rec.ival != i) || (rec.fval != (float) i * 7)
              || (rec2.ival != i) || (rec2.fval != i * 7)) {
            System.err
                .println("*** Record " + i + " differs from our update\n");
            System.err.println("rec.ival: " + rec.ival + " should be " + i
                + "\n");
            System.err.println("rec.fval: " + rec.fval + " should be "
                + (i * 7.0) + "\n");
            status = FAIL;
            break;
          }

        }
        i += 2; // Because we deleted the odd ones...
      }
      scan.close();
          //save I/O counts
	      saveCounts("test3");
	      saveCounts(null);

    }

    if (status == PASS)
      System.out.println("  Test 3 completed successfully.\n");
    return status;

  } // protected boolean test3()

  /**
   *
   */
  protected boolean test4() {

    System.out.println("\n  Test 4: Test some error conditions\n");
    boolean status = PASS;
    HeapScan scan = null;
    RID rid = new RID();
    HeapFile f = null;

    try {
      f = new HeapFile("file_1");
    } catch (Exception e) {
      status = FAIL;
      System.err.println("*** Could not create heap file\n");
      e.printStackTrace();
    }

    if (status == PASS) {
      System.out.println("  - Try to change the size of a record\n");
      try {
        scan = f.openScan();
      } catch (Exception e) {
        status = FAIL;
        System.err.println("*** Error opening scan\n");
        e.printStackTrace();
      }
    }

    // The following is to test whether tinkering with the size of
    // the tuples will cause any problem.

    if (status == PASS) {
      DummyRecord rec = null;
      byte[] tuple = null;

      try {
        tuple = scan.getNext(rid);
        if (tuple == null) {
          status = FAIL;
        }
      } catch (Exception e) {
        status = FAIL;
        e.printStackTrace();
      }
      if (status == FAIL) {
        System.err.println("*** Error reading first record\n");
      }

      if (status == PASS) {
        try {
          rec = new DummyRecord(tuple);
        } catch (Exception e) {
          System.err.println("" + e);
          status = FAIL;
        }
        byte[] newTuple = null;
        try {
          rec.name = "short";
          newTuple = rec.toByteArray();
        } catch (Exception e) {
          System.err.println("" + e);
          e.printStackTrace();
        }
        try {
          f.updateRecord(rid, newTuple);
          status = PASS;
        } catch (IllegalArgumentException e) {
          status = FAIL;
          System.out.println("  ** Shortening a record");
          System.out.println("  --> Failed as expected \n");
        } catch (Exception e) {
          e.printStackTrace();
        }

        if (status == PASS) {
          status = FAIL;
          System.err.println("######The expected exception was not thrown\n");
        } else {
          status = PASS;
        }
      }

      if (status == PASS) {
        try {
          rec = new DummyRecord(tuple);
        } catch (Exception e) {
          System.err.println("" + e);
          e.printStackTrace();
        }

        byte[] newTuple = null;
        try {
          rec.name = "this one's longer!";
          newTuple = rec.toByteArray();
        } catch (Exception e) {
          System.err.println("" + e);
          e.printStackTrace();
        }
        try {
          f.updateRecord(rid, newTuple);
          status = PASS;
        } catch (IllegalArgumentException e) {
          status = FAIL;
          System.out.println("  ** Lengthening a record");
          System.out.println("  --> Failed as expected \n");
        } catch (Exception e) {
          e.printStackTrace();
        }

        if (status == PASS) {
          status = FAIL;
          System.err.println("The expected exception was not thrown\n");
        } else {
          status = PASS;
        }
      }
    }

    scan.close();
    scan = null;

    if (status == PASS) {
      System.out.println("  - Try to insert a record that's too long");
      byte[] record = new byte[PAGE_SIZE + 4];
      try {
        rid = f.insertRecord(record);
        status = PASS;
      } catch (IllegalArgumentException e) {
        status = FAIL;
        System.out.println("  --> Failed as expected \n");
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (status == PASS) {
        status = FAIL;
        System.err.println("The expected exception was not thrown\n");
      } else {
        status = PASS;
      }
    }

    if (status == PASS)
      System.out.println("  Test 4 completed successfully.\n");

      //save and print I/O counts
      saveCounts("test4");
      printSummary(10);

    return (status == PASS);

  } // protected boolean test4()

  /**
   * Used in fixed-length record test cases.
   */
  class DummyRecord {

    public int ival;

    public float fval;

    public String name;

    /** Constructs with default values. */
    public DummyRecord() {
    }

    /** Constructs from a byte array. */
    public DummyRecord(byte[] data) {
      ival = Convert.getIntValue(0, data);
      fval = Convert.getFloatValue(4, data);
      name = Convert.getStringValue(8, data, NAME_MAXLEN);
    }

    /** Gets a byte array representation. */
    public byte[] toByteArray() {
      byte[] data = new byte[length()];
      Convert.setIntValue(ival, 0, data);
      Convert.setFloatValue(fval, 4, data);
      Convert.setStringValue(name, 8, data);
      return data;
    }

    /** Gets the length of the record. */
    public int length() {
      return 4 + 4 + name.length();
    }

  } // class DummyRecord

} // class HFTest extends TestDriver

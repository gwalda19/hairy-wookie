package heap;

/**
 * A heap file page containing actual data records.
 */
class DataPage extends HFPage {

  /**
   * Default constructor; creates a data page with default values.
   */
  public DataPage() {
    super();
    setType(HeapFile.DATA_PAGE);
  }
  
} // class DataPage extends HFPage

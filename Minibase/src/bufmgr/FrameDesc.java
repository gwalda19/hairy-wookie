package bufmgr;

import global.PageId;

/**
 *
 *  @author David Shanline
 *  @author David Gwalthney
 * 
 *  Each frame has certain states associated with it. These states include
 *  whether the frame is dirty, whether it includes valid data (data which
 *  reflects data in a disk page), and if it includes valid data then what
 *  is the disk page number of the data, how many callers have pins on the
 *  data (the pin count), and any other information you wish to store, for
 *  example information relevant to the replacement algorithm.
 * 
 *  The below class stores the states as described above. This is the frame
 *  table. The members are protected so that they can be used by elements
 *  residing in its own class or classes in the same package.
 * 
 */
class FrameDesc
{
    protected boolean dirty;
    protected int pin_count;
    protected boolean valid;
    protected boolean refbit;
    protected PageId pageno; 


   /**
    *
    * Constructs a frame descriptor by initializing member data.  
    *
    */
    public FrameDesc()
    {
        dirty = false;
        pin_count = 0;
        valid = false;
        refbit = false;
        pageno = null;
    }
}

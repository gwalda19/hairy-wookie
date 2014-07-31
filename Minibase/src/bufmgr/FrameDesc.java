package bufmgr;

import global.PageId;

/**
 *
 * @author Bill
 */
class FrameDesc
{
    protected boolean dirty;
    protected int pin_count;
    protected boolean valid;
    protected boolean refbit;
    protected PageId pageno; 

    public FrameDesc()
    {
        dirty = false;
        pin_count = 0;
        valid = false;
        refbit = false;
        pageno = null;
    }
}

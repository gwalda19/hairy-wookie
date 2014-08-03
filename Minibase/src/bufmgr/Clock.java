package bufmgr;

/**
 *
 * @author Not Bill
 */
class Clock extends Replacer
{
    private int counter;
    
    public Clock(BufMgr bufmgr)
    {
        super(bufmgr);
        counter = 0;
    }
    
    @Override
    public void newPage(FrameDesc fdesc)
    {
    }

    @Override
    public void freePage(FrameDesc fdesc)
    {   
    }

    @Override
    public void pinPage(FrameDesc fdesc)
    {
    }

    @Override
    public void unpinPage(FrameDesc fdesc)
    {
    }

    @Override
    public int pickVictim()
    {
        boolean found = false;
        
        // Go thru all the frames at most twice
        for (int i=0; i<(frametab.length*2); i++)
        {
            if (!frametab[counter].valid)
            {
                // Any invalid (no data) frames are choosen first
                found = true;
                break;
            }
            else if (frametab[counter].pin_count == 0)
            {
                // This frame is not pinned
                if (!frametab[counter].refbit)
                {
                    // Found unpinned that has not been referenced
                    // recently
                    found = true;
                    break;
                }
                else
                {
                    // Don't choose this, set reference bit and
                    // keep going
                    frametab[counter].refbit = false;                 
                }
            }
            
            //0, 1, 2 ... 99
            counter = (counter + 1) % frametab.length;
        }
        
        if (found)
        {
            return counter;
        }
        else
        {
            // Could not find a frame, return error
            return -1;
        }
    }
}

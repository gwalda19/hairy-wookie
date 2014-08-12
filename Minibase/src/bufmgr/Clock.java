package bufmgr;

/**
 *
 * @author David Shanline
 * @auther David Gwalthney
 * 
 * The Clock.java class extends the abstract class Replacer, and will be used as the replacement policy
 * by the buffer manager class. We override the pickVictim() method in this class with the implementation
 * of the clock replacement policy. In the pickVictim() method, we iterate through the frames in the array
 * of frame descriptions looking for an available frame to store the current page.  First we check if the
 * frame contains any valid data. If the frame is empty then we can store data to that frame, so we select
 * it. If the frame contains valid data, we then check the frames pin count. If the pin count is greater
 * than 0, then the frame is not a candidate for replacement so we increment the counter in order to check
 * the next frame. If the pin count is 0, we then consider its reference bit which indicates how recently
 * the frame was used. If the current frame has the reference bit turned on, then the algorithm turns it
 * off and increments the counter to the next frame. If the frames pin count is 0 and the reference bit is
 * off, then we have found an unpinned frame that has not been used recently, so we chose it and return
 * its location (counter).
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

package bufmgr;

/**
 *
 * @author Bill
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
        
        for (int i=0; i<(frametab.length*2); i++)
        {
            if (!frametab[counter].valid)
            {
                found = true;
                break;
            }
            else if (frametab[counter].pin_count == 0)
            {
                if (!frametab[counter].refbit)
                {
                    found = true;
                    break;
                }
                else
                {
                    frametab[counter].refbit = false;                 
                }
            }
            
            counter = (counter + 1) % frametab.length;
        }
        
        if (found)
        {
            return counter;
        }
        else
        {
            return -1;
        }
    }
}

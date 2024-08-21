package com.jaianper.sigagent.bo;

import com.jaianper.common.error.GenericException;
import com.jaianper.common.service.AbstractService;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

/**
 *
 * @author jaianper
 */
public class SIGDaemon extends AbstractService
{
    private final Timer timer;
    private final TimerTask timerTask;
    private long timerSchedulePeriod;
    
    public static final long DEFAULT_TIMER_SCHEDULE_PERIOD = 30000L;
    
    public SIGDaemon(final SIGDaemonInterface sigDaemonInterface) throws GenericException
    {
        super();
        
        timer = new Timer("SIGDaemon_"+System.currentTimeMillis(), false);
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    sigDaemonInterface.publicSystemDetail();
                }
                catch (Exception e)
                {
                    Logger.getLogger(SIGDaemon.class).error("Error trying to send system information.", e);
                }
            }
        };
    }
    
    public void start()
    {
        timer.schedule(timerTask, this.timerSchedulePeriod, this.timerSchedulePeriod);
    }
    
    public void stop()
    {
        if (timer != null)
        {
            try
            {
                timer.cancel();
                timer.purge();
            }
            catch (Exception e)
            {
                Logger.getLogger(SIGDaemon.class).error("Error stopping the Timer", e);
            }
        }
    }
    
    /**
     * 
     * @param timerSchedulePeriod Time in milliseconds between successive task executions.
     */
    public void setTimerSchedulePeriod(long timerSchedulePeriod)
    {
        this.timerSchedulePeriod = timerSchedulePeriod;
    }
}

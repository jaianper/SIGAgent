package com.jaianper.sigagent.bs;

import com.jaianper.common.conf.Configuration;
import com.jaianper.sigagent.bo.SIGDaemon;
import com.jaianper.sigagent.bo.SystemInfoGatherer;
import com.jaianper.common.error.GenericException;
import java.util.Map;

/**
 *
 * @author jaianper
 */
public class SystemInfoGathererServiceImpl extends SIGDaemon implements SystemInfoGathererService
{
    public SystemInfoGathererServiceImpl() throws GenericException
    {
        super(SystemInfoGatherer.getInstance().getSIGDaemonInterface());
        setTimerSchedulePeriod(Configuration.getConfigurationParameter("TIMER_SCHEDULE_PERIOD", DEFAULT_TIMER_SCHEDULE_PERIOD));
        start();
    }
    
    @Override
    public void restartTimerSchedule(long timerSchedulePeriod) throws GenericException
    {
        setTimerSchedulePeriod(timerSchedulePeriod);
        stop();
        start();
    }
    
    @Override
    public Map<String, Object> getOverallSystemDetail() throws GenericException
    {
        return SystemInfoGatherer.getInstance().getOverallSystemDetail();
    }

    @Override
    public Map<String, Object> getSystemDetail(String keys) throws GenericException
    {
        return SystemInfoGatherer.getInstance().getSystemDetail(keys);
    }
}

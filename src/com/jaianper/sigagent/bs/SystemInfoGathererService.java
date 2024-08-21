package com.jaianper.sigagent.bs;

import com.jaianper.common.error.GenericException;
import java.util.Map;

/**
 *
 * @author jaianper
 */
public interface SystemInfoGathererService
{
    public void restartTimerSchedule(long timerSchedulePeriod) throws GenericException;
    
    public Map<String, Object> getOverallSystemDetail() throws GenericException;
    
    public Map<String, Object> getSystemDetail(String keys) throws GenericException;
}

package com.jaianper.sigagent;

import com.jaianper.common.conf.Configuration;
import com.jaianper.common.conf.Registry;
import com.jaianper.common.error.GenericException;
import com.jaianper.sigagent.bs.SystemInfoGathererService;
import java.awt.GraphicsEnvironment;
import org.apache.log4j.Logger;

/**
 *
 * @author jaianper
 */
public class SIGAgent extends Configuration
{
    public SIGAgent(String[] args) throws GenericException
    {
        super(args);
        Logger.getLogger(SIGAgent.class).info("******************** System Info Gatherer Agent ********************");
    }
    
    private void startAgent() throws GenericException
    {
        Registry.loadResource(SystemInfoGathererService.class);
        
        Logger.getLogger(SIGAgent.class).info("The agent has been started successfully!");
    }
    
    @Override
    public void postConfigurationUI() throws GenericException
    {
        super.postConfigurationUI();
        printSystemParameters();
        startAgent();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            SIGAgent agent = new SIGAgent(args);
            
            if (GraphicsEnvironment.isHeadless())
            {
                agent.setConfigurationParameters();
                agent.printSystemParameters();
                agent.startAgent();
            }
            else
            {
                agent.initConfigurationUI();
            }
        }
        catch (GenericException ex)
        {
            Logger.getLogger(SIGAgent.class).error("Error starting agent.", ex);
        }
    }
}

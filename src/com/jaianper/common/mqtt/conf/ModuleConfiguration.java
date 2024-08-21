package com.jaianper.common.mqtt.conf;

import com.jaianper.common.conf.Configuration;
import com.jaianper.common.conf.IMessageBroker;
import java.io.IOException;
import org.apache.log4j.Logger;
import com.jaianper.common.conf.IModuleConfiguration;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jaianper
 */
public class ModuleConfiguration implements IModuleConfiguration
{
    public enum Param
    {
        MQTT_BROKER_PUBLISH("", "", true),
        TOPIC_TO_PUBLISH_MESSAGES("", "", true),
        TIMER_SCHEDULE_PERIOD("", "", true);

        private final String spec;
        private final String defValue;
        private final boolean required;

        Param(String spec, String defValue, boolean required)
        {
            this.spec = spec;
            this.defValue = defValue;
            this.required = required;
        }
    }
    
    @Override
    public IMessageBroker getMessageBroker()
    {
        return new MessageBroker();
    }

    @Override
    public void configureParameters()
    {
        System.out.println("===> Please enter the values ​​for the MQTT configuration parameters:");
        System.out.println("");
        try
        {
            for(Param p : Param.values())
            {
                Configuration.inputSystemParameter(p.name(), p.spec, Configuration.getConfigurationParameter(p.name(), p.defValue));
            }
        }
        catch(IOException ioex)
        {
            Logger.getLogger(ModuleConfiguration.class).error("Error setting configuration parameters for MQTT.", ioex);
        }
        System.out.println("");
    }
    
    @Override
    public List<String> listRequiredParameters()
    {
        List<String> lstParameters = new ArrayList<>();
        
        for(Param param : Param.values())
        {
            if(param.required)
            {
                lstParameters.add(param.name());
            }
        }
        
        return lstParameters;
    }
    
    @Override
    public List<String> listAllParameters()
    {
        List<String> lstParameters = new ArrayList<>();
        
        for(Param param : Param.values())
        {
            lstParameters.add(param.name());
        }
        
        return lstParameters;
    }
}

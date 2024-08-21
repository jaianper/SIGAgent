package com.jaianper.sigagent.controller;

import com.jaianper.common.conf.Configuration;
import com.jaianper.common.error.GenericException;
import com.jaianper.sigagent.gui.SIGConfigurationFI;
import com.jaianper.sigagent.gui.SIGConfigurationUI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 *
 * @author jaianper
 */
public class SIGConfigurationController
{
    private final Map<String, SIGConfigurationFI> mConfigurationField = new HashMap<>();
    private final Configuration conf;
    
    public SIGConfigurationController(Configuration conf)
    {
        this.conf = conf;
    }
    
    public String getApplicationName()
    {
        return Configuration.getConfigurationParameter("APPLICATION_NAME", "SIGAgent");
    }
    
    public List<String> getMessageBrokerNames()
    {
        return Configuration.getMessageBrokerNames();
    }
    
    public Map<String, String> getConfigurationParameters(String messageBroker) throws GenericException
    {
        return Configuration.getConfigurationParameters(messageBroker);
    }
    
    public void addConfigurationField(SIGConfigurationFI cfi)
    {
        mConfigurationField.put(cfi.getName(), cfi);
    }
    
    public String getConfigurationValue(String key)
    {
        return mConfigurationField.get(key).getValue();
    }
    
    public void saveConfiguration() throws GenericException
    {
        for(Map.Entry<String, SIGConfigurationFI> entry : mConfigurationField.entrySet())
        {
            SIGConfigurationFI configurationField = entry.getValue();
            String defValue = Configuration.getConfigurationParameter(configurationField.getName(), "");
            Configuration.setSystemParameter(configurationField.getName(), configurationField.getValue(), defValue);
        }
        
        conf.postConfigurationUI();
    }
    
    public void initConfigurationUI()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                SIGConfigurationUI s = new SIGConfigurationUI(SIGConfigurationController.this);
                s.setSize(500, 500);
                s.setVisible(true);
            }
        });
    }
}

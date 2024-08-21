package com.jaianper.common.conf;

import java.util.List;

/**
 *
 * @author jaianper
 */
public interface IModuleConfiguration
{
    public IMessageBroker getMessageBroker();
    
    public void configureParameters();
    
    public List<String> listRequiredParameters();
    
    public List<String> listAllParameters();
}

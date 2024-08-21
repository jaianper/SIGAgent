package com.jaianper.common.conf;

/**
 *
 * @author jaianper
 */
public abstract class IMessageBroker
{
    protected final String clientId;
    protected final String topic;
    
    public IMessageBroker()
    {
        clientId = Configuration.getConfigurationParameter("APPLICATION_NAME", "");
        topic = Configuration.getConfigurationParameter("TOPIC_TO_PUBLISH_MESSAGES", "sigagent-monitoring-push");
    }
    
    public abstract void publish(String message);
}

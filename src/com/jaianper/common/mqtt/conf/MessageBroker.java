package com.jaianper.common.mqtt.conf;

import com.jaianper.common.conf.IMessageBroker;
import org.apache.log4j.Logger;

/**
 *
 * @author jaianper
 */
public class MessageBroker extends IMessageBroker
{
    // TODO: MQTT Client implementation
    
    public MessageBroker()
    {
        super();
    }
    
    @Override
    public void publish(String message)
    {
        Logger.getLogger(MessageBroker.class).info(message);
        // TODO: implementation for publishing messages
    }
}

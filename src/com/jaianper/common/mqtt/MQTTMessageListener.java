package com.jaianper.common.mqtt;

/**
 * @author jaianper
 */
public interface MQTTMessageListener
{
    void onMessage(String message);
}

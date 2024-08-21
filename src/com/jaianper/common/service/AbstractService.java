package com.jaianper.common.service;

import com.jaianper.common.error.GenericException;
import org.apache.log4j.Logger;

/**
 *
 * @author jaianper
 */
public abstract class AbstractService
{
    public AbstractService () throws GenericException
    {
        Logger.getLogger(AbstractService.class).info("Instantiating service: "+getClass().getCanonicalName());
    }
}

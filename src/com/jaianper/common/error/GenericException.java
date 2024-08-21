package com.jaianper.common.error;

/**
 *
 * @author jaianper
 */
public class GenericException extends Exception
{
    public GenericException(String message)
    {
        super(message);
    }
    
    public GenericException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

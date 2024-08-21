package com.jaianper.common.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaianper.common.error.GenericException;
import com.jaianper.common.util.Tools;
import com.jaianper.sigagent.controller.SIGConfigurationController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author jaianper
 */
public class Configuration
{
    private InputStreamReader isr;
    private static BufferedReader br;
    private static final Map<String, Object> CONFIGURATIONPARAMETERS = new LinkedHashMap<>();
    private static IModuleConfiguration moduleConfiguration;
    private static IMessageBroker messageBroker;
    private static ObjectMapper objectMapper;
    private static final String MAINCONFIGURATIONPARAMETER = "MESSAGE_BROKER";
    private boolean isLoaded;
    
    private enum MessageBroker
    {
        MQTT(com.jaianper.common.mqtt.conf.ModuleConfiguration.class);
        
        private final Class configClass;
        
        <C extends IModuleConfiguration> MessageBroker(Class<C> configClass)
        {
            this.configClass = configClass;
        }
        
        static <C extends IModuleConfiguration> Class<C> getConfigClass(String messageBroker)
        {
            for(MessageBroker mb : MessageBroker.values())
            {
                if(mb.name().equals(messageBroker.toUpperCase()))
                {
                    return mb.configClass;
                }
            }
            return MQTT.configClass;
        }
    }
    
    public static List<String> getMessageBrokerNames()
    {
        List<String> lstNames = new ArrayList<>();
        
        for(MessageBroker mb : MessageBroker.values())
        {
            lstNames.add(mb.name());
        }
        
        return lstNames;
    }
    
    protected Configuration(String[] args) throws GenericException
    {
        //PropertyConfigurator.configure("log4j.properties");
        PropertyConfigurator.configure(Tools.getResource("log4j.properties"));
        
        isr = new InputStreamReader(System.in);
        br = new BufferedReader(isr);

        try
        {
            Properties properties = new Properties();
            
            properties.load(Tools.getResourceAsStream("manager.properties"));

            Set<String> propertyNames = properties.stringPropertyNames();

            for(String propertyName : propertyNames)
            {
                setSystemParameter(propertyName, properties.getProperty(propertyName));
            }

            // Se cargan configuraciones pasadas como argumentos
            if(args.length > 0)
            {
                for(String arg : args)
                {
                    String[] assign = arg.split("=");

                    if(assign.length == 2)
                    {
                        setSystemParameter(assign[0], assign[1]);
                        if(MAINCONFIGURATIONPARAMETER.equals(assign[0]))isLoaded = true;
                    }
                }
            }

            Tools.loadLibraries();
        }
        catch (IOException ex)
        {
            throw new GenericException("Error loading system parameters.", ex);
        }
    }
    
    protected void initConfigurationUI()
    {
        SIGConfigurationController sigCController = new SIGConfigurationController(this);
        sigCController.initConfigurationUI();
    }
    
    public void postConfigurationUI() throws GenericException
    {
        Logger.getLogger(Configuration.class).info("Setup complete.");
        isLoaded = true;
    }
    
    public static <O> O getConfigurationParameter(String key, O defaultValue)
    {
        Object result = CONFIGURATIONPARAMETERS.get(key);
        
        if(result == null)
        {
            return defaultValue;
        }
        
        if(defaultValue instanceof Integer)
        {
            return (O) Integer.valueOf(result+"");
        }
        else if(defaultValue instanceof Long)
        {
            return (O) Long.valueOf(result+"");
        }
        else
        {
            return (O) result;
        }
    }
    
    protected void setConfigurationParameters() throws GenericException
    {
        Logger.getLogger(Configuration.class).info("Setting configuration parameters...");
        
        System.out.println("===> Initial configuration:");
        System.out.println("");
        
        try
        {
            if(!isLoaded)
            {
                inputSystemParameter(MAINCONFIGURATIONPARAMETER, "", CONFIGURATIONPARAMETERS.get(MAINCONFIGURATIONPARAMETER)+"");
            }
            
            String mBroker = CONFIGURATIONPARAMETERS.get(MAINCONFIGURATIONPARAMETER)+"";
            
            Class<? extends IModuleConfiguration> clazz = MessageBroker.getConfigClass(mBroker);
            Constructor<? extends IModuleConfiguration> ctor = clazz.getConstructor();
            moduleConfiguration = ctor.newInstance();
            
            if(!isLoaded)
            {
                moduleConfiguration.configureParameters();
            }
            
            isLoaded = true;
        }
        catch (Exception ex)
        {
            throw new GenericException("An error occurred while configuring parameters for the message broker.", ex);
        }
    }
    
    public static Map<String, String> getConfigurationParameters(String mBroker) throws GenericException
    {
        try
        {
            Class<? extends IModuleConfiguration> clazz = MessageBroker.getConfigClass(mBroker);
            Constructor<? extends IModuleConfiguration> ctor = clazz.getConstructor();
            moduleConfiguration = ctor.newInstance();
            List<String> keys = moduleConfiguration.listAllParameters();
            Map<String, String> mParameters = new LinkedHashMap<>();
            
            for(String key : keys)
            {
                mParameters.put(key, CONFIGURATIONPARAMETERS.get(key)+"");
            }
            
            return mParameters;
        }
        catch (Exception ex)
        {
            throw new GenericException("An error occurred while getting parameters for the message broker.", ex);
        }
    }
    
    public static IMessageBroker getMessageBroker()
    {
        if(messageBroker == null)
        {
            messageBroker = moduleConfiguration.getMessageBroker();
        }
        
        return messageBroker;
    }
    
    public static ObjectMapper getObjectMapper()
    {
        if(objectMapper == null)
        {
            objectMapper = new ObjectMapper();
        }
        
        return objectMapper;
    }
    
    public static void inputSystemParameter(String param, String spec, String defValue) throws IOException
    {
        System.out.print(param + (spec.isEmpty() ? ": " : " ("+spec+"): "));
        
        String value = br.readLine();
        CONFIGURATIONPARAMETERS.put(param, (value == null || value.isEmpty() ? defValue : value));
    }
    
    public static void setSystemParameter(String param, String value, String defValue)
    {                   
        CONFIGURATIONPARAMETERS.put(param, (value == null || value.isEmpty() ? defValue : value));
    }
    
    public static void setSystemParameter(String param, Object value)
    {
        CONFIGURATIONPARAMETERS.put(param, value);
    }
    
    protected void printSystemParameters()
    {
        Logger.getLogger(Configuration.class).debug("The following system configuration parameters were set:");
        for(Map.Entry<String, Object> entry : CONFIGURATIONPARAMETERS.entrySet())
        {
            Logger.getLogger(Configuration.class).debug(entry.getKey()+"="+entry.getValue());
        }
    }
}

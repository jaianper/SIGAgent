package com.jaianper.common.conf;

import com.jaianper.common.error.GenericException;
import com.jaianper.common.util.Tools;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jaianper
 */
public class Registry
{
    private static final Map resources = new HashMap();
    
    public static <S> S  loadResource(Class<S> c) throws GenericException
    {
        if(resources.containsKey(c.getCanonicalName()))
        {
            S result = (S)resources.get (c.getCanonicalName());
            return result;
        }
        
        try
        {
            ClassLoader ldr = Tools.getClassLoader();
            InputStream is = ldr.getResourceAsStream("META-INF/services/services.ini");
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    int comment = line.indexOf('#');
                    if (comment >= 0) {
                        line = line.substring(0, comment);
                    }
                    String name = line.trim();
                    if (name.length() == 0)
                    {
                        continue;
                    }
                    Class<?> clz = Class.forName(name, true, ldr);

                    if(c.isAssignableFrom(clz))
                    {
                        Class<? extends S> impl = clz.asSubclass(c);
                        Constructor<? extends S> ctor = impl.getConstructor();
                        S svc = ctor.newInstance();
                        resources.put(c.getCanonicalName(), svc);
                        return svc;
                        //services.add(svc);
                    }
                }
            }
            finally
            {
                is.close();
            }
        }
        catch(Exception ex)
        {
            throw new GenericException("Error getting service: "+c.getCanonicalName(), ex);
        }
        
        throw new GenericException("Service "+c.getCanonicalName()+" not found.");
    }
    
    public static <S extends IModuleConfiguration> Iterable<S> loadResources(Class<S> c) throws GenericException
    {
        Collection<S> resources = new ArrayList<S>();
        try
        {
            ClassLoader ldr = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> e = ldr.getResources("META-INF/services/services.ini");
            
            while (e.hasMoreElements()) {
                URL url = e.nextElement();
                InputStream is = url.openStream();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    while (true) {
                        String line = r.readLine();
                        if (line == null) {
                            break;
                        }
                        int comment = line.indexOf('#');
                        if (comment >= 0) {
                            line = line.substring(0, comment);
                        }
                        String name = line.trim();
                        if (name.length() == 0)
                        {
                            continue;
                        }
                        Class<?> clz = Class.forName(name, true, ldr);

                        if(c.isAssignableFrom(clz))
                        {
                            Class<? extends S> impl = clz.asSubclass(c);
                            Constructor<? extends S> ctor = impl.getConstructor();
                            S svc = ctor.newInstance();
                            resources.add(svc);
                        }
                    }
                }
                finally
                {
                    is.close();
                }
            }
        }
        catch(Exception ex)
        {
            throw new GenericException("Error getting service: "+c.getCanonicalName(), ex);
        }
        
        return resources;
    }
}

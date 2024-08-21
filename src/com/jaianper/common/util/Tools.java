package com.jaianper.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.hyperic.jni.ArchNotSupportedException;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarLoader;

/**
 *
 * @author jaianper
 */
public class Tools
{
    private static final ClassLoader ldr;
    static
    {
        ldr = Thread.currentThread().getContextClassLoader();
    }
    
    public static ClassLoader getClassLoader()
    {
        return ldr;
    }
    
    public static boolean isUnix()
    {
        String sPropiedades = System.getProperty("os.name");
        boolean flag = true;

        if (sPropiedades.toUpperCase().contains("WINDOWS"))
        {
            flag = false;
        }
        return flag;
    }

    /**
     * Method responsible for returning the IPv4 of the machine that is running it.
     *
     * @return
     */
    public static String getInet4Address()
    {
        try
        {
            return Inet4Address.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex)
        {
            Logger.getLogger(Tools.class).error("", ex);
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException ex)
        {
            Logger.getLogger(Tools.class).error("", ex);
        }
        return "127.0.0.1";
    }
    
    public static URL getResource(String resource)
    {
        return ldr.getResource(resource);
    }
    
    public static InputStream getResourceAsStream(String resource) throws IOException
    {
        return ldr.getResourceAsStream(resource);
    }
    
    public static void loadLibraries() throws IOException
    {
        Logger.getLogger(Tools.class).info("java.io.tmpdir: "+System.getProperty("java.io.tmpdir"));
        
        SigarLoader sl = new SigarLoader(Sigar.class);
        try
        {
            loadLib(sl.getLibraryName());
        }
        catch (ArchNotSupportedException ex)
        {
            Logger.getLogger(Tools.class).error("", ex);
        }
    }
    
    private static void loadLib(String fileName)
    {
        try
        {
            URL urlLib = ldr.getResource("META-INF/lib/sigar/"+fileName);
            
            File tmpDir = new File("tmp");
            if(!tmpDir.exists())
            {
                tmpDir.mkdir();
            }
            
            File tmpFile = new File(tmpDir, fileName);
            InputStream in = urlLib.openStream();
            OutputStream out = new FileOutputStream(tmpFile);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            Logger.getLogger(Tools.class).info("Loading " + tmpFile.getAbsolutePath() +" ...");
            System.load(tmpFile.getAbsolutePath());
        }
        catch (Exception e)
        {
            Logger.getLogger(Tools.class).error("Error loading libraries.", e);
        }
    }
}

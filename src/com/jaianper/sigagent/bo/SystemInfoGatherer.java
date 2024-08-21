package com.jaianper.sigagent.bo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jaianper.common.conf.Configuration;
import com.jaianper.common.error.GenericException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.hyperic.sigar.Arp;
import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.DiskUsage;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInfo;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.NetStat;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcDiskIO;
import org.hyperic.sigar.ProcFd;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.ProcStat;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.ResourceLimit;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarPermissionDeniedException;
import org.hyperic.sigar.Swap;
import org.hyperic.sigar.SysInfo;
import org.hyperic.sigar.Tcp;
import org.hyperic.sigar.Uptime;
import org.hyperic.sigar.Who;
import org.hyperic.sigar.win32.Service;
import org.hyperic.sigar.win32.Win32Exception;

/**
 *
 * @author jaianper
 */
public class SystemInfoGatherer
{
    private static SystemInfoGatherer sig;
    private final Sigar sigar;
    private final String javaVersion;
    private String ipAddress;
    private String macAddress;
    private String hostName;
    
    SystemInfoGatherer()
    {
        sigar = new Sigar();
        javaVersion = System.getProperty("java.specification.version");
        logInfo("java.specification.version: "+javaVersion);
    }
    
    public static SystemInfoGatherer getInstance()
    {
        if(sig == null)
        {
            sig = new SystemInfoGatherer();
        }
        return sig;
    }
    
    /**
     * Gets information about the system CPU.
     * 
     * @return 
     */
    private Map<String, Object> getCpuDetail()
    {
        logDebug("Getting system CPU details...");
        Map<String, Object> mCpuDetail = new LinkedHashMap<>();
        
        try
        {
            // Gets information about the system CPU.
            Cpu cpu = sigar.getCpu();
            Map<String, Object> mCpu = new LinkedHashMap<>();
            mCpu.put("User", cpu.getUser()); // Total system cpu user time.
            mCpu.put("System", cpu.getSys()); // Total system cpu kernel time.
            mCpu.put("Nice", cpu.getNice()); // Total system cpu nice time.
            mCpu.put("Wait", cpu.getWait()); // Total system cpu IO wait time.
            mCpu.put("Idle", cpu.getIdle()); // Total system cpu idle time.
            mCpu.put("Total", cpu.getTotal()); // Total system cpu time.
            mCpu.put("Stolen", cpu.getStolen()); // Total system cpu involuntary wait time.
            mCpu.put("Irq", cpu.getIrq()); // Total system cpu time servicing interrupts.
            mCpu.put("SoftIrq", cpu.getSoftIrq()); // Total system cpu time servicing softirqs.
            
            mCpuDetail.put("OverallCpu", mCpu);
        }
        catch(SigarException se)
        {
            logWarn("Error getting system CPU information. " + se.getMessage(), null);
        }
        
        try
        {
            // Gets system CPU information in percentage format. (i.e. fraction of 1)
            CpuPerc cpuPerc = sigar.getCpuPerc();
            Map<String, Object> mCpuPerc = new LinkedHashMap<>();
            mCpuPerc.put("User", CpuPerc.format(cpuPerc.getUser()));
            mCpuPerc.put("System", CpuPerc.format(cpuPerc.getSys()));
            mCpuPerc.put("Nice", CpuPerc.format(cpuPerc.getNice()));
            mCpuPerc.put("Wait", CpuPerc.format(cpuPerc.getWait()));
            mCpuPerc.put("Idle", CpuPerc.format(cpuPerc.getIdle()));
            mCpuPerc.put("Combined", CpuPerc.format(cpuPerc.getCombined()));
            mCpuPerc.put("Stolen", cpuPerc.getStolen());
            mCpuPerc.put("Irq", cpuPerc.getIrq());
            mCpuPerc.put("SoftIrq", cpuPerc.getSoftIrq());
            
            mCpuDetail.put("OverallCpuPerc", mCpuPerc);
        }
        catch(SigarException se)
        {
            logError("Error getting system CPU percentage information. " + se.getMessage(), null);
        }
        
        try
        {
            // Gets a list of per-CPU metrics.
            Cpu[] arrCpu = sigar.getCpuList();
            // Gets a list with information about the CPUs
            CpuInfo[] arrCpuInfo = sigar.getCpuInfoList();
            // Gets system information per CPU in percentage format. (i.e. fraction of 1)
            CpuPerc[] arrCpuPerc = sigar.getCpuPercList();
            
            List<Map> lstCpu = new ArrayList<>();
            
            if(arrCpu.length == arrCpuInfo.length && arrCpu.length == arrCpuPerc.length)
            {
                for(int i=0; i<arrCpu.length; ++i)
                {
                    Map<String, Object> mCpuData = new LinkedHashMap<>();
                    Map<String, Object> mCpuInfo = new LinkedHashMap<>();
                    Map<String, Object> mCpu = new LinkedHashMap<>();
                    Map<String, Object> mCpuPerc = new LinkedHashMap<>();
                    
                    CpuInfo cpuInfo = arrCpuInfo[i];
                    mCpuInfo.put("Model", cpuInfo.getModel()); // CPU model.
                    mCpuInfo.put("Vendor", cpuInfo.getVendor()); // CPU vendor id.
                    mCpuInfo.put("Mhz", cpuInfo.getMhz()); // Current CPU speed.
                    mCpuInfo.put("MhzMax", cpuInfo.getMhzMax()); // 
                    mCpuInfo.put("MhzMin", cpuInfo.getMhzMin()); // 
                    mCpuInfo.put("TotalCores", cpuInfo.getTotalCores()); // Total CPU cores (logical).
                    mCpuInfo.put("TotalSockets", cpuInfo.getTotalSockets()); // Total CPU sockets (physical).
                    mCpuInfo.put("CoresPerSocket", cpuInfo.getCoresPerSocket()); // Number of CPU cores per CPU socket.
                    mCpuInfo.put("CacheSize", cpuInfo.getCacheSize()); // CPU cache size.
                    
                    Cpu cpu = arrCpu[i];
                    mCpu.put("User", cpu.getUser()); // Total system cpu user time.
                    mCpu.put("System", cpu.getSys()); // Total system cpu kernel time.
                    mCpu.put("Nice", cpu.getNice()); // Total system cpu nice time.
                    mCpu.put("Wait", cpu.getWait()); // Total system cpu IO wait time.
                    mCpu.put("Idle", cpu.getIdle()); // Total system cpu idle time.
                    mCpu.put("Total", cpu.getTotal()); // Total system cpu time.
                    mCpu.put("Stolen", cpu.getStolen()); // Total system cpu involuntary wait time.
                    mCpu.put("Irq", cpu.getIrq()); // Total system cpu time servicing interrupts.
                    mCpu.put("SoftIrq", cpu.getSoftIrq()); // Total system cpu time servicing softirqs.
                    
                    CpuPerc cpuPerc = arrCpuPerc[i];
                    mCpuPerc.put("User", CpuPerc.format(cpuPerc.getUser()));
                    mCpuPerc.put("System", CpuPerc.format(cpuPerc.getSys()));
                    mCpuPerc.put("Nice", CpuPerc.format(cpuPerc.getNice()));
                    mCpuPerc.put("Wait", CpuPerc.format(cpuPerc.getWait()));
                    mCpuPerc.put("Idle", CpuPerc.format(cpuPerc.getIdle()));
                    mCpuPerc.put("Combined", CpuPerc.format(cpuPerc.getCombined()));
                    mCpuPerc.put("Stolen", cpuPerc.getStolen());
                    mCpuPerc.put("Irq", cpuPerc.getIrq());
                    mCpuPerc.put("SoftIrq", cpuPerc.getSoftIrq());
                    
                    mCpuData.put("CpuInfo", mCpuInfo);
                    mCpuData.put("Cpu", mCpu);
                    mCpuData.put("CpuPerc", mCpuPerc);
                    
                    lstCpu.add(mCpuData);
                }
            }
            else
            {
                throw new GenericException("CPU information lists do not match in size.");
            }
            
            mCpuDetail.put("CpuDataList", lstCpu);
        }
        catch(SigarException se)
        {
            logWarn(" " + se.getMessage(), null);
        }
        catch (GenericException ex)
        {
            logError("Error getting system CPU information.", ex);
        }
        
        return mCpuDetail;
    }
    
    /**
     * Gets information about system memory.
     * @return 
     */
    private Map<String, Object> getMemDetail()
    {
        logDebug("Getting details of system memory...");
        
        Map<String, Object> mMemDetail = new LinkedHashMap<>();
        
        try
        {
            Mem mem = sigar.getMem();

            mMemDetail.put("Ram", mem.getRam()); // System Random Access Memory (in MB).
            mMemDetail.put("Total", mem.getTotal()); // Total system memory. Bytes
            mMemDetail.put("Used", mem.getUsed()); // Total used system memory. Bytes
            mMemDetail.put("Free", mem.getFree()); // Total free system memory (e.g. Linux plus cached). Bytes
            mMemDetail.put("ActualUsed", mem.getActualUsed()); // Actual total used system memory (e.g. Linux minus buffers). Bytes
            mMemDetail.put("ActualFree", mem.getActualFree()); // Actual total free system memory. Bytes
            mMemDetail.put("UsedPercent", mem.getUsedPercent()); // Percent total used system memory.
            mMemDetail.put("FreePercent", mem.getFreePercent()); // Percent total free system memory.
        }
        catch(SigarException se)
        {
            logWarn("Error getting information from system memory. " + se.getMessage(), null);
        }
        
        return mMemDetail;
    }
    
    /**
     * Gets information about system uptime.
     * 
     */
    private Map<String, Object> getUptimeDetail()
    {
        logDebug("Getting details about system uptime...");

        Map<String, Object> mUptimeDetail = new LinkedHashMap<>();
        try
        {
            Uptime uptime = sigar.getUptime();
            long now = System.currentTimeMillis();
            
            mUptimeDetail.put("Uptime", uptime.getUptime()); // milliseconds
            mUptimeDetail.put("Boottime", (now - (long)uptime.getUptime()*1000)); // Date in millis
        }
        catch(SigarException se)
        {
            logWarn("Error getting system uptime information. " + se.getMessage(), null);
        }
        
        return mUptimeDetail;
    }
    
    /**
     * Gets information about the system Swap.
     * 
     */
    private Map<String, Object> getSwapDetail()
    {
        logDebug("Getting Swap information from the system...");

        Map<String, Object> mSwapDetail = new LinkedHashMap<>();
        try
        {
            Swap swap = sigar.getSwap();
            mSwapDetail.put("Total", swap.getTotal()); // Total system swap. Bytes
            mSwapDetail.put("Used", swap.getUsed()); // Total used system swap. Bytes
            mSwapDetail.put("Free", swap.getFree()); // Total free system swap. Bytes
            mSwapDetail.put("PageIn", swap.getPageIn()); // The Pages in.
            mSwapDetail.put("PageOut", swap.getPageOut()); // The Pages out.
        }
        catch(SigarException se)
        {
            logWarn("Error getting Swap information from system. " + se.getMessage(), null);
        }
        return mSwapDetail;
    }
    
    /**
     * Gets the system load average.
     * 
     */
    private Map<String, Object> getLoadAverageDetail()
    {
        logDebug("Getting the system load average...");
        
        Map<String, Object> mLoadAverageDetail = new LinkedHashMap<>();
        
        try
        {
            // Average system load for the last 1, 5 and 15 minutes
            double[] arrLoadAverage = sigar.getLoadAverage();

            mLoadAverageDetail.put("1min", arrLoadAverage[0]);
            mLoadAverageDetail.put("5min", arrLoadAverage[1]);
            mLoadAverageDetail.put("15min", arrLoadAverage[2]);
        }
        catch(SigarException se)
        {
            mLoadAverageDetail.put("errorCode", 1);
            mLoadAverageDetail.put("errorMessage", se.getMessage());

            logDebug("System load average was not obtained. -> " + se.getMessage());//, null);
        }
        
        return mLoadAverageDetail;
    }
    
    /**
     * Get system resource limits.
     * @return 
     */
    private Map<String, Object> getResourceLimitDetail()
    {
        logDebug("Getting system resource limits...");
        
        Map<String, Object> mResourceLimitDetail = new LinkedHashMap<>();
        
        try
        {
            ResourceLimit resourceLimit = sigar.getResourceLimit();
            mResourceLimitDetail.put("CoreCur", resourceLimit.getCoreCur());
            mResourceLimitDetail.put("CoreMax", resourceLimit.getCoreMax());
            mResourceLimitDetail.put("CpuCur", resourceLimit.getCpuCur());
            mResourceLimitDetail.put("CpuMax", resourceLimit.getCpuMax());
            mResourceLimitDetail.put("DataCur", resourceLimit.getDataCur());
            mResourceLimitDetail.put("DataMax", resourceLimit.getDataMax());
            mResourceLimitDetail.put("FileSizeCur", resourceLimit.getFileSizeCur());
            mResourceLimitDetail.put("FileSizeMax", resourceLimit.getFileSizeMax());
            mResourceLimitDetail.put("MemoryCur", resourceLimit.getMemoryCur());
            mResourceLimitDetail.put("MemoryMax", resourceLimit.getMemoryMax());
            mResourceLimitDetail.put("OpenFilesCur", resourceLimit.getOpenFilesCur());
            mResourceLimitDetail.put("OpenFilesMax", resourceLimit.getOpenFilesMax());
            mResourceLimitDetail.put("PipeSizeCur", resourceLimit.getPipeSizeCur());
            mResourceLimitDetail.put("PipeSizeMax", resourceLimit.getPipeSizeMax());
            mResourceLimitDetail.put("ProcessesCur", resourceLimit.getProcessesCur());
            mResourceLimitDetail.put("ProcessesMax", resourceLimit.getProcessesMax());
            mResourceLimitDetail.put("StackCur", resourceLimit.getStackCur());
            mResourceLimitDetail.put("StackMax", resourceLimit.getStackMax());
            mResourceLimitDetail.put("VirtualMemoryCur", resourceLimit.getVirtualMemoryCur());
            mResourceLimitDetail.put("VirtualMemoryMax", resourceLimit.getVirtualMemoryMax());
        }
        catch(SigarException se)
        {
            logWarn("Error getting system resource limits. " + se.getMessage(), null);
        }
        return mResourceLimitDetail;
    }
    
    /**
     * 
     */
    private Map<String, Object> getSysInfoDetail()
    {
        logDebug("Getting system information...");
        
        Map<String, Object> mSysInfoDetail = new LinkedHashMap<>();
        try
        {
            if("1.6".equals(javaVersion) || "1.7".equals(javaVersion) || "1.8".equals(javaVersion))
            {
                SysInfo sysInfo = new SysInfo();
                sysInfo.gather(sigar);
                mSysInfoDetail.put("Name",sysInfo.getName());
                mSysInfoDetail.put("Description",sysInfo.getDescription());
                mSysInfoDetail.put("Version",sysInfo.getVersion());
                mSysInfoDetail.put("Arch",sysInfo.getArch());
                mSysInfoDetail.put("Machine",sysInfo.getMachine());
                mSysInfoDetail.put("Vendor",sysInfo.getVendor());
                mSysInfoDetail.put("VendorName",sysInfo.getVendorName());
                mSysInfoDetail.put("VendorCodeName",sysInfo.getVendorCodeName());
                mSysInfoDetail.put("VendorVersion",sysInfo.getVendorVersion());
                mSysInfoDetail.put("PatchLevel",sysInfo.getPatchLevel());
            }
            else
            {
                mSysInfoDetail.put("Name", System.getProperty("os.name"));
                mSysInfoDetail.put("Version", System.getProperty("os.version"));
                mSysInfoDetail.put("Arch", System.getProperty("os.arch"));
            }
        }
        catch(SigarException se)
        {
            logWarn("Error getting system information. " + se.getMessage(), null);
        }
        return mSysInfoDetail;
    }
    
    /**
     * 
     * @return 
     */
    private List<Map> getWhoDetail()
    {
        logDebug("Getting information from users...");
        
        List<Map> lstWho = new ArrayList<>();
        
        try
        {
            Who[] arrWho = sigar.getWhoList();
            
            for(Who who : arrWho)
            {
                Map<String, Object> mWhoDetail = new LinkedHashMap<>();
                mWhoDetail.put("User", who.getUser());
                mWhoDetail.put("Device", who.getDevice());
                mWhoDetail.put("Host", who.getHost());
                mWhoDetail.put("Time", who.getTime());
                lstWho.add(mWhoDetail);
            }
        }
        catch(SigarException se)
        {
            logWarn("Error getting user information. " + se.getMessage(), null);
        }
        return lstWho;
    }
    
    /**
     * Gets the list of services.
     * It is only supported on Windows platforms.
     * 
     * @return 
     */
    private Map<String, Object> getServicesDetail()
    {
        logDebug("Getting service information...");
        Map<String, Object> mServicesDetail = new LinkedHashMap<>();
        try
        {
            if(System.getProperty("os.name").toUpperCase().contains("WIN"))
            {
                List<String> serviceNames = Service.getServiceNames();

                mServicesDetail.put("ServiceNames", serviceNames);
                mServicesDetail.put("Count", serviceNames.size());
            }
            else
            {
                mServicesDetail.put("errorCode", 1);
                mServicesDetail.put("errorMessage", "Not supported.");
                
//                logWarn("Not supported.", null);
                logDebug("Getting service detail not supported on Linux.");
            }
        }
        catch(Win32Exception we)
        {
            mServicesDetail.put("errorCode", 1);
            mServicesDetail.put("errorMessage", we.getMessage());
            
            logWarn("Not supported.", we);
        }
        return mServicesDetail;
    }
    
    /**
     * Gets information about the file system.
     */
    private List<Map> getFileSystemDetail()
    {
        logDebug("Getting file system information...");
        
        List<Map> lstFileSystemDetail = new ArrayList<>();
        
        try
        {
            // Gets a listing of the file system
            FileSystem[] arrFileSystem = sigar.getFileSystemList();

            for(FileSystem fileSystem : arrFileSystem)
            {
                Map<String, Object> mFileSystemData = new LinkedHashMap<>();
                Map<String, Object> mFileSystem = new LinkedHashMap<>();
                
                mFileSystem.put("DevName", fileSystem.getDevName()); // Device name.
                mFileSystem.put("DirName", fileSystem.getDirName()); // Directory name.
                mFileSystem.put("Type", fileSystem.getType()); // File system type.
                mFileSystem.put("TypeName", fileSystem.getTypeName()); // File system generic type name.
                mFileSystem.put("SysTypeName", fileSystem.getSysTypeName()); // File system os specific type name.
                mFileSystem.put("Options", fileSystem.getOptions()); // File system mount options.
                mFileSystem.put("Flags", fileSystem.getFlags()); // File system flags.
                
                mFileSystemData.put("FileSystem", mFileSystem);
                
                if(fileSystem.getType() == FileSystem.TYPE_LOCAL_DISK)
                {
                    Map<String, Object> mFileSystemUsage = new LinkedHashMap<>();
                    Map<String, Object> mDiskUsage = new LinkedHashMap<>();
                    
                    // Gets the file system usage
                    FileSystemUsage fileSystemUsage = sigar.getFileSystemUsage(fileSystem.getDirName());
                    mFileSystemUsage.put("DiskServiceTime", fileSystemUsage.getDiskServiceTime());
                    mFileSystemUsage.put("DiskQueue", fileSystemUsage.getDiskQueue());
                    mFileSystemUsage.put("Total", fileSystemUsage.getTotal()); // Total Kbytes of filesystem.
                    mFileSystemUsage.put("Free", fileSystemUsage.getFree()); // Total free Kbytes on filesystem.
                    mFileSystemUsage.put("Avail", fileSystemUsage.getAvail()); // Total free Kbytes on filesystem available to caller.
                    mFileSystemUsage.put("Used", fileSystemUsage.getUsed()); // Total used Kbytes on filesystem.
                    mFileSystemUsage.put("UsePercent", (fileSystemUsage.getUsePercent()*100)); // Percent of disk used.
                    mFileSystemUsage.put("DiskReads", fileSystemUsage.getDiskReads()); // Number of physical disk reads.
                    mFileSystemUsage.put("DiskWrites", fileSystemUsage.getDiskWrites()); // Number of physical disk writes.
                    mFileSystemUsage.put("DiskReadBytes", fileSystemUsage.getDiskReadBytes()); // Number of physical disk bytes read.
                    mFileSystemUsage.put("DiskWriteBytes", fileSystemUsage.getDiskWriteBytes()); // Number of physical disk bytes written.
                    mFileSystemUsage.put("Files", fileSystemUsage.getFiles()); // Total number of file nodes on the filesystem.
                    mFileSystemUsage.put("FreeFiles", fileSystemUsage.getFreeFiles()); // Number of free file nodes on the filesystem.
                    
                    // Gets the disk usage.
                    DiskUsage diskUsage = sigar.getDiskUsage(fileSystem.getDirName());
                    mDiskUsage.put("ServiceTime", diskUsage.getServiceTime());
                    mDiskUsage.put("Queue", diskUsage.getQueue());
                    mDiskUsage.put("Reads", diskUsage.getReads()); // Number of physical disk reads
                    mDiskUsage.put("Writes", diskUsage.getWrites()); // Number of physical disk writes.
                    mDiskUsage.put("ReadBytes", diskUsage.getReadBytes()); // Number of physical disk bytes read.
                    mDiskUsage.put("WriteBytes", diskUsage.getWriteBytes()); // Number of physical disk bytes written.
                    
                    mFileSystemData.put("FileSystemUsage", mFileSystemUsage);
                    mFileSystemData.put("DiskUsage", mDiskUsage);
                }
                
                lstFileSystemDetail.add(mFileSystemData);
            }
        }
        catch(SigarException se)
        {
            logWarn("Error getting file system information. " + se.getMessage(), null);
        }
        
        return lstFileSystemDetail;
    }
    
    /**
     * Method for obtaining information about system processes.
     */
    private Map<String, Object> getProcessDetail()
    {
        logDebug("Getting information about system processes...");
        
        Map<String, Object> mServicesDetail = new LinkedHashMap<>();
        
        try
        {
            Map<String, Object> mProcStatDetail = new LinkedHashMap<>();
            
            // Gets statistics on system processes.
            ProcStat procStat = sigar.getProcStat();
            mProcStatDetail.put("Threads", procStat.getThreads()); // Total number of threads.
            mProcStatDetail.put("Sleeping", procStat.getSleeping()); // Total number of processes in sleep state.
            mProcStatDetail.put("Stopped", procStat.getStopped()); // Total number of processes in stop state.
            mProcStatDetail.put("Zombie", procStat.getZombie()); // Total number of processes in zombie state.
            mProcStatDetail.put("Idle", procStat.getIdle()); // Total number of processes in idle state.
            mProcStatDetail.put("Total", procStat.getTotal()); // Total number of processes.
            mProcStatDetail.put("Running", procStat.getRunning()); // Total number of processes in run state.
            
            mServicesDetail.put("ProcStatDetail", mProcStatDetail); // System process statistics.
        }
        catch(SigarException se)
        {
            logWarn("Error getting system process statistics. " + se.getMessage(), null);
        }
        
        try
        {
            // Gets the list of system processes.
            long[] procList = sigar.getProcList();
            int wp = 0;
            int e = 0;
            
            Map<Character, String> mState = new HashMap<Character, String>(){{
                put('S',"SLEEP");
                put('R',"RUN");
                put('T',"STOP");
                put('Z',"ZOMBIE");
                put('D',"IDLE");
            }};
            
            List<Map> mProcList = new ArrayList<>();
            for(long pId : procList)
            {
                Map<String, Object> mProcStateDetail = new LinkedHashMap<>();
                
                try
                {
                    /*
                    // Gets process credentials information.
                    ProcCred procCred = sigar.getProcCred(pId);
                    // Gets process loaded modules.
                    List procModules = sigar.getProcModules(pId);
                    // Gets process credential names.
                    ProcCredName procCredName = sigar.getProcCredName(pId);
                    // Gets the current working directory of the process.
                    ProcExe procExe = sigar.getProcExe(pId);
                    */
                    
                    // Gets information about the status of the process.
                    ProcState procState = sigar.getProcState(pId);
                    // Gets information about the process file descriptor.
                    ProcFd procFd = sigar.getProcFd(pId);
                    
                    mProcStateDetail.put("PID", pId); // Process id
                    mProcStateDetail.put("Name", procState.getName()); // Name of the process program.
                    mProcStateDetail.put("OpenFd", procFd.getTotal()); // Total number of open file descriptors.
                    mProcStateDetail.put("State", mState.get(procState.getState())); // Process state (Running, Zombie, etc.).
                    mProcStateDetail.put("Nice", procState.getNice()); // Nice value of process.
                    mProcStateDetail.put("Priority", procState.getPriority()); // Kernel scheduling priority of process.
                    mProcStateDetail.put("Threads", procState.getPriority()); // Number of active threads.
                    mProcStateDetail.put("Processor", procState.getProcessor()); // Processor number last run on.
                }
                catch(SigarPermissionDeniedException spde)
                {
                    wp++;
                    logDebug("PID: "+pId+" - [State] "+spde.getMessage());
                }
                catch(SigarException se)
                {
                    e++;
                    logDebug("PID: "+pId+" - [State] "+se.getMessage());
                }
                
                try
                {
                    Map<String, Object> mProcCpuDetail = new LinkedHashMap<>();
                    
                    // Gets information about the process's CPU.
                    ProcCpu procCpu = sigar.getProcCpu(pId);
                    mProcCpuDetail.put("StartTime", procCpu.getStartTime());
                    mProcCpuDetail.put("LastTime", procCpu.getLastTime());
                    mProcCpuDetail.put("Percent", procCpu.getPercent());
                    mProcCpuDetail.put("User", procCpu.getUser());
                    mProcCpuDetail.put("Sys", procCpu.getSys());
                    mProcCpuDetail.put("Total", procCpu.getTotal());
                    
                    mProcStateDetail.put("ProcCpu", mProcCpuDetail);
                }
                catch(SigarException se)
                {
                    logDebug("PID: "+pId+" - [Cpu] "+se.getMessage());
                }
                
                try
                {
                    Map<String, Object> mProcMemDetail = new LinkedHashMap<>();
                    
                    // Obtains information from the process memory.
                    ProcMem procMem = sigar.getProcMem(pId);
                    
                    mProcMemDetail.put("MajorFaults", procMem.getMajorFaults());
                    mProcMemDetail.put("MinorFaults", procMem.getMinorFaults());
                    mProcMemDetail.put("PageFaults", procMem.getPageFaults());
                    mProcMemDetail.put("Resident", procMem.getResident());
                    mProcMemDetail.put("Share", procMem.getShare());
                    mProcMemDetail.put("Size", procMem.getSize());
                    
                    mProcStateDetail.put("ProcMem", mProcMemDetail);
                }
                catch(SigarException se)
                {
                    logDebug("PID: "+pId+" - [Mem] "+se.getMessage());
                }
                
                try
                {
                    Map<String, Object> mProcDiskIODetail = new LinkedHashMap<>();
                    
                    // Gets disk I/O information about the process.
                    ProcDiskIO procDiskIO = sigar.getProcDiskIO(pId);
                    mProcDiskIODetail.put("BytesRead", procDiskIO.getBytesRead());
                    mProcDiskIODetail.put("BytesWritten", procDiskIO.getBytesWritten());
                    mProcDiskIODetail.put("BytesTotal", procDiskIO.getBytesTotal());
                    
                    mProcStateDetail.put("ProcDiskIO", mProcDiskIODetail);
                }
                catch(SigarException se)
                {
                    logDebug("PID: "+pId+" - [Disk] "+se.getMessage());
                }
                
                mProcList.add(mProcStateDetail);
            }
            
            mServicesDetail.put("ProcList", mProcList); // List of system processes.
            mServicesDetail.put("ProcPermissionDenied", wp); // Without permission to obtain information
            mServicesDetail.put("ProcError", e); // Error when obtaining information
        }
        catch(SigarException se)
        {
            logWarn("Error getting list of system processes. " + se.getMessage(), null);
        }
        
        return mServicesDetail;
    }
    
    /**
     * Method for obtaining network information.
     */
    private Map<String, Object> getNetDetail()
    {
        logDebug("Getting information from the network...");
        
        Map<String, Object> mNetDetail = new LinkedHashMap<>();
        
        try
        {
            Map<String, Object> mNetInfoDetail = new LinkedHashMap<>();
            
            // Get general information about the network
            NetInfo netInfo = sigar.getNetInfo();
            
            hostName = netInfo.getHostName();
            
            mNetInfoDetail.put("HostName", hostName);
            mNetInfoDetail.put("DomainName", netInfo.getDomainName());
            mNetInfoDetail.put("DefaultGateway", netInfo.getDefaultGateway());
            mNetInfoDetail.put("DefaultGatewayInterface", netInfo.getDefaultGatewayInterface());
            mNetInfoDetail.put("PrimaryDns", netInfo.getPrimaryDns());
            mNetInfoDetail.put("SecondaryDns", netInfo.getSecondaryDns());
            
            mNetDetail.put("NetInfoDetail", mNetInfoDetail); // General network information.
        }
        catch(SigarException se)
        {
            logWarn("Error getting general network information. " + se.getMessage(), null);
        }
        
        try
        {
            Map<String, Object> mNetStatDetail = new LinkedHashMap<>();
            
            // Obtains general statistical information about the network
            NetStat netStat = sigar.getNetStat();
            mNetStatDetail.put("AllInboundTotal", netStat.getAllInboundTotal());
            mNetStatDetail.put("AllOutboundTotal", netStat.getAllOutboundTotal());
            mNetStatDetail.put("TcpInboundTotal", netStat.getTcpInboundTotal());
            mNetStatDetail.put("TcpOutboundTotal", netStat.getTcpOutboundTotal());
            mNetStatDetail.put("TcpBound", netStat.getTcpBound());
            mNetStatDetail.put("TcpClose", netStat.getTcpClose());
            mNetStatDetail.put("TcpCloseWait", netStat.getTcpCloseWait());
            mNetStatDetail.put("TcpClosing", netStat.getTcpClosing());
            mNetStatDetail.put("TcpEstablished", netStat.getTcpEstablished());
            mNetStatDetail.put("TcpFinWait1", netStat.getTcpFinWait1());
            mNetStatDetail.put("TcpFinWait2", netStat.getTcpFinWait2());
            mNetStatDetail.put("TcpIdle", netStat.getTcpIdle());
            mNetStatDetail.put("TcpLastAck", netStat.getTcpLastAck());
            mNetStatDetail.put("TcpListen", netStat.getTcpListen());
            mNetStatDetail.put("TcpStates", netStat.getTcpStates());
            mNetStatDetail.put("TcpSynRecv", netStat.getTcpSynRecv());
            mNetStatDetail.put("TcpSynSent", netStat.getTcpSynSent());
            mNetStatDetail.put("TcpTimeWait", netStat.getTcpTimeWait());
            
            mNetDetail.put("NetStatDetail", mNetStatDetail);
        }
        catch(SigarException se)
        {
            logWarn("Error getting general network statistics information. " + se.getMessage(), null);
        }
        
        try
        {
            Map<String, Object> mDefaultNetInterfaceConfigDetail = new LinkedHashMap<>();
            
            // Get configuration information for the default network interface.
            // Iterates over getNetInterfaceList() and returns the first available Ethernet interface.
            NetInterfaceConfig defaultNetInterfaceConfig = sigar.getNetInterfaceConfig();
            
            String ifName = defaultNetInterfaceConfig.getName();
            ipAddress = defaultNetInterfaceConfig.getAddress();
            macAddress = defaultNetInterfaceConfig.getHwaddr();
            
            mDefaultNetInterfaceConfigDetail.put("Name", ifName);
            mDefaultNetInterfaceConfigDetail.put("Type", defaultNetInterfaceConfig.getType());
            mDefaultNetInterfaceConfigDetail.put("Address", ipAddress);
            mDefaultNetInterfaceConfigDetail.put("Netmask", defaultNetInterfaceConfig.getNetmask());
            mDefaultNetInterfaceConfigDetail.put("Broadcast", defaultNetInterfaceConfig.getBroadcast());
            mDefaultNetInterfaceConfigDetail.put("Description", defaultNetInterfaceConfig.getDescription());
            mDefaultNetInterfaceConfigDetail.put("Destination", defaultNetInterfaceConfig.getDestination());
            mDefaultNetInterfaceConfigDetail.put("Flags", defaultNetInterfaceConfig.getFlags());
            mDefaultNetInterfaceConfigDetail.put("Hwaddr", macAddress);
            mDefaultNetInterfaceConfigDetail.put("Metric", defaultNetInterfaceConfig.getMetric());
            mDefaultNetInterfaceConfigDetail.put("Mtu", defaultNetInterfaceConfig.getMtu());
            mDefaultNetInterfaceConfigDetail.put("Address6", defaultNetInterfaceConfig.getAddress6());
            mDefaultNetInterfaceConfigDetail.put("Prefix6Length", defaultNetInterfaceConfig.getPrefix6Length());
            mDefaultNetInterfaceConfigDetail.put("Scope6", defaultNetInterfaceConfig.getScope6());
            mDefaultNetInterfaceConfigDetail.put("TxQueueLen", defaultNetInterfaceConfig.getTxQueueLen());
            mDefaultNetInterfaceConfigDetail.put("Type", defaultNetInterfaceConfig.getType());
            
            mNetDetail.put("DefaultNetInterfaceConfigDetail", mDefaultNetInterfaceConfigDetail); // Default network interface configuration information.
            
            //-----------
            
            Map<String, Object> mDefaultNetInterfaceStatDetail = new LinkedHashMap<>();
            
            // Gets statistics for the default network interface.
            NetInterfaceStat defaultNetInterfaceStat = sigar.getNetInterfaceStat(ifName);
            mDefaultNetInterfaceStatDetail.put("RxBytes", defaultNetInterfaceStat.getRxBytes());
            mDefaultNetInterfaceStatDetail.put("TxBytes", defaultNetInterfaceStat.getTxBytes());
            mDefaultNetInterfaceStatDetail.put("RxDropped", defaultNetInterfaceStat.getRxDropped());
            mDefaultNetInterfaceStatDetail.put("TxDropped", defaultNetInterfaceStat.getTxDropped());
            mDefaultNetInterfaceStatDetail.put("RxErrors", defaultNetInterfaceStat.getRxErrors());
            mDefaultNetInterfaceStatDetail.put("TxErrors", defaultNetInterfaceStat.getTxErrors());
            mDefaultNetInterfaceStatDetail.put("RxOverruns", defaultNetInterfaceStat.getRxOverruns());
            mDefaultNetInterfaceStatDetail.put("TxOverruns", defaultNetInterfaceStat.getTxOverruns());
            mDefaultNetInterfaceStatDetail.put("RxPackets", defaultNetInterfaceStat.getRxPackets());
            mDefaultNetInterfaceStatDetail.put("TxPackets", defaultNetInterfaceStat.getTxPackets());
            mDefaultNetInterfaceStatDetail.put("Speed", defaultNetInterfaceStat.getSpeed());
            mDefaultNetInterfaceStatDetail.put("RxFrame", defaultNetInterfaceStat.getRxFrame());
            mDefaultNetInterfaceStatDetail.put("TxCarrier", defaultNetInterfaceStat.getTxCarrier());
            mDefaultNetInterfaceStatDetail.put("TxCollisions", defaultNetInterfaceStat.getTxCollisions());
            
            mNetDetail.put("DefaultNetInterfaceStatDetail", mDefaultNetInterfaceStatDetail);
        }
        catch(SigarException se)
        {
            logWarn("Error getting default network interface information. " + se.getMessage(), null);
        }
        
        try
        {
            List<Map> lstNetDetail = new ArrayList<>();
            
            // Gets the list of configured network interface names.
            String[] netInterfaceList = sigar.getNetInterfaceList();
            
            for(String netInterfaceName : netInterfaceList)
            {
                Map<String, Object> mNetInterfaceDetail = new LinkedHashMap<>();
                
                try
                {
                    Map<String, Object> mNetInterfaceConfigDetail = new LinkedHashMap<>();

                    // Gets network interface configuration information.
                    NetInterfaceConfig netInterfaceConfig = sigar.getNetInterfaceConfig(netInterfaceName);
                    mNetInterfaceConfigDetail.put("Name", netInterfaceConfig.getName());
                    mNetInterfaceConfigDetail.put("Type", netInterfaceConfig.getType());
                    mNetInterfaceConfigDetail.put("Address", netInterfaceConfig.getAddress());
                    mNetInterfaceConfigDetail.put("Netmask", netInterfaceConfig.getNetmask());
                    mNetInterfaceConfigDetail.put("Broadcast", netInterfaceConfig.getBroadcast());
                    mNetInterfaceConfigDetail.put("Description", netInterfaceConfig.getDescription());
                    mNetInterfaceConfigDetail.put("Destination", netInterfaceConfig.getDestination());
                    mNetInterfaceConfigDetail.put("Flags", netInterfaceConfig.getFlags());
                    mNetInterfaceConfigDetail.put("Hwaddr", netInterfaceConfig.getHwaddr());
                    mNetInterfaceConfigDetail.put("Metric", netInterfaceConfig.getMetric());
                    mNetInterfaceConfigDetail.put("Mtu", netInterfaceConfig.getMtu());
                    mNetInterfaceConfigDetail.put("Address6", netInterfaceConfig.getAddress6());
                    mNetInterfaceConfigDetail.put("Prefix6Length", netInterfaceConfig.getPrefix6Length());
                    mNetInterfaceConfigDetail.put("Scope6", netInterfaceConfig.getScope6());
                    mNetInterfaceConfigDetail.put("TxQueueLen", netInterfaceConfig.getTxQueueLen());
                    mNetInterfaceConfigDetail.put("Type", netInterfaceConfig.getType());

                    mNetInterfaceDetail.put("NetInterfaceConfigDetail", mNetInterfaceConfigDetail);
                }
                catch(SigarException exc)
                {
                    logDebug("Error InterfaceConfig -> "+netInterfaceName+" - \t "+exc.getMessage());
                }
                
                try
                {
                    Map<String, Object> mNetInterfaceStatDetail = new LinkedHashMap<>();

                    // Gets network interface statistics.
                    NetInterfaceStat netInterfaceStat = sigar.getNetInterfaceStat(netInterfaceName);
                    mNetInterfaceStatDetail.put("RxBytes", netInterfaceStat.getRxBytes());
                    mNetInterfaceStatDetail.put("TxBytes", netInterfaceStat.getTxBytes());
                    mNetInterfaceStatDetail.put("RxDropped", netInterfaceStat.getRxDropped());
                    mNetInterfaceStatDetail.put("TxDropped", netInterfaceStat.getTxDropped());
                    mNetInterfaceStatDetail.put("RxErrors", netInterfaceStat.getRxErrors());
                    mNetInterfaceStatDetail.put("TxErrors", netInterfaceStat.getTxErrors());
                    mNetInterfaceStatDetail.put("RxOverruns", netInterfaceStat.getRxOverruns());
                    mNetInterfaceStatDetail.put("TxOverruns", netInterfaceStat.getTxOverruns());
                    mNetInterfaceStatDetail.put("RxPackets", netInterfaceStat.getRxPackets());
                    mNetInterfaceStatDetail.put("TxPackets", netInterfaceStat.getTxPackets());
                    mNetInterfaceStatDetail.put("Speed", netInterfaceStat.getSpeed());
                    mNetInterfaceStatDetail.put("RxFrame", netInterfaceStat.getRxFrame());
                    mNetInterfaceStatDetail.put("TxCarrier", netInterfaceStat.getTxCarrier());
                    mNetInterfaceStatDetail.put("TxCollisions", netInterfaceStat.getTxCollisions());

                    mNetInterfaceDetail.put("NetInterfaceStatDetail", mNetInterfaceStatDetail);
                }
                catch(SigarException exc)
                {
                    logDebug("Error InterfaceStat -> "+netInterfaceName+" - \t "+exc.getMessage());
                }
                
                if(!mNetInterfaceDetail.isEmpty()) lstNetDetail.add(mNetInterfaceDetail);
            }
            
            mNetDetail.put("NetInterfaceListDetail", lstNetDetail);
        }
        catch(SigarException se)
        {
            logWarn("Error getting list of network interfaces. " + se.getMessage(), null);
        }
        
        /*try
        {
            List<Map> lstNetConnectionDetail = new ArrayList<>();
            
            int flags = NetFlags.CONN_SERVER | NetFlags.CONN_TCP;
            // Gets the list of network connections.
            NetConnection[] netConnectionList = sigar.getNetConnectionList(flags);
            
            for(NetConnection netConnection : netConnectionList)
            {
                Map<String, Object> mNetConnectionDetail = new LinkedHashMap<>();
                mNetConnectionDetail.put("LocalAddress", netConnection.getLocalAddress());
                mNetConnectionDetail.put("LocalPort", netConnection.getLocalPort());
                mNetConnectionDetail.put("RemoteAddress", netConnection.getRemoteAddress());
                mNetConnectionDetail.put("RemotePort", netConnection.getRemotePort());
                mNetConnectionDetail.put("State", netConnection.getState());
                mNetConnectionDetail.put("StateString", netConnection.getStateString());
                mNetConnectionDetail.put("Type", netConnection.getType());
                mNetConnectionDetail.put("TypeString", netConnection.getTypeString());
                mNetConnectionDetail.put("ReceiveQueue", netConnection.getReceiveQueue());
                mNetConnectionDetail.put("SendQueue", netConnection.getSendQueue());
                
                lstNetConnectionDetail.add(mNetConnectionDetail);
            }
            
            mNetDetail.put("NetConnectionListDetail", lstNetConnectionDetail);
        }
        catch(SigarException se)
        {
            logWarn("Error getting list of network connections. " + se.getMessage(), null);
        }
        
        try
        {
            List<Map> lstNetRouteDetail = new ArrayList<>();
            
            // Gets the list of network routes.
            NetRoute[] netRouteList = sigar.getNetRouteList();
            
            for(NetRoute netRoute : netRouteList)
            {
                Map<String, Object> mNetRouteDetail = new LinkedHashMap<>();
                
                mNetRouteDetail.put("Ifname", netRoute.getIfname());
                mNetRouteDetail.put("Destination", netRoute.getDestination());
                mNetRouteDetail.put("Gateway", netRoute.getGateway());
                mNetRouteDetail.put("Flags", netRoute.getFlags());
                mNetRouteDetail.put("Irtt", netRoute.getIrtt());
                mNetRouteDetail.put("Mask", netRoute.getMask());
                mNetRouteDetail.put("Metric", netRoute.getMetric());
                mNetRouteDetail.put("Mtu", netRoute.getMtu());
                mNetRouteDetail.put("Refcnt", netRoute.getRefcnt());
                mNetRouteDetail.put("Use", netRoute.getUse());
                mNetRouteDetail.put("Window", netRoute.getWindow());
                
                lstNetRouteDetail.add(mNetRouteDetail);
            }
            
            mNetDetail.put("NetRouteListDetail", lstNetRouteDetail); // 
        }
        catch(SigarException se)
        {
            logWarn("Error getting list of network routes. " + se.getMessage(), null);
        }*/
        
        return mNetDetail;
    }
    
    /**
     * Method for obtaining ARP (Address Resolution Protocol) information.
     */
    private List<Map> getArpDetail()
    {
        List<Map> lstArpDetail = new ArrayList<>();
        
        try
        {
            // Gets ARP information from the system.
            Arp[] arpList = sigar.getArpList();
            
            for(Arp arp : arpList)
            {
                Map<String, Object> mArpDetail = new LinkedHashMap<>();
                mArpDetail.put("Ifname", arp.getIfname());
                mArpDetail.put("Address", arp.getAddress());
                mArpDetail.put("Hwaddr", arp.getHwaddr());
                mArpDetail.put("Flags", arp.getFlags());
                mArpDetail.put("Type", arp.getType());
                
                lstArpDetail.add(mArpDetail);
            }
        }
        catch(SigarException se)
        {
            logWarn("Error getting ARP information. " + se.getMessage(), null);
        }
        
        return lstArpDetail;
    }
    
    /**
     * Method to obtain TCP information.
     */
    private Map<String, Object> getTcpDetail()
    {
        Map<String, Object> mTcpDetail = new LinkedHashMap<>();
        
        try
        {
            // TCP-MIB Statistics
            Tcp tcp = sigar.getTcp();
            mTcpDetail.put("ActiveOpens", tcp.getActiveOpens());
            mTcpDetail.put("PassiveOpens", tcp.getPassiveOpens());
            mTcpDetail.put("AttemptFails", tcp.getAttemptFails());
            mTcpDetail.put("CurrEstab", tcp.getCurrEstab());
            mTcpDetail.put("EstabResets", tcp.getEstabResets());
            mTcpDetail.put("InErrs", tcp.getInErrs());
            mTcpDetail.put("InSegs", tcp.getInSegs());
            mTcpDetail.put("OutRsts", tcp.getOutRsts());
            mTcpDetail.put("OutSegs", tcp.getOutSegs());
            mTcpDetail.put("RetransSegs", tcp.getRetransSegs());
        }
        catch(SigarException se)
        {
            logWarn("Error getting TCP information. " + se.getMessage(), null);
        }
        
        return mTcpDetail;
    }
    
    /**
     * Method to obtain information from the JVM.
     */
    private Map<String, Object> getJavaDetail()
    {
        Map<String, Object> mJavaDetail = new LinkedHashMap<>();
        
        String[] arrProperties = {
            "os.name",
            "os.version",
            "os.arch",
            "user.name",
            "user.country",
            "user.language",
            "user.home",
            "user.dir",
            "file.encoding",
            "native.encoding",
            "sun.cpu.endian",
            "sun.arch.data.model",
            "java.vm.name",
            "java.vm.version",
            "java.vm.vendor",
            "java.vm.specification.version",
            "java.vm.specification.vendor",
            "java.vm.compressedOopsMode",
            "java.version",
            "java.version.date",
            "java.vendor",
            "java.specification.name",
            "java.specification.vendor",
            "java.home",
        };
        
        Map<String, Object> mSystemDetail = new LinkedHashMap<>();
        
        for(String property : arrProperties)
        {
            mSystemDetail.put(property, System.getProperty(property));
        }
        mJavaDetail.put("SystemDetail", mSystemDetail);
        
        //--------------
        
        // Available processors (cores)
        mJavaDetail.put("AvailableProcessors", Runtime.getRuntime().availableProcessors());
        
        //--------------
        
        // This will return Long.MAX_VALUE if there is no preset limit
        long maxMemory = Runtime.getRuntime().maxMemory();
        
        Map<String, Object> mJVMDetail = new LinkedHashMap<>();
        mJVMDetail.put("FreeMemory", Runtime.getRuntime().freeMemory()); // Total amount of free memory (bytes) available to the JVM
        mJVMDetail.put("MaxMemory", (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));  // Maximum amount of memory (bytes) the JVM will attempt to use
        mJVMDetail.put("TotalMemory", Runtime.getRuntime().totalMemory());  // Total memory (bytes) currently available to the JVM
        
        mJavaDetail.put("JVMDetail", mJVMDetail);
        
        //--------------
        
        List<Map> lstFileSystem = new ArrayList<>();
        
        // Get a list of all filesystem roots on this system
        File[] roots = File.listRoots();
        
        // For each filesystem root, print some info
        for (File root : roots)
        {
            Map<String, Object> mFileSystemDetail = new LinkedHashMap<>();
            
            mFileSystemDetail.put("AbsolutePath", root.getAbsolutePath()); // File system root
            mFileSystemDetail.put("TotalSpace", root.getTotalSpace()); // Total space (bytes)
            mFileSystemDetail.put("FreeSpace", root.getFreeSpace()); // Free space (bytes)
            mFileSystemDetail.put("UsableSpace", root.getUsableSpace()); // Usable space (bytes)
            
            lstFileSystem.add(mFileSystemDetail);
        }
        
        mJavaDetail.put("FileSystemDetail", lstFileSystem);
        
        return mJavaDetail;
    }
    
    /**
     * Provides general system information.
     * 
     * @return 
     */
    public Map<String, Object> getOverallSystemDetail()
    {
        logDebug("Getting general system details...");
        
        Map<String, Object> mNetDetail = getNetDetail();
        
        Map<String, Object> mServerInfoDetail = new LinkedHashMap<>();
        mServerInfoDetail.put("IPAddress", ipAddress);
        mServerInfoDetail.put("MacAddress", macAddress);
        mServerInfoDetail.put("HostName", hostName);
        
        Map<String, Object> mOverallSystemDetail = new LinkedHashMap<>();
        mOverallSystemDetail.put("ServerInfo", mServerInfoDetail);
        mOverallSystemDetail.put("SysInfoDetail", getSysInfoDetail());
        mOverallSystemDetail.put("UptimeDetail", getUptimeDetail());
        mOverallSystemDetail.put("WhoDetail", getWhoDetail());
        mOverallSystemDetail.put("CpuDetail", getCpuDetail());
        mOverallSystemDetail.put("MemDetail", getMemDetail());
        mOverallSystemDetail.put("SwapDetail", getSwapDetail());
        mOverallSystemDetail.put("LoadAverageDetail", getLoadAverageDetail());
        mOverallSystemDetail.put("ResourceLimitDetail", getResourceLimitDetail());
        mOverallSystemDetail.put("FileSystemDetail", getFileSystemDetail());
        mOverallSystemDetail.put("ProcessDetail", getProcessDetail());
        mOverallSystemDetail.put("ServicesDetail", getServicesDetail());
        mOverallSystemDetail.put("NetDetail", mNetDetail);
        //mOverallSystemDetail.put("ArpDetail", getArpDetail());
        mOverallSystemDetail.put("TcpDetail", getTcpDetail());
        mOverallSystemDetail.put("JavaDetail", getJavaDetail());
        
        return mOverallSystemDetail;
    }
    
    /**
     * Provides system information by sections.
     * 
     * @param keys
     * @return 
     */
    public Map<String, Object> getSystemDetail(String keys)
    {
        logInfo("Getting system details...");
        
        Map<String, Object> mOverallSystemDetail = new LinkedHashMap<>();
        
        String[] arrKeys = keys.split(",");
        
        for(String key : arrKeys)
        {
            String comp = key.trim();
            
            if("SysInfo".equals(comp))
            {
                mOverallSystemDetail.put("SysInfoDetail", getSysInfoDetail());
            }
            else if("Uptime".equals(comp))
            {
                mOverallSystemDetail.put("UptimeDetail", getUptimeDetail());
            }
            else if("Who".equals(comp))
            {
                mOverallSystemDetail.put("WhoDetail", getWhoDetail());
            }
            else if("Cpu".equals(comp))
            {
                mOverallSystemDetail.put("CpuDetail", getCpuDetail());
            }
            else if("Mem".equals(comp))
            {
                mOverallSystemDetail.put("MemDetail", getMemDetail());
            }
            else if("Swap".equals(comp))
            {
                mOverallSystemDetail.put("SwapDetail", getSwapDetail());
            }
            else if("LoadAverage".equals(comp))
            {
                mOverallSystemDetail.put("LoadAverageDetail", getLoadAverageDetail());
            }
            else if("ResourceLimit".equals(comp))
            {
                mOverallSystemDetail.put("ResourceLimitDetail", getResourceLimitDetail());
            }
            else if("FileSystem".equals(comp))
            {
                mOverallSystemDetail.put("FileSystemDetail", getFileSystemDetail());
            }
            else if("Process".equals(comp))
            {
                mOverallSystemDetail.put("ProcessDetail", getProcessDetail());
            }
            else if("Services".equals(comp))
            {
                mOverallSystemDetail.put("ServicesDetail", getServicesDetail());
            }
        }
        
        return mOverallSystemDetail;
    }
    
    public SIGDaemonInterface getSIGDaemonInterface()
    {
        return new SIGDaemonInterface(){
            @Override
            public void publicSystemDetail() throws GenericException
            {
                SystemInfoGatherer.this.publicSystemDetail();
            }
        };
    }
    
    private void publicSystemDetail() throws GenericException
    {
        Logger.getLogger(SystemInfoGatherer.class).debug("Posting system information...");
        
        try
        {
            Map<String, Object> mResponse = getOverallSystemDetail();
            
            String systemDetail = Configuration.getObjectMapper().writeValueAsString(mResponse);
            
            Configuration.getMessageBroker().publish(systemDetail);
            logWarn("Collected info has been published.", null);
        }
        catch (JsonProcessingException ex)
        {
            throw new GenericException("Error converting object to JSON.", ex);
        }
    }
    
    private void logDebug(String message)
    {
        Logger.getLogger(SystemInfoGatherer.class).debug(message);
    }
    
    private void logInfo(String message)
    {
        Logger.getLogger(SystemInfoGatherer.class).info(message);
    }
    
    private void logWarn(String message, Throwable throwable)
    {
        Logger.getLogger(SystemInfoGatherer.class).warn(message, throwable);
    }
    
    private void logError(String message, Throwable throwable)
    {
        Logger.getLogger(SystemInfoGatherer.class).error(message, throwable);
    }
}

# sig-agent

[1. System Info Gatherer Agent](#1-system-info-gatherer-agent)  
[1.1. Tools used](#11-tools-used)  
[1.2. Compilation](#12-compilation)  
[1.3. Execution](#13-execution)  
[2. The Sigar API](#2-the-sigar-api)  
[2.1. Overview](#21-overview)  
[2.2. License](#22-license)  
[2.3. Binaries](#23-binaries)  
[2.4. Source code and documentation](#24-source-code-and-documentation)  

## 1. System Info Gatherer Agent
SIGAgent is an application that uses the SIGAR library for gathering system information and supports multiple platforms. The information collected can be delivered to an external system using a messaging protocol. The collection frequency time and node connection data are configurable.

### 1.1. Tools used:
* NetBeans IDE 15
* JDK 1.8+
* Jackson 2.7.9
* Sigar API

### 1.2. Compilation
The application sources can be loaded into NetBeans IDE and compilation can be performed normally using the `Run -> Build Project` option.

The compilation generates the file `dist/SIGAgent.jar` in which all the resources necessary for the application to run on any platform are packaged.

In order for the `dist/SIGAgent.jar` file to be generated and executed without errors, the libraries must be correctly included in the `manifest.mf` configuration file as seen below:

```
Manifest-Version: 1.0
X-COMMENT: Main-Class will be added automatically by build
Rsrc-Class-Path: ./
                META-INF/lib/sigar/log4j.jar
                META-INF/lib/sigar/sigar.jar
                META-INF/lib/jackson-annotations-2.7.9.jar
                META-INF/lib/jackson-core-2.7.9.jar
                META-INF/lib/jackson-databind-2.7.9.jar
                META-INF/lib/org.eclipse.paho.client.mqttv3-1.1.1.jar 
                META-INF/lib/slf4j-api-1.7.6.jar
                META-INF/lib/utilities.jar
Class-Path: .
Rsrc-Main-Class: com.jaianper.sigagent.SIGAgent
Main-Class: org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader

```

### 1.3. Execution
The `dist/SIGAgent.jar` file can be run in 2 ways:

* Passing the following configuration parameters as arguments via command line:

```
# Linux
java -jar SIGAgent.jar \
    MESSAGE_BROKER=MQTT \
    TIMER_SCHEDULE_PERIOD=15000 \
    TOPIC_TO_PUBLISH_MESSAGES=<topic>

# Windows CMD
java -jar SIGAgent.jar \^
    MESSAGE_BROKER=MQTT \^
    TIMER_SCHEDULE_PERIOD=15000 \^
    TOPIC_TO_PUBLISH_MESSAGES=<topic>
```

* Without passing arguments to the execution of the jar. In this way, when starting the execution of the agent, the command line will request values ​​for the configuration that are necessary for communication with the node.

```
java -jar SIGAgent.jar
```

## 2. The Sigar API
SIGAgent uses the Sigar API (_System Information Gatherer And Reporter_) for gathering system information. The Sigar API has low-level implementations to obtain and interpret information about each system and architecture.

### 2.1. Overview:
The Sigar API provides a portable interface for gathering system information such as:
* System memory, swap, CPU, load average, uptime, logins.
* Per-process memory, CPU, credential info, state, arguments, environment, open files.
* File system detection and metrics.
* Network interface detection, configuration info and metrics.
* TCP and UDP connection tables.
* Network route table.

This information is available in most operating systems, but each OS has their own way(s) providing it.
SIGAR provides developers with one API to access this information regardless of the underlying platform.
**The core API is implemented in pure C with bindings currently implemented for Java, Perl, Ruby, Python, Erlang, PHP and C#**.

The following platforms are currently supported:

|Operating Systems|Architecture|Versions|Binary Included|
|------|------|------|------|
|Linux|x86|2.2, 2.4, 2.6 kernels|✅|
|Linux|amd64|2.6 kernel|✅|
|Linux|ppc|2.6 kernel|✅|
|Linux|ppc64|2.6 kernel|✅|
|Linux|ia64|2.6 kernel|✅|
|Linux|s390|2.6 kernel|❌|
|Linux|s390x|2.6 kernel|✅|
|Windows|x86|NT 4.0 to Windows 11|✅|
|Windows|x64|2003 Server, Vista, 2008 Server, 7, 2012 server|✅|
|Solaris|sparc-32|2.6, 7, 8, 9, 10|✅|
|Solaris|sparc-64|2.6, 7, 8, 9, 10|✅|
|Solaris|x86|8, 9, 10|✅|
|Solaris|x64|8, 9, 10|✅|
|AIX|ppc|4.3, 5.1, 5.2, 5.3, 6.1|✅|
|AIX|ppc64|5.2, 5.3, 6.1|✅|
|HP-UX|PA-RISC|11|✅|
|HP-UX|ia64|11|✅|
|FreeBSD|x86|4.x|❌|
|FreeBSD|x86|5.x, 6.x|✅|
|FreeBSD|x64|6.x|✅|
|FreeBSD|x86, x64|7.x, 8.x|❌|
|OpenBSD|x86|4.x, 5.x|❌|
|NetBSD|x86|3.1|❌|
|Mac OS X|PowerPC|10.3, 10.4|✅|
|Mac OS X|x86|10.4, 10.5, 10.6|✅|
|Mac OS X|x64|10.5, 10.6|✅|


While SIGAR only depends on the Linux kernel version, the following distributions have been certified:

|Distribution|Versions|
|------|------|
|Red Hat|6.2, 7.3, 8.0, 9.0|
|RHEL|3, 4, 5, 6|
|CentOS|3, 4, 5|
|Fedora|2, 3, 4, 5, 6, 7, 8, 9, 10|
|SuSE|8, 9, 10, 11|
|Ubuntu|6.06, 8.04, 8.10, 9.04|
|Debian|2.6, 3.0, 3.1, 3.2, 4.0, 5.0|
|VMware ESX|2.x, 3.0|
|XenServer|3.1, 3.2, 4.0, 4.1, 5.0|
|Slackware|10, 11|
|Mandrake|10|
|Scientific Linux|5|
|Gentoo||

### 2.2. License

SIGAR is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

### 2.3. Binaries
The SIGAR binary distribution contains the following files in `sigar-bin/lib`:

|File|Language|Description|Required|
|------|------|------|------|
|sigar.jar|Java|Java API|Yes (for Java only)|
|log4j.jar|Java|Java logging API|No|
|libsigar-x86-linux.so|C|Linux AMD/Intel 32-bit|*|
|libsigar-amd64-linux.so|C|Linux AMD/Intel 64-bit|*|
|libsigar-ppc-linux.so|C|Linux PowerPC 32-bit|*|
|libsigar-ppc64-linux.so|C|Linux PowerPC 64-bit|*|
|libsigar-ia64-linux.so|C|Linux Itanium 64-bit|*|
|libsigar-s390x-linux.so|C|Linux zSeries 64-bit|*|
|sigar-x86-winnt.dll|C|Windows AMD/Intel 32-bit|*|
|sigar-amd64-winnt.dll|C|Windows AMD/Intel 64-bit|*|
|libsigar-ppc-aix-5.so|C|AIX PowerPC 32-bit|*|
|libsigar-ppc64-aix-5.so|C|AIX PowerPC 64-bit|*|
|libsigar-pa-hpux-11.sl|C|HP-UX PA-RISC 32-bit|*|
|libsigar-ia64-hpux-11.sl|C|HP-UX Itanium 64-bit|*|
|libsigar-sparc-solaris.so|C|Solaris Sparc 32-bit|*|
|libsigar-sparc64-solaris.so|C|Solaris Sparc 64-bit|*|
|libsigar-x86-solaris.so|C|Solaris AMD/Intel 32-bit|*|
|libsigar-amd64-solaris.so|C|Solaris AMD/Intel 64-bit|*|
|libsigar-universal-macosx.dylib|C|Mac OS X PowerPC/Intel 32-bit|*|
|libsigar-universal64-macosx.dylib|C|Mac OS X PowerPC/Intel 64-bit|*|
|libsigar-x86-freebsd-5.so|C|FreeBSD 5.x AMD/Intel 32-bit|*|
|libsigar-x86-freebsd-6.so|C|FreeBSD 6.x AMD/Intel 64-bit|*|
|libsigar-amd64-freebsd-6.so|C|FreeBSD 6.x AMD/Intel 64-bit|*|

> [!NOTE]
> **(*) Required to run on listed OS + Architecture combo**  
> For example, minimal requirements to use the SIGAR Java API on Windows would be **sigar.jar** and **sigar-x86-winnt.dll**

### 2.4. Source code and documentation

* Official project link -> [[GitHub]](https://github.com/hyperic/sigar)  
* Source code and documentation in the cloud -> [[OneDrive]](https://drive.google.com/drive/folders/1TFWTriluRwecxJCbPh863rnB9SGUe6ja?usp=sharing)  

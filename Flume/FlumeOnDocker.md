

## Running Flume for the First Time

Using docker image: https://hub.docker.com/r/probablyfine/flume/

Here is what we are going to do:

1. Create a docker container with flume running.
   - We will mount `/opt/flume-config` configuration directory to be used by flume
   - We will mount a `/spool` directory, monitored by flume.
   - We will mount a `config.conf` file as `flume.conf` 
2. Copy the flume default configuration files from `/opt/flume/conf` to mounted configuration directory (`/opt/flume-config`). This will make configuration available to flume and persistent. 
3. We will create simple flume configuration for agent `agent1` which monitors the `/spool` directory and logs the changes.
4. Restart flume.
5. Create text file in the monitored directory.

Highlights:

* Docker image expects flume configuration to be in `/opt/flume-config` directory
* Docker image expects `flume.conf` to be present in the flume configuration directory
* Agent name needs to be specified as environment variable (`FLUME_AGENT_NAME`) during container creation 

```powershell
' Create a flume docker container to copy configuration files
PS> docker pull probablyfine/flume
PS> $FLUMEDIR = "C:\Sandbox\flume\"
PS> mkdir -p $FLUMEDIR\conf
PS> mkdir -p $FLUMEDIR\spool
PS> echo "" >$FLUMEDIR\conf\config.conf
PS> docker run --rm `
    --env FLUME_AGENT_NAME=agent1 `
    --volume $FLUMEDIR\conf\config.conf:/opt/flume-config/flume.conf `
    --volume $FLUMEDIR\conf:/opt/flume-config `
    --volume $FLUMEDIR\spool:/spool `
    --name flume `
    probablyfine/flume:latest
```

In another PowerShell get into flume container:

```powershell
PS>  docker exec -it flume bash
# Copy flume configuration
$ cp /opt/flume/conf/* /opt/flume-config/.
# Stop flume
$ ps -ef
UID        PID  PPID  C STIME TTY          TIME CMD
root         1     0  0 12:55 ?        00:00:00 /bin/bash /opt/flume/bin/start-flume
root         6     1  0 12:55 ?        00:00:02 /opt/java/bin/java -Xmx20m -Dflume.root.logger=INFO,console -cp /opt/flu
root        32     0  0 12:55 pts/0    00:00:00 bash
root       138    32  0 13:02 pts/0    00:00:00 ps -ef

$ kill -9 6
```

Modify the `C:\Sandbox\flume\config.conf` file:

```properties
agent1.sources = source1
agent1.sinks = sink1
agent1.channels = channel1

agent1.sources.source1.channels = channel1
agent1.sinks.sink1.channel = channel1

agent1.sources.source1.type = spooldir
agent1.sources.source1.spoolDir = /opt/flume-conf/spooldir

agent1.sinks.sink1.type = logger
agent1.channels.channel1.type = file
```

Start docker container for flume again (Step 4):

```powershell
PS> docker run --rm `
    --env FLUME_AGENT_NAME=agent1 `
    --volume $FLUMEDIR\conf\config.conf:/opt/flume-config/flume.conf `
    --volume $FLUMEDIR\conf:/opt/flume-config `
    --volume $FLUMEDIR\spool:/spool `
    --name flume `
    probablyfine/flume:latest
```

In another console connect to the container:

```powershell
PS> docker exec -it flume bash
```

And create some file in the `/spool` directory:

```bash
$ echo "Hello Flume" > /spool/.file1.txt
$ mv /spool/.file1.txt /spool/file1.txt
```

In the flume docker container console you will see logging showing the content of the file.



## Netcat to Rolling File

Inspired by [ExecuteFlumeSink NiFi Processor Additional Details](https://nifi.apache.org/docs/nifi-docs/components/org.apache.nifi/nifi-flume-nar/1.7.1/org.apache.nifi.processors.flume.ExecuteFlumeSink/additionalDetails.html).

Prepare following config:

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

a1.sources.r1.type = netcat
a1.sources.r1.bind = localhost
a1.sources.r1.port = 44444

a1.sinks.k1.type = file_roll
a1.sinks.k1.channel = c1
a1.sinks.k1.sink.directory = /var/log/flume

a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000
a1.channels.c1.transactionCapacity = 100

a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```



In one console start following:

```bash
$ apt-get install telnet
$ mkdir /var/log/flume

FLUME_CONF_DIR=/opt/flume-config
flume-ng agent \
   --conf-file $FLUME_CONF_DIR/config.conf \
   --name a1 \
   --conf $FLUME_CONF_DIR \
   -Dflume.root.logger=INFO,console
```



In another console:

```bash
$ telnet localhost 44444
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
Hello
OK
Ivan
OK
is here
OK
```



Check the results in a third console:

```bash
$ cd /var/log/flume
$ ls -R | grep '^[^\.]' | xargs cat
Hello
Ivan
is here
```



## How to Stop Flume

```bash
# Find the flume PID
ps -ef | grep flume
kill -9 6
# Note - this stops the container
```




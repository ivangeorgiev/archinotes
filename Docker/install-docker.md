# Install Docker



## Install Docker on Windows 10

### Upgrade to Windows Pro or Enterprise

Docker requires Windows Pro or Enterprise (Hyper-V is available in Windows 10 Pro).

* https://www.windowscentral.com/how-upgrade-windows-10-pro

### Install Docker

Download docker community edition (https://hub.docker.com/editions/community/docker-ce-desktop-windows   -- https://download.docker.com/win/stable/Docker%20for%20Windows%20Installer.exe ).

Double click `Docker for Windows Installer` to run the installer. Follow the installation steps.

Once docker is installed, open a command-line terminal like PowerShell and try docker command:

```
# Verify docker is up and running
> docker version
```

This would produce output similar to:

```
Client: Docker Engine - Community
 Version:           18.09.0
 API version:       1.39
 Go version:        go1.10.4
 Git commit:        4d60db4
 Built:             Wed Nov  7 00:47:51 2018
 OS/Arch:           windows/amd64
 Experimental:      false

Server: Docker Engine - Community
 Engine:
  Version:          18.09.0
  API version:      1.39 (minimum version 1.12)
  Go version:       go1.10.4
  Git commit:       4d60db4
  Built:            Wed Nov  7 00:55:00 2018
  OS/Arch:          linux/amd64
  Experimental:     false
```

Run a simple docker image to verify docker can pull and run images:

```
# Verify docker can pull and run images
> docker run hello-world
```

Expected output is something like:

```
Hello from Docker!
This message shows that your installation appears to be working correctly.

To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.

To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash

Share images, automate workflows, and more with a free Docker ID:
 https://hub.docker.com/

For more examples and ideas, visit:
 https://docs.docker.com/get-started/

```

## Run Ubuntu Container

```
docker run -it --name ubuntu ubuntu:19.04 bash
```


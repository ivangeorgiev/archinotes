



```bash
$ docker run -d --name database -e MYSQL_ROOT_PASSWORD=root mysql
$ docker run -d --link database:db --name web runseb/hostname
$ docker run -d --link web:application --name lb nginx
```



```bash
docker pull redis
docker run -d redis
docker pull mongo
docker pull postgres
docker pull ubuntu:latest

docker pull elasticsearch
```


# Explore Apache Kafka



## Installation

### Prerequisites

#### Docker

- Docker Desktop 2.0.0.0-win78 (28905)
- Docker Engine 18.08.0

### Install Kafka

We are using the following image: https://github.com/Landoop/fast-data-dev

```bash
docker run --rm -it -p 2181:2181 -p 3030:3030 -p 8081-8083:8081-8083 -p 9581-9585:9581-9585 -p 9092:9092 -e ADV_HOST=127.0.0.1 --name kafka landoop/fast-data-dev:latest
```

`--rm` option - when the container is stopped, it will be removed.

`--it`  container will run in interactive mode. To stop it, you stop Kafka, by hitting `Ctrl-C`

To see running Docker images:

```
docker ps
```

This Docker image comes with nice web UI:

```
http://localhost:3030/
```



### Install Ubuntu

We will use another docker image to interact with Kafka

```bash
docker pull ubuntu:18.04

docker run -it --rm --name ubuntu ubuntu:18.04 bash
apt update
apt-get install -y kafkacat

groupadd kafka \
    && useradd --shell /bin/bash -g kafka -m kafka

# Need earlier version
curl http://apache.mirror.triple-it.nl/kafka/2.0.1/kafka_2.11-2.0.1.tgz kafka_2.11-2.0.1.tgz


```

### Explore Docker Network

```bash
$ docker network ls
NETWORK ID          NAME                DRIVER              SCOPE
f4884c50476d        bridge              bridge              local
fd510b2c48d6        host                host                local
e1975bd7b852        none                null                local

$ docker network inspect bridge
        "Containers": {
            "b30c91e5a61b020b06e261f6ab9eb4a53d7b212fca6898b2a9d897a8d1f7451a": {
                "Name": "elastic",
                "EndpointID": "7ed0b0c53eb7c28dc76a3eeead13bd955ab00381258e62577b6ea8a3a0ff6dd1",
                "MacAddress": "02:42:ac:11:00:03",
                "IPv4Address": "172.17.0.3/16",
                "IPv6Address": ""
            },
            "c33f692283009e4da8f1fa6802f8707a00cadc86c6b2330793ff7a1ff24dc6cd": {
                "Name": "kafka",
                "EndpointID": "8e2fc4fb2b12642cab461b7cdf40b3913f685ae0e415a302bdf5c4017f6a3af2",
                "MacAddress": "02:42:ac:11:00:02",
                "IPv4Address": "172.17.0.2/16",
                "IPv6Address": ""
            },
            "ee8c491e98cd1f56deac108f7deaa27d30e2748b48f23634fdfc2d56351cab57": {
                "Name": "ubuntu",
                "EndpointID": "fb39698ac541702cac331b17438c0fb68022221a87476f4b6fe55f7b3a9e6289",
                "MacAddress": "02:42:ac:11:00:04",
                "IPv4Address": "172.17.0.4/16",
                "IPv6Address": ""
            }
        },
```



Note down the `kafka` container IP address is `172.17.0.2`

```bash
KAFKA_HOST=172.17.0.2
KAFKA_PORT=9092
KAFKA_BROKER=172.17.0.2:9092
```



## Interact on CLI

```bash
$ docker exec -it --user kafka ubuntu bash
$ KAFKA_BROKER=172.17.0.2:9092
$ kafkacat -L -b $KAFKA_BROKER
... follows a list of brokers, topics and partitions ...
# Same information in JSON format
$ kafkacat -L -b $KAFKA_BROKER -J
... same metadata formatted in JSON ...

```



### From the Kafka Container

```bash
$ docker exec -ti kafka bash
$ kafka-console-producer --broker-list localhost:9092 --topic allice
Hi
Allice here!
```



In another console

```bash
$ docker exec -ti kafka bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic allice --group mygroup --from-beginning
Hi
Allice here!
```



## Resources

* Udemy - [Apache Kafka Series - Learn Apache Kafka for Beginners v2](https://www.udemy.com/apache-kafka/)
* Safari Books - [Apache Kafka Series - Learn Apache Kafka for Beginners](https://www.safaribooksonline.com/videos/apache-kafka-series/9781789342604)
* [Kafkacat Utility](https://github.com/edenhill/kafkacat)
* 
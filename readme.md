# Flink playground


## Playground setup

Start docker-compose:

```
source ./env.sh
docker-compose up -d
```

Compile jar file:

```
mvn compile assembly:single
```

Create input topic and output topic

```
kafka-topics --create --bootstrap-server localhost:9092 --topic input-topic
```

## Submit job:

```
flink run -m localhost:18081 ./target/flink-etl-example-1.0.0-jar-with-dependencies.jar
```

## Insert dummy data:

- (Option1) Input one message in each line, press `Ctrl+D` to quite producer.

```
kafka-console-producer --bootstrap-server localhost:9092 --topic input-topic
```

- (Option2) Bulk insert.

```
kafka-console-producer --bootstrap-server localhost:9092 --topic input-topic < ./pom.xml
```


## Check output
Check the output from flink:

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic output-topic --from-beginning
```
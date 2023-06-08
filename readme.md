# Flink playground


Start docker-compose

```
source ./env.sh
docker-compose up -d
```

Compile jar file

```
mvn compile assembly:single
```

Submit job:

```
flink run -m localhost:18081 ./target/flink-etl-example-1.0.0-jar-with-dependencies.jar
```

Input dummy data:

- Input one message in each line.
- Press Ctrl+D to quite producer.

```
kafka-console-producer --bootstrap-server localhost:9092 --topic input-topic
```

Check the output from flink:

```
kafka-console-consumer --bootstrap-server localhost:9092 --topic output-topic --from-beginning
```
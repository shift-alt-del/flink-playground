
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Properties;

public class FlinkETLJob {

    public static void main(String[] args) throws Exception {
        // Set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Configure Kafka consumer properties
        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", "broker:9092");
        consumerProps.setProperty("group.id", "flink-consumer-group");

        // Create a Kafka consumer
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(
                "input-topic",
                new SimpleStringSchema(),
                consumerProps
        );

        // Add the Kafka consumer as a data source
        DataStream<String> inputStream = env.addSource(kafkaConsumer);

        // Apply your ETL logic
        DataStream<String> transformedStream = inputStream.map(data -> {
            // Perform your ETL transformation here
            // For example, convert data to uppercase
            return data.toUpperCase();
        });

        // Configure Kafka producer properties
        Properties producerProps = new Properties();
        producerProps.setProperty("bootstrap.servers", "broker:9092");

        // Create a Kafka producer
        FlinkKafkaProducer<String> kafkaProducer = new FlinkKafkaProducer<>(
                "output-topic",
                new SimpleStringSchema(),
                producerProps
        );

        // Add the Kafka producer as a data sink
        transformedStream.addSink(kafkaProducer);

        // Execute the Flink job
        env.execute("Flink ETL Job");
    }
}

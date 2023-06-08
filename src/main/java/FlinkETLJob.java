
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Properties;

public class FlinkETLJob {

    public static void main(String[] args) throws Exception {
        // Set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/datastream/kafka/
        env.fromSource(KafkaSource.<String>builder()
                                .setBootstrapServers("broker:9092")
                                .setTopics("input-topic")
                                .setGroupId("flink-consumer-group")
                                .setStartingOffsets(OffsetsInitializer.earliest())
                                .setValueOnlyDeserializer(new SimpleStringSchema())
                                .build(),
                        WatermarkStrategy.noWatermarks(), "Kafka source")
                .map(String::toUpperCase)
                .sinkTo(KafkaSink.<String>builder()
                        .setBootstrapServers("broker:9092")
                        .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                                .setTopic("output-topic")
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .build())
                        .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                        .build());

        // Execute the Flink job
        env.execute("Flink ETL Job");
    }
}

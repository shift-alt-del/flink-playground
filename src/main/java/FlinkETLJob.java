import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

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
                        WatermarkStrategy.noWatermarks(), "intput")
                .flatMap(new Tokenizer()).name("tokenizer")
                .keyBy((v) -> (v).f0).sum(1).name("counter")
                .map((v) -> v.toString()).name("parse-string")
                .sinkTo(KafkaSink.<String>builder()
                        .setBootstrapServers("broker:9092")
                        .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                                .setTopic("output-topic")
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .build())
                        .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                        .build()).name("output");

        // Execute the Flink job
        env.execute("Flink ETL Job");
    }

    public static final class Tokenizer
            implements FlatMapFunction<String, Tuple2<String, Integer>> {

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            // normalize and split the line
            String[] tokens = value.toLowerCase().split("\\W+");

            // emit the pairs
            for (String token : tokens) {
                if (token.length() > 0) {
                    out.collect(new Tuple2<>(token, 1));
                }
            }
        }
    }

}

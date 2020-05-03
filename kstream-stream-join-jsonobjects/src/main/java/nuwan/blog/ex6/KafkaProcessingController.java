package nuwan.blog.ex6;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Properties;

@RestController
public class KafkaProcessingController {
    private KafkaStreams streamsInnerJoin;

    private Properties properties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream-stream-inner-join");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        return props;
    }

    private void streamsInnerJoinStart(StreamsBuilder builder) {
        if (streamsInnerJoin != null) {
            streamsInnerJoin.close();
        }
        final Topology topology = builder.build();
        streamsInnerJoin = new KafkaStreams(topology, properties());
        streamsInnerJoin.start();
    }

    public class ItemSerde extends Serdes.WrapperSerde<Item> {
        public ItemSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>(Item.class));
        }
    }

    @Autowired
    KafkaProducerController kafkaProducerController;

    @RequestMapping("/startStreaJoin/")
    public void startStreamStreamInnerJoin3Auto() throws InterruptedException {
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Item> leftSource = builder.stream("my-kafka-left-stream-topic"
                , Consumed.with(Serdes.String(), new ItemSerde()));
        KStream<String, Item> rightSource = builder.stream("my-kafka-right-stream-topic"
                , Consumed.with(Serdes.String(), new KafkaProcessingController.ItemSerde()));

        KStream<String, Item> joined = leftSource
                .selectKey((key, value) -> key)
                .join(rightSource.selectKey((key, value) -> key)
                        , (value1, value2) -> {
                            System.out.println("value2.getName() >> " + value1.getName() + value2.getName());
                            value2.setCategory(value1.getCategory());
                            return value2;
                        }
                        , JoinWindows.of(Duration.ofSeconds(10))
                        , Joined.with(
                                Serdes.String(),
                                new ItemSerde(),
                                new ItemSerde()
                        )
                );
        joined.to("my-kafka-stream-stream-inner-join-out", Produced.with(Serdes.String(), new ItemSerde()));
        streamsInnerJoinStart(builder);
    }
}// end public class KafkaProcessingController
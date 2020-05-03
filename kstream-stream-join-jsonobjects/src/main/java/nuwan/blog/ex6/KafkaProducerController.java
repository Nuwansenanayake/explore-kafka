package nuwan.blog.ex6;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class KafkaProducerController {

    public static void main(String[] args) {
        SpringApplication.run(KafkaProducerController.class, args);
    }

    @Autowired
    KafkaTemplate<String, Item> KafkaJsontemplate;
    String TOPIC_NAME = "my-kafka-left-stream-topic";
    String TOPIC_NAME_2 = "my-kafka-right-stream-topic";
    Item item;

    public ProducerFactory<String, Item> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Item> kafkaTemplate() {
        return new KafkaTemplate<String, Item>(producerFactory());
    }

    //sending same msg to both topics
    @RequestMapping("/sendMessages2/")
    public String sendMessages2() {
        System.out.println("processing>> " + item);

        item = new Item(1, "nuwan", "home");
        KafkaJsontemplate.send(TOPIC_NAME, "1", item);

        return "Message ONE published successfully";
    }

    @RequestMapping("/sendMessages3a/")
    public String sendMessages3a() {
        System.out.println("processing>> " + item);

        item = new Item(1, "chamara", "office");
        KafkaJsontemplate.send(TOPIC_NAME_2, "1", item);

        return "Message ONE published successfully";
    }
}// end of public class KafkaProducerController
package com.datasrc.config;

import static com.datasrc.config.JsonDeserializer.OBJECT_MAPPER;
import static com.datasrc.config.JsonDeserializer.TYPE_REFERENCE;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.GROUP_INSTANCE_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import com.datasrc.ConsumerException;
import com.datasrc.model.StringValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.util.retry.Retry;

public final class ReactiveReceiver {
    private static final Logger log = LoggerFactory.getLogger(ReactiveReceiver.class);
    private final Random random = new Random();
    private final Flux<ConsumerRecord<Long, StringValue>> inboundFlux;

    public static final String GROUP_ID_CONFIG_NAME = "reactiveKafkaConsumerGroup";
    public static final int MAX_POLL_INTERVAL_MS = 1_000;

    public ReactiveReceiver(String bootstrapServers, String topicName, Scheduler schedulerValueReceiver) {
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(GROUP_ID_CONFIG, GROUP_ID_CONFIG_NAME);
        props.put(GROUP_INSTANCE_ID_CONFIG, makeGroupInstanceIdConfig());
        props.put(ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(OBJECT_MAPPER, new ObjectMapper());
        props.put(TYPE_REFERENCE, new TypeReference<StringValue>() {});

        props.put(MAX_POLL_RECORDS_CONFIG, 3);
        props.put(MAX_POLL_INTERVAL_MS_CONFIG, MAX_POLL_INTERVAL_MS);

        ReceiverOptions<Long, StringValue> receiverOptions = ReceiverOptions.<Long, StringValue>create(props)
                .pollTimeout(Duration.ofSeconds(500))
                .schedulerSupplier(() -> schedulerValueReceiver)
                .subscription(Collections.singleton(topicName));

        inboundFlux = KafkaReceiver.create(receiverOptions)
                .receiveAutoAck()
                .concatMap(consumerRecordFlux -> {
                    log.info("consumerRecordFlux done, commit");
                    return consumerRecordFlux;
                })
                .retryWhen(Retry.backoff(3, Duration.of(10L, ChronoUnit.SECONDS)));
    }

    public Flux<ConsumerRecord<Long, StringValue>> getInboundFlux() {
        return inboundFlux;
    }

    public String makeGroupInstanceIdConfig() {
        try {
            var hostName = InetAddress.getLocalHost().getHostName();
            return String.join("-", GROUP_ID_CONFIG_NAME, hostName, String.valueOf(random.nextInt(100_999_999)));
        } catch (Exception ex) {
            throw new ConsumerException("can't make GroupInstanceIdConfig", ex);
        }
    }
}

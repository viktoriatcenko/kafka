package ru.demo.service;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import ru.demo.model.MessageValue;

public class DataSenderImpl implements DataSender {
    private static final Logger log = LoggerFactory.getLogger(DataSenderImpl.class);

    private final KafkaTemplate<String, MessageValue> template;

    private final Consumer<MessageValue> sendAsk;

    private final String topic;

    public DataSenderImpl(
            String topic,
            KafkaTemplate<String, MessageValue> template,
            Consumer<MessageValue> sendAsk) {
        this.topic = topic;
        this.template = template;
        this.sendAsk = sendAsk;
    }

    @Override
    public void send(MessageValue value) {
        try {
            log.info("value:{}", value);
            template.send(topic, value)
                    .whenComplete(
                            (result, ex) -> {
                                if (ex == null) {
                                    log.info(
                                            "message id:{} was sent, offset:{}",
                                            value.id(),
                                            result.getRecordMetadata().offset());
                                    sendAsk.accept(value);
                                } else {
                                    log.error("message id:{} was not sent", value.id(), ex);
                                }
                            });
        } catch (Exception ex) {
            log.error("send error, value:{}", value, ex);
        }
    }
}

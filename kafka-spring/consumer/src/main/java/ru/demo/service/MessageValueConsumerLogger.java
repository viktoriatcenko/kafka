package ru.demo.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.demo.model.MessageValue;

public class MessageValueConsumerLogger implements MessageValueConsumer {
    private static final Logger log = LoggerFactory.getLogger(MessageValueConsumerLogger.class);

    @Override
    public void accept(List<MessageValue> values) {
        for (var value : values) {
            log.info("log:{}", value);
        }
    }
}

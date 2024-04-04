package ru.demo.service;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.demo.model.MessageValue;

public class MessageValueSource implements ValueSource {
    private static final Logger log = LoggerFactory.getLogger(MessageValueSource.class);
    private final AtomicLong nextValue = new AtomicLong(1);

    // private final Long long = 1L;
    private final DataSender valueConsumer;

    public MessageValueSource(DataSender dataSender) {
        this.valueConsumer = dataSender;
    }

    @Override
    public void generate() {
        var executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> valueConsumer.send(makeValue()), 0, 1, TimeUnit.SECONDS);
        log.info("generation of message started");
    }

    private MessageValue makeValue() {
        var id = nextValue.getAndIncrement();
        return new MessageValue(id, "value of message is :" + id);
    }
}

package ru.demo.service;

import java.util.List;
import ru.demo.model.MessageValue;

public interface MessageValueConsumer {

    void accept(List<MessageValue> value);
}

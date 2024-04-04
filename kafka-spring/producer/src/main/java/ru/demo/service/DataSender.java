package ru.demo.service;

import ru.demo.model.MessageValue;

public interface DataSender {
    void send(MessageValue value);
}

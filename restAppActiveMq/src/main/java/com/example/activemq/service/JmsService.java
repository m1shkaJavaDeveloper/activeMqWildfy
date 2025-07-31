package com.example.activemq.service;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.Optional;

@Service
public class JmsService {

    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private MessageConsumer consumer;
    private boolean connected = false;

    public synchronized void connect(String brokerUrl, String username, String password) throws JMSException {
        if (connected) {
            throw new IllegalStateException("Already connected");
        }
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            if (username != null && !username.isEmpty()) {
                connection = connectionFactory.createConnection(username, password);
            } else {
                connection = connectionFactory.createConnection();
            }
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connected = true;
        } catch (JMSException e) {
            connected = false;
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ignored) {
                }
            }
            throw e;
        }
    }

    public synchronized void disconnect() {
        if (!connected) {
            throw new IllegalStateException("Not connected");
        }

        try {
            if (producer != null) producer.close();
            if (consumer != null) consumer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException e) {
            // логировать при необходимости
        } finally {
            producer = null;
            consumer = null;
            session = null;
            connection = null;
            connected = false;
        }
    }

    public synchronized void send(String queueName, String messageText) throws JMSException {
        ensureConnected();

        Destination destination = session.createQueue(queueName);
        if (producer == null) {
            producer = session.createProducer(destination);
        }

        TextMessage message = session.createTextMessage(messageText);
        producer.send(message);
    }

    public synchronized Optional<String> receive(String queueName) throws JMSException {
        ensureConnected();

        Destination destination = session.createQueue(queueName);
        if (consumer == null) {
            consumer = session.createConsumer(destination);
        }

        Message message = consumer.receive(1000); // wait up to 1 second
        if (message == null) {
            return Optional.empty();
        }

        if (message instanceof TextMessage) {
            return Optional.of(((TextMessage) message).getText());
        } else {
            return Optional.of("[Non-text message received]");
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private void ensureConnected() {
        if (!connected) {
            throw new IllegalStateException("Not connected to broker");
        }
    }

    @PreDestroy
    public void cleanup() {
        if (connected) {
            disconnect();
        }
    }
}

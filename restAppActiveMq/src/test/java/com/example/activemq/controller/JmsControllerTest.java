package com.example.activemq.controller;

import com.example.activemq.service.JmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.jms.JMSException;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JmsControllerTest {

    @InjectMocks
    private JmsController controller;

    @Mock
    private JmsService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void connect_success() throws Exception {
        JmsController.ConnectRequest req = new JmsController.ConnectRequest();
        req.setBrokerUrl("tcp://localhost:61616");
        req.setUsername("user");
        req.setPassword("pass");

        doNothing().when(service).connect(anyString(), anyString(), anyString());

        ResponseEntity<?> response = controller.connect(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("status", "connected"));
        verify(service).connect("tcp://localhost:61616", "user", "pass");
    }

    @Test
    void connect_illegalStateException() throws Exception {
        JmsController.ConnectRequest request = new JmsController.ConnectRequest();
        request.setBrokerUrl("someUrl");
        request.setUsername("user");
        request.setPassword("pass");

        doThrow(new IllegalStateException("Already connected"))
                .when(service).connect(anyString(), anyString(), anyString());

        ResponseEntity<?> response = controller.connect(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("error", "Already connected");    }

    @Test
    void connect_jmsException() throws Exception {
        JmsController.ConnectRequest req = new JmsController.ConnectRequest();
        req.setBrokerUrl("url");
        req.setUsername(null);
        req.setPassword(null);

        doThrow(new JMSException("Broker down"))
                .when(service).connect(any(), any(), any());

        ResponseEntity<?> response = controller.connect(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Connection failed: Broker down"));
    }

    @Test
    void disconnect_success() {
        doNothing().when(service).disconnect();

        ResponseEntity<?> response = controller.disconnect();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("status", "disconnected"));
        verify(service).disconnect();
    }

    @Test
    void disconnect_illegalStateException() {
        doThrow(new IllegalStateException("Not connected"))
                .when(service).disconnect();

        ResponseEntity<?> response = controller.disconnect();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Not connected"));
    }

    @Test
    void sendMessage_success() throws Exception {
        JmsController.SendRequest req = new JmsController.SendRequest();
        req.setQueueName("queue1");
        req.setMessage("hello");

        doNothing().when(service).send(anyString(), anyString());

        ResponseEntity<?> response = controller.sendMessage(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("status", "message sent"));
        verify(service).send("queue1", "hello");
    }

    @Test
    void sendMessage_illegalStateException() throws Exception {
        JmsController.SendRequest req = new JmsController.SendRequest();
        req.setQueueName("queue1");
        req.setMessage("msg");

        doThrow(new IllegalStateException("Not connected"))
                .when(service).send(anyString(), anyString());

        ResponseEntity<?> response = controller.sendMessage(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Not connected"));
    }

    @Test
    void sendMessage_jmsException() throws Exception {
        JmsController.SendRequest req = new JmsController.SendRequest();
        req.setQueueName("queue1");
        req.setMessage("msg");

        doThrow(new JMSException("Send failed"))
                .when(service).send(anyString(), anyString());

        ResponseEntity<?> response = controller.sendMessage(req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Failed to send message: Send failed"));
    }

    @Test
    void receiveMessage_successWithMessage() throws Exception {
        when(service.receive("queue1")).thenReturn(Optional.of("msg"));

        ResponseEntity<?> response = controller.receiveMessage("queue1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("message", "msg"));
    }

    @Test
    void receiveMessage_successEmpty() throws Exception {
        when(service.receive(anyString())).thenReturn(Optional.of("some message"));

        ResponseEntity<?> response = controller.receiveMessage("queue1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("message", "some message"));
    }

    @Test
    void receiveMessage_illegalStateException() throws Exception {
        when(service.receive("queue1")).thenThrow(new IllegalStateException("Not connected"));

        ResponseEntity<?> response = controller.receiveMessage("queue1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Not connected"));
    }

    @Test
    void receiveMessage_jmsException() throws Exception {
        when(service.receive("queue1")).thenThrow(new JMSException("Receive failed"));

        ResponseEntity<?> response = controller.receiveMessage("queue1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Failed to receive message: Receive failed"));
    }

    @Test
    void getStatus_connected() {
        when(service.isConnected()).thenReturn(true);

        ResponseEntity<?> response = controller.getStatus();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("status", "connected"));
    }

    @Test
    void getStatus_disconnected() {
        when(service.isConnected()).thenReturn(false);

        ResponseEntity<?> response = controller.getStatus();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("status", "disconnected"));
    }

    @Test
    void testEndpoint() {
        String response = controller.test();
        assertThat(response).isEqualTo("WildFly is working!");
    }
}

package com.example.activemq.controller;

import com.example.activemq.service.JmsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.jms.JMSException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class JmsController {

    private final JmsService jmsService;

    public JmsController(JmsService jmsService) {
        this.jmsService = jmsService;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody ConnectRequest request) {
        try {
            jmsService.connect(request.getBrokerUrl(), request.getUsername(), request.getPassword());
            return ResponseEntity.ok(Map.of("status", "connected"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (JMSException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "Connection failed: " + e.getMessage()));
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect() {
        try {
            jmsService.disconnect();
            return ResponseEntity.ok(Map.of("status", "disconnected"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody SendRequest request) {
        try {
            jmsService.send(request.getQueueName(), request.getMessage());
            return ResponseEntity.ok(Map.of("status", "message sent"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (JMSException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    @GetMapping("/receive")
    public ResponseEntity<?> receiveMessage(@RequestParam(defaultValue = "testQueue") String queueName) {
        try {
            Optional<String> result = jmsService.receive(queueName);

            if (result.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(Map.of("message", result.get()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (JMSException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Failed to receive message: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of("status", jmsService.isConnected() ? "connected" : "disconnected"));
    }

    @GetMapping("/test")
    public String test() {
        return "WildFly is working!";
    }

    public static class ConnectRequest {
        private String brokerUrl;
        private String username;
        private String password;

        public String getBrokerUrl() {
            return brokerUrl;
        }

        public void setBrokerUrl(String brokerUrl) {
            this.brokerUrl = brokerUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class SendRequest {
        private String queueName;
        private String message;

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

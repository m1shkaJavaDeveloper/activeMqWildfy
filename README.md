# Test Assignment: ActiveMQ Integration with WildFly and Spring Boot

## Overview

This project demonstrates integration of a Spring Boot backend deployed as a WAR on WildFly 26.1.0.Final, using ActiveMQ 5.19.0 as the message broker. The backend provides REST API endpoints to connect/disconnect, send, and receive messages from ActiveMQ queues.

The ActiveMQ client libraries are loaded via a WildFly shared module (not included in the WAR), following best practices.

---

## Folder Structure

- `apache-activemq-5.19.0/`  
  The ActiveMQ broker distribution. Start this broker before testing the REST API.

- `SAPTEST/`  
  The Spring Boot backend source code including `pom.xml` and Java source files.

- `wildfly-26.1.0.Final/`  
  The WildFly 26.1.0.Final application server distribution.

- `activemq-wildfly-api.war`  
  The compiled WAR file ready for deployment on WildFly.

---

## Setup and Running Instructions

### Start ActiveMQ Broker

Navigate to the `apache-activemq-5.19.0` folder and start the broker:

```bash
cd apache-activemq-5.19.0/bin
./activemq start

```
## Running WildFly Server

1. Navigate to the WildFly installation directory:

```bash
cd wildfly-26.1.0.Final/bin
./standalone.sh or standalone.bat
```

## Copy the WAR file into WildFly's deployment folder (if not done already):
```bash
cp activemq-wildfly-api.war ../standalone/deployments/
```bash

## Setup Postman requests
Base URL: http://localhost:8080


```Example requests
1. Connect to ActiveMQ broker

Method: POST

URL: http://localhost:8080/api/connect

Body (raw JSON):

json
{
  "brokerUrl": "tcp://localhost:61616"
}
2.Send a message

Method: POST

URL: http://localhost:8080/api/send

Body (raw JSON):

json
{
  "queueName": "testQueue",
  "message": "Hello from Postman"
}
3. Receive a message

Method: GET

URL: http://localhost:8080/api/receive?queueName=testQueue

4.Check connection status

Method: GET

URL: http://localhost:8080/api/status

5. Disconnect from ActiveMQ

Method: POST

URL: http://localhost:8080/api/disconnect


For POST requests, set header Content-Type to application/json.```

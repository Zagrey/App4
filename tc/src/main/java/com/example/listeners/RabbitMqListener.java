package com.example.listeners;


import com.example.model.*;
import com.example.service.GreetingService;
import com.example.service.PointService;
import com.example.thread.ThreadPool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@EnableRabbit
@Component
public class RabbitMqListener {

    @Autowired
    private GreetingService greetingService;
    @Autowired
    private PointService pointService;

    private Logger logger = LoggerFactory.getLogger(RabbitMqListener.class);
    private ObjectMapper mapper = new ObjectMapper();

    @RabbitListener(queues = "${rabbit.input.queue}")
    public void tcListener(String message) throws InterruptedException {
        logger.info("tcListener: " + message);
        try {
            TypeReference<RequestObject> reqRef = new TypeReference<RequestObject>() {
            };
            RequestObject requestObject = mapper.readValue(message, reqRef);


            if (requestObject instanceof GreetingRequestObject) {
                GreetingRequestObject greetingRequestObject = (GreetingRequestObject) requestObject;
                Task t = new GreetingTask(greetingRequestObject.getGreeting(), requestObject.getAction(), greetingService);
                ThreadPool.getInstance().submitTask(t);
            } else if (requestObject instanceof PointRequestObject) {
                PointRequestObject pointRequestObject = (PointRequestObject) requestObject;
                Task t = new PointTask(pointRequestObject.getPoint(), requestObject.getAction(), pointService);
                ThreadPool.getInstance().submitTask(t);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}

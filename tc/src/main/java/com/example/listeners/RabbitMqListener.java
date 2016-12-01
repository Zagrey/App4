package com.example.listeners;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.model.GreetingRequestObject;
import com.example.model.GreetingTask;
import com.example.model.Task;
import com.example.service.GreetingService;
import com.example.thread.ThreadPool;
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
    private Logger logger = LoggerFactory.getLogger(RabbitMqListener.class);
    private ObjectMapper mapper = new ObjectMapper();

    @RabbitListener(queues = "${rabbit.input.queue}")
    public void worker1(String message) throws InterruptedException {
        logger.info("worker 1 submit: " + message);
        try {
            TypeReference<GreetingRequestObject> groRef = new TypeReference<GreetingRequestObject>() {};
            GreetingRequestObject gro = mapper.readValue(message, groRef);
            Task t = new GreetingTask(gro.getGreeting(), gro.getAction(), greetingService);
            ThreadPool.getInstance().submitTask(t);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}

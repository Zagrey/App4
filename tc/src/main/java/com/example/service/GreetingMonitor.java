package com.example.service;

import com.common.util.Greeting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.Collection;

//@Profile("tc")
@Component
public class GreetingMonitor {
    private Logger logger = LoggerFactory.getLogger(GreetingMonitor.class);
    @Autowired
    private GreetingService greetingService;

    @Scheduled(cron = "*/30 * * * * *")
    public void cronJob() {
        Collection<Greeting> greetings = greetingService.findAll();
        logger.info("Monitor: There are {} greetings in the data store.", greetings.size());
    }

}
package com.example.model;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class Task implements Callable<String> {
    Logger logger = LoggerFactory.getLogger(Task.class);
    // do something after task processing in finally block
    // boolean status of task passing into consumer as parameter
//    private final Consumer<Boolean> finallyHandler;



    @Override
    public String call() throws Exception {

        logger.info("Task started");
        Thread.sleep(2000);
        logger.info("Task finished");

//        finallyHandler.accept(true);

        return null;
    }
}

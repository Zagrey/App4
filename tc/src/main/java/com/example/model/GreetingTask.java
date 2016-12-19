package com.example.model;

import com.example.service.GreetingService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.common.util.Greeting;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class GreetingTask extends Task {
    private Greeting greeting;
    private String action = "create";
    private GreetingService greetingService;

    @Override
    public String call() throws Exception {

//        logger.info("Greeting Task started");

        if ("create".equals(action)){
            Greeting savedGreeting = greetingService.create(greeting);
        } else if ("update".equals(action)){
            Greeting savedGreeting = greetingService.update(greeting);
        } else if ("delete".equals(action)){
            greetingService.delete(greeting.getId());
        } else {
            logger.info("Unknown task type");
        }

//        logger.info("Greeting Task finished");

        return null;
    }
}

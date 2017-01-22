package com.tc.it;


import com.common.util.Greeting;
import com.example.TcApplication;
import lombok.extern.java.Log;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@RunWith(SpringRunner.class)
@SpringApplicationConfiguration(classes = TcApplication.class)
@WebIntegrationTest
@Log
public class TcAppServicesIT {

    RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testPostAndGet() {

        Map<String, String> vars = new HashMap<>();

        for (int i = 0; i < 1000; i++) {
            Greeting g = new Greeting();
            g.setText("user-" + i);
            String uri = "http://localhost:8080/api/greetings";
            Greeting response = restTemplate.postForObject(uri, g, Greeting.class, vars);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }

        ResponseEntity<List<Greeting>> responseEntity = restTemplate.exchange("http://localhost:8080/api/greetings",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Greeting>>() {
                });

        List<Greeting> actualList = responseEntity.getBody();
        assertThat(actualList.size(), greaterThan(5));
    }

    @Test
    @Ignore
    public void testPostAndGetParallel() {

        ExecutorService executor = Executors.newFixedThreadPool(10);
        String uri = "http://localhost:8080/api/greetings";

        Map<String, String> vars = new HashMap<>();
        List<Greeting> gList= new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Greeting g = new Greeting();
            g.setText("user-" + i);
            gList.add(g);
        }

        for (Greeting g : gList) {
            executor.execute(new Runnable() {
                @Override
                public void run() {

                    log.warning(g.toString());
                    Greeting response = restTemplate.postForObject(uri, g, Greeting.class, vars);
                }
            });
        }

        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {
        }
        ResponseEntity<List<Greeting>> responseEntity = restTemplate.exchange("http://localhost:8080/api/greetings",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Greeting>>() {
                });

        List<Greeting> actualList = responseEntity.getBody();
        assertThat(actualList.size(), greaterThan(5));
    }
}

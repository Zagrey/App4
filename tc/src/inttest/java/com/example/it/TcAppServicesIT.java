package com.example.it;


import com.common.util.Greeting;
import com.example.TcApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@RunWith(SpringRunner.class)
@SpringApplicationConfiguration(classes = TcApplication.class)
@WebIntegrationTest
public class TcAppServicesIT {

    RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testPostAndGet() {

        Map<String, String> vars = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            Greeting g = new Greeting();
            g.setText("user-" + i);
            String uri = "http://localhost:8080/api/greetings";
            Greeting response = restTemplate.postForObject(uri, g, Greeting.class, vars);
        }

        ResponseEntity<List<Greeting>> responseEntity = restTemplate.exchange("http://localhost:8080/api/greetings",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Greeting>>() {
                });

        List<Greeting> actualList = responseEntity.getBody();
        assertThat(actualList.size(), greaterThan(5));

    }
}

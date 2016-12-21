package com.tc.it;

import com.example.TcApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringApplicationConfiguration(classes = TcApplication.class)
@SpringBootTest
public class TcApplicationIT {

    @Test
    public void contextLoads() {
    }

}

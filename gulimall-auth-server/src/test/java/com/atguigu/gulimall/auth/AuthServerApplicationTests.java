package com.atguigu.gulimall.auth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthServerApplicationTests {


    @Test
    public void contextLoads() {
        RestTemplate restTemplate = new RestTemplate();
        String forObject = restTemplate.getForObject("http://www.baidu.com", String.class);

        System.out.println("restTemplate = " + restTemplate);
    }

}

package com.atguigu.gulimall.member.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Description: TODO
 * @Author fengjc
 * @Date 2020/12/9
 * @Version V1.0
 **/
@RestController
public class TestController {
    @GetMapping("/test")
    public String test(){
        return "test success!";
    }
}

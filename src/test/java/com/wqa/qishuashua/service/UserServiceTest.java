package com.wqa.qishuashua.service;

import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 用户服务测试
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void userRegister() {
        String userAccount = "wqa";
        String userPassword = "";
        String checkPassword = "123456";
        String phoneNumber = "18179307801";
        String userName = "wqa";
        try {
            long result = userService.userRegister(userAccount, userPassword, checkPassword, phoneNumber, userName);
            Assertions.assertEquals(-1, result);
            userAccount = "yu";
            result = userService.userRegister(userAccount, userPassword, checkPassword, phoneNumber, userName);
            Assertions.assertEquals(-1, result);
        } catch (Exception e) {

        }
    }
}

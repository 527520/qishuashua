package com.wqa.qishuashua.service;

import com.wqa.qishuashua.model.entity.User;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 题目点赞服务测试
 */
@SpringBootTest
class QuestionThumbServiceTest {

    @Resource
    private QuestionThumbService questionThumbService;

    private static final User loginUser = new User();

    @BeforeAll
    static void setUp() {
        loginUser.setId(1L);
    }

    @Test
    void doQuestionThumb() {
        int i = questionThumbService.doQuestionThumb(1L, loginUser);
        Assertions.assertTrue(i >= 0);
    }
}

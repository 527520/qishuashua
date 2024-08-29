package com.wqa.qishuashua.service;

import javax.annotation.Resource;

import com.wqa.qishuashua.model.dto.question.QuestionQueryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 题目服务测试
 */
@SpringBootTest
class QuestionServiceTest {

    @Resource
    private QuestionService questionService;

    @Test
    void searchFromEs() {
        QuestionQueryRequest postQueryRequest = new QuestionQueryRequest();
        postQueryRequest.setUserId(1L);
//        Page<Question> postPage = questionService.searchFromEs(postQueryRequest);
//        Assertions.assertNotNull(postPage);
    }

}
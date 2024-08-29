package com.wqa.qishuashua.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wqa.qishuashua.model.entity.Question;
import com.wqa.qishuashua.model.entity.User;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 题目收藏服务测试
 */
@SpringBootTest
class QuestionFavourServiceTest {

    @Resource
    private QuestionFavourService questionFavourService;

    private static final User loginUser = new User();

    @BeforeAll
    static void setUp() {
        loginUser.setId(1L);
    }

    @Test
    void doQuestionFavour() {
        int i = questionFavourService.doQuestionFavour(1L, loginUser);
        Assertions.assertTrue(i >= 0);
    }

    @Test
    void listFavourQuestionByPage() {
        QueryWrapper<Question> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.eq("id", 1L);
        questionFavourService.listFavourQuestionByPage(Page.of(0, 1), postQueryWrapper, loginUser.getId());
    }
}

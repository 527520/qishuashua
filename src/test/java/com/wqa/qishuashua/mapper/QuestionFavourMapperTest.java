package com.wqa.qishuashua.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import javax.annotation.Resource;

import com.wqa.qishuashua.model.entity.Question;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 题目收藏数据库操作测试
 */
@SpringBootTest
class QuestionFavourMapperTest {

    @Resource
    private QuestionFavourMapper questionFavourMapper;

    @Test
    void listUserFavourQuestionByPage() {
        IPage<Question> page = new Page<>(2, 1);
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", 1);
        queryWrapper.like("content", "a");
        IPage<Question> result = questionFavourMapper.listFavourQuestionByPage(page, queryWrapper, 1);
        Assertions.assertNotNull(result);
    }
}
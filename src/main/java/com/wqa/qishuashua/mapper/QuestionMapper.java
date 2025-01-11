package com.wqa.qishuashua.mapper;

import com.wqa.qishuashua.model.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
* @author lenovo
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2024-08-27 11:53:13
* @Entity com.wqa.qishuashua.model.entity.Question
*/
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("select * from question where updateTime >= #{minUpdateTime}")
    List<Question> listQuestionWithDelete(Date minUpdateTime);
}





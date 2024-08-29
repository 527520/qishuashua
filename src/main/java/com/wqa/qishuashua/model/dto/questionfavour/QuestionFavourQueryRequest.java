package com.wqa.qishuashua.model.dto.questionfavour;

import com.wqa.qishuashua.common.PageRequest;
import java.io.Serializable;

import com.wqa.qishuashua.model.dto.question.QuestionQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目收藏查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionFavourQueryRequest extends PageRequest implements Serializable {

    /**
     * 题目查询请求
     */
    private QuestionQueryRequest questionQueryRequest;

    /**
     * 用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
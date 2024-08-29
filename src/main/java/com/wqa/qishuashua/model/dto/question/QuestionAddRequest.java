package com.wqa.qishuashua.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 创建题目请求
 *
 */
@Data
public class QuestionAddRequest implements Serializable {

    /**
     * 题库id
     */
    private Set<Long> questionBankIds;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 推荐答案
     */
    private String answer;

    /**
     * 可见状态：0-所有人可见, 1-仅本人可见
     */
    private Integer visibleStatus;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 题目来源
     */
    private String source;

    /**
     * 仅会员可见（1 表示仅会员可见）
     */
    private Integer needVip;

    private static final long serialVersionUID = 1L;
}
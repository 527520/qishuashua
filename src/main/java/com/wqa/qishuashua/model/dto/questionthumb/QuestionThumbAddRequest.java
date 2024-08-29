package com.wqa.qishuashua.model.dto.questionthumb;

import java.io.Serializable;
import lombok.Data;

/**
 * 题目点赞请求
 */
@Data
public class QuestionThumbAddRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}
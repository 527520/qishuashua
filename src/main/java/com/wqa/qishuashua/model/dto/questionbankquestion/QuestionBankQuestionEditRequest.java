package com.wqa.qishuashua.model.dto.questionbankquestion;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑题库题目关联请求
 *
 */
@Data
public class QuestionBankQuestionEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}
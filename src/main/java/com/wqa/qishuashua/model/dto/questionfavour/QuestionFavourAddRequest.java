package com.wqa.qishuashua.model.dto.questionfavour;

import java.io.Serializable;
import lombok.Data;

/**
 * 题目收藏 / 取消收藏请求
 */
@Data
public class QuestionFavourAddRequest implements Serializable {

    /**
     * 题目 id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}
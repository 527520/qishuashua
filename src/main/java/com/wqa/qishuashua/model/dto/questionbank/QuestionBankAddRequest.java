package com.wqa.qishuashua.model.dto.questionbank;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建题库请求
 *
 */
@Data
public class QuestionBankAddRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片
     */
    private String picture;

    /**
     * 可见状态：0-所有人可见, 1-仅本人可见
     */
    private Integer visibleStatus;

    /**
     * 优先级
     */
    private Integer priority;

    private static final long serialVersionUID = 1L;
}
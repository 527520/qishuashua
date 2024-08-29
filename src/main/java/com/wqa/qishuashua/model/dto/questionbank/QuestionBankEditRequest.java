package com.wqa.qishuashua.model.dto.questionbank;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑题库请求
 *
 */
@Data
public class QuestionBankEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

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
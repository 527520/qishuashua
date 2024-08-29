package com.wqa.qishuashua.model.dto.question;

import com.wqa.qishuashua.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询题目请求
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * id
     */
    private Long notId;

    /**
     * 题库id
     */
    private Long questionBankId;

    /**
     * 搜索词
     */
    private String searchText;

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
     * 创建用户 id
     */
    private Long userId;

    /**
     * 推荐答案
     */
    private String answer;

    /**
     * 状态：0-待审核, 1-通过, 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 可见状态：0-所有人可见, 1-仅本人可见
     */
    private Integer visibleStatus;

    /**
     * 仅会员可见（1 表示仅会员可见）
     */
    private Integer needVip;

    private static final long serialVersionUID = 1L;
}
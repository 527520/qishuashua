package com.wqa.qishuashua.model.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wqa.qishuashua.model.entity.Question;
import com.wqa.qishuashua.model.entity.QuestionBank;
import com.wqa.qishuashua.model.enums.ReviewStatusEnum;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 题库视图
 *
 */
@Data
public class QuestionBankVO implements Serializable {

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
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 状态：0-待审核, 1-通过, 2-拒绝
     */
    private String reviewStatus;

    /**
     * 可见状态：0-所有人可见, 1-仅本人可见
     */
    private Integer visibleStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 浏览量
     */
    private Integer viewNum;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 题库里的题目（分页）
     */
    private Page<Question> questionPage;

    /**
     * 封装类转对象
     *
     * @param questionBankVO
     * @return
     */
    public static QuestionBank voToObj(QuestionBankVO questionBankVO) {
        if (questionBankVO == null) {
            return null;
        }
        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(questionBankVO, questionBank);
        return questionBank;
    }

    /**
     * 对象转封装类
     *
     * @param questionBank
     * @return
     */
    public static QuestionBankVO objToVo(QuestionBank questionBank) {
        if (questionBank == null) {
            return null;
        }
        QuestionBankVO questionBankVO = new QuestionBankVO();
        BeanUtils.copyProperties(questionBank, questionBankVO);
        questionBankVO.setReviewStatus(Objects.requireNonNull(ReviewStatusEnum.getEnumByValue(questionBank.getReviewStatus())).getText());
        return questionBankVO;
    }
}

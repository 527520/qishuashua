package com.wqa.qishuashua.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wqa.qishuashua.model.dto.questionbankquestion.QuestionBankQuestionQueryRequest;
import com.wqa.qishuashua.model.entity.QuestionBankQuestion;
import com.wqa.qishuashua.model.entity.User;
import com.wqa.qishuashua.model.vo.QuestionBankQuestionVO;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题库题目关联服务
 *
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add 对创建的数据进行校验
     */
    void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest);
    
    /**
     * 获取题库题目关联封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request);

    /**
     * 分页获取题库题目关联封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request);

    /**
     * 批量添加题目到题库
     * @param questionIdList
     * @param questionBankId
     * @param loginUser
     */
    void batchAddQuestionToBank(List<Long> questionIdList, Long questionBankId, User loginUser);

    /**
     * 批量添加题目到题库（事务）
     * @param questionBankQuestionList
     */
    @Transactional(rollbackFor = Exception.class)
    void batchAddQuestionToBankInner(List<QuestionBankQuestion> questionBankQuestionList);

    /**
     * 批量从题库移除题目
     * @param questionIdList
     * @param questionBankId
     */
    void batchRemoveQuestionFromBank(List<Long> questionIdList, Long questionBankId, User loginUser);

    /**
     * 批量从题库移除题目，根据题目ID列表
     * @param questionIdList
     */
    void removeBatchByQuestionIdList(List<Long> questionIdList);
}

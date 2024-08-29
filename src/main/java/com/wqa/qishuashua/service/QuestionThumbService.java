package com.wqa.qishuashua.service;

import com.wqa.qishuashua.model.entity.QuestionThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wqa.qishuashua.model.entity.User;

/**
 * 题目点赞服务
 */
public interface QuestionThumbService extends IService<QuestionThumb> {

    /**
     * 点赞
     *
     * @param questionId
     * @param loginUser
     * @return
     */
    int doQuestionThumb(long questionId, User loginUser);

    /**
     * 题目点赞（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doQuestionThumbInner(long userId, long postId);

    /**
     * 查看点赞信息
     */
    QuestionThumb getQuestionThumbInfo(long questionId, long userId);
}

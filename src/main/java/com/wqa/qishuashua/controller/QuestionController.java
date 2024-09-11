package com.wqa.qishuashua.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wqa.qishuashua.annotation.AuthCheck;
import com.wqa.qishuashua.common.BaseResponse;
import com.wqa.qishuashua.common.DeleteRequest;
import com.wqa.qishuashua.common.ErrorCode;
import com.wqa.qishuashua.common.ResultUtils;
import com.wqa.qishuashua.constant.UserConstant;
import com.wqa.qishuashua.exception.BusinessException;
import com.wqa.qishuashua.exception.ThrowUtils;
import com.wqa.qishuashua.model.dto.question.QuestionAddRequest;
import com.wqa.qishuashua.model.dto.question.QuestionEditRequest;
import com.wqa.qishuashua.model.dto.question.QuestionQueryRequest;
import com.wqa.qishuashua.model.dto.question.QuestionUpdateRequest;
import com.wqa.qishuashua.model.entity.*;
import com.wqa.qishuashua.model.enums.ReviewStatusEnum;
import com.wqa.qishuashua.model.vo.QuestionVO;
import com.wqa.qishuashua.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    @Resource
    private QuestionFavourService questionFavourService;

    @Resource
    private QuestionThumbService questionThumbService;

    // region 增删改查

    /**
     * 创建题目
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @Transactional
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionAddRequest == null || CollUtil.isEmpty(questionAddRequest.getQuestionBankIds()),
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!userService.isAdminOrVip(loginUser), ErrorCode.NO_AUTH_ERROR, "暂无权限");
        // 将实体类和 DTO 进行转换
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        // 数据校验
        questionService.validQuestion(question, true);
        // 填充默认值
        question.setUserId(loginUser.getId());
        // 如果创建用户为admin则无需审核
        if (userService.isAdmin(loginUser)) {
            question.setReviewStatus(ReviewStatusEnum.PASS.getValue());
        }
        if (questionAddRequest.getVisibleStatus() == 1) {
            question.setReviewStatus(ReviewStatusEnum.WITHOUT.getValue());
        }
        // 写入数据库
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionId = question.getId();
        // 写入关联表
        Set<Long> questionBankIds = questionAddRequest.getQuestionBankIds();
        List<QuestionBankQuestion> questionBankQuestionList = new ArrayList<>(questionBankIds.size());
        for (Long questionBankId : questionBankIds) {
            QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
            questionBankQuestion.setQuestionId(newQuestionId);
            questionBankQuestion.setQuestionBankId(questionBankId);
            questionBankQuestion.setUserId(loginUser.getId());
            questionBankQuestionList.add(questionBankQuestion);
        }
        result = questionBankQuestionService.saveBatch(questionBankQuestionList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除题目
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!userService.isAdminOrVip(loginUser), ErrorCode.NO_AUTH_ERROR, "暂无权限");
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        // 数据校验
        questionService.validQuestion(question, false);
        // 判断是否存在
        long id = questionUpdateRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        // 判断题目是否为会员可见，判断用户是否为会员
        User loginUser = userService.getLoginUser(request);
        if (question.getNeedVip() == 1) {
            ThrowUtils.throwIf(!UserConstant.VIP_ROLE.equals(loginUser.getVipCode()), ErrorCode.NO_AUTH_ERROR);
        }
        QuestionVO questionVO = questionService.getQuestionVO(question, request);
        // 查看是否点赞、是否收藏
        Long userId = loginUser.getId();
        QuestionThumb questionThumb = questionThumbService.getQuestionThumbInfo(id, userId);
        QuestionFavour questionFavour = questionFavourService.getQuestionFavourInfo(id, userId);
        questionVO.setThumb(ObjectUtils.isNotEmpty(questionThumb));
        questionVO.setFavour(ObjectUtils.isNotEmpty(questionFavour));
        // 增加浏览数，添加浏览记录
        Question newQuestion = new Question();
        newQuestion.setId(question.getId());
        newQuestion.setViewNum(question.getViewNum() + 1);
        questionService.updateById(newQuestion);
        // 获取封装类
        return ResultUtils.success(questionVO);
    }

    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest, true, null);
        return ResultUtils.success(questionPage);
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        Long myId = null;
        try {
            myId = userService.getLoginUser(request).getId();
        } catch (Exception e) {
            log.info("未登录用户");
        }
        Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest,
                false, myId);
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前登录用户创建的题目列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest)
                        .eq(ObjectUtils.isNotEmpty(questionQueryRequest.getVisibleStatus()),
                                "visibleStatus", questionQueryRequest.getVisibleStatus()));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 编辑题目（给用户使用）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        // 数据校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = questionEditRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 如果可见性由本人可见改为所有人可见，需要修改审核状态为待审核
        if (oldQuestion.getVisibleStatus() == 1 && questionEditRequest.getVisibleStatus() == 0) {
            question.setReviewStatus(ReviewStatusEnum.REVIEWING.getValue());
        } else if (oldQuestion.getVisibleStatus() == 0 && questionEditRequest.getVisibleStatus() == 1) {
            question.setReviewStatus(ReviewStatusEnum.WITHOUT.getValue());
        }
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}

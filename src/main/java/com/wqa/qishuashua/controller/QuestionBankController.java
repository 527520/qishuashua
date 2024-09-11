package com.wqa.qishuashua.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wqa.qishuashua.annotation.AuthCheck;
import com.wqa.qishuashua.common.BaseResponse;
import com.wqa.qishuashua.common.DeleteRequest;
import com.wqa.qishuashua.common.ErrorCode;
import com.wqa.qishuashua.common.ResultUtils;
import com.wqa.qishuashua.constant.UserConstant;
import com.wqa.qishuashua.exception.BusinessException;
import com.wqa.qishuashua.exception.ThrowUtils;
import com.wqa.qishuashua.model.dto.question.QuestionQueryRequest;
import com.wqa.qishuashua.model.dto.questionbank.QuestionBankAddRequest;
import com.wqa.qishuashua.model.dto.questionbank.QuestionBankEditRequest;
import com.wqa.qishuashua.model.dto.questionbank.QuestionBankQueryRequest;
import com.wqa.qishuashua.model.dto.questionbank.QuestionBankUpdateRequest;
import com.wqa.qishuashua.model.entity.Question;
import com.wqa.qishuashua.model.entity.QuestionBank;
import com.wqa.qishuashua.model.entity.User;
import com.wqa.qishuashua.model.enums.ReviewStatusEnum;
import com.wqa.qishuashua.model.vo.QuestionBankVO;
import com.wqa.qishuashua.model.vo.QuestionVO;
import com.wqa.qishuashua.service.QuestionBankService;
import com.wqa.qishuashua.service.QuestionService;
import com.wqa.qishuashua.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;

/**
 * 题库接口
 */
@RestController
@RequestMapping("/questionBank")
@Slf4j
public class QuestionBankController {

    @Resource
    private QuestionBankService questionBankService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    // region 增删改查

    /**
     * 创建题库
     *
     * @param questionBankAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestionBank(@RequestBody QuestionBankAddRequest questionBankAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验权限 -> 只能管理员或会员
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!userService.isAdminOrVip(loginUser), ErrorCode.NO_AUTH_ERROR, "暂无权限");
        // 将实体类和 DTO 进行转换
        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(questionBankAddRequest, questionBank);
        // 数据校验
        questionBankService.validQuestionBank(questionBank, true);
        // 填充默认值
        questionBank.setUserId(loginUser.getId());
        // 如果创建用户为admin则无需审核
        if (userService.isAdmin(loginUser)) {
            questionBank.setReviewStatus(ReviewStatusEnum.PASS.getValue());
        }
        if (questionBankAddRequest.getVisibleStatus() == 1) {
            questionBank.setReviewStatus(ReviewStatusEnum.WITHOUT.getValue());
        }
        // 写入数据库
        boolean result = questionBankService.save(questionBank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionBankId = questionBank.getId();
        return ResultUtils.success(newQuestionBankId);
    }

    /**
     * 删除题库
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionBank(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!userService.isAdminOrVip(loginUser), ErrorCode.NO_AUTH_ERROR, "暂无权限");
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionBank oldQuestionBank = questionBankService.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestionBank.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionBankService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题库（仅管理员可用）
     *
     * @param questionBankUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestionBank(@RequestBody QuestionBankUpdateRequest questionBankUpdateRequest) {
        if (questionBankUpdateRequest == null || questionBankUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(questionBankUpdateRequest, questionBank);
        // 数据校验
        questionBankService.validQuestionBank(questionBank, false);
        // 判断是否存在
        long id = questionBankUpdateRequest.getId();
        QuestionBank oldQuestionBank = questionBankService.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionBankService.updateById(questionBank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题库（封装类）
     *
     * @param questionBankQueryRequest
     * @return
     */
    @PostMapping("/get/vo")
    public BaseResponse<QuestionBankVO> getQuestionBankVOById(@RequestBody QuestionBankQueryRequest questionBankQueryRequest,
                                                              HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = questionBankQueryRequest.getId();
        Boolean needQueryQuestionList = questionBankQueryRequest.getNeedQueryQuestionList();
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        QuestionBank questionBank = questionBankService.getById(id);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // todo 增加浏览数，添加浏览记录
        QuestionBank newQuestionBank = new QuestionBank();
        newQuestionBank.setId(questionBank.getId());
        newQuestionBank.setViewNum(questionBank.getViewNum() + 1);
        questionBankService.updateById(questionBank);
        QuestionBankVO questionBankVO = questionBankService.getQuestionBankVO(questionBank, request);
        // 获取题目列表
        if (ObjectUtils.isNotEmpty(needQueryQuestionList) && needQueryQuestionList) {
            Long myId = null;
            try {
                myId = userService.getLoginUser(request).getId();
            } catch (Exception e) {
                log.info("未登录用户");
            }
            QuestionQueryRequest questionQueryRequest = new QuestionQueryRequest();
            questionQueryRequest.setPageSize(questionBankQueryRequest.getPageSize());
            questionQueryRequest.setCurrent(questionBankQueryRequest.getCurrent());
            questionQueryRequest.setQuestionBankIds(Collections.singletonList(id));
            Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest, false, myId);
            Page<QuestionVO> questionVOPage = questionService.getQuestionVOPage(questionPage, request);
            questionBankVO.setQuestionPage(questionVOPage);
        }
        // 获取封装类
        return ResultUtils.success(questionBankVO);
    }

    /**
     * 分页获取题库列表（仅管理员可用）
     *
     * @param questionBankQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBank>> listQuestionBankByPage(@RequestBody QuestionBankQueryRequest questionBankQueryRequest) {
        long current = questionBankQueryRequest.getCurrent();
        long size = questionBankQueryRequest.getPageSize();
        // 查询数据库
        QueryWrapper<QuestionBank> queryWrapper = questionBankService.getQueryWrapper(questionBankQueryRequest);
        Integer visibleStatus = questionBankQueryRequest.getVisibleStatus();
        if (visibleStatus != null && Arrays.asList(0, 1).contains(visibleStatus)) {
            queryWrapper.eq("visibleStatus", visibleStatus);
        }
        Page<QuestionBank> questionBankPage = questionBankService.page(new Page<>(current, size),
                queryWrapper);
        return ResultUtils.success(questionBankPage);
    }

    /**
     * 分页获取题库列表（封装类）
     *
     * @param questionBankQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionBankVO>> listQuestionBankVOByPage(@RequestBody QuestionBankQueryRequest questionBankQueryRequest,
                                                                       HttpServletRequest request) {
        long current = questionBankQueryRequest.getCurrent();
        long size = questionBankQueryRequest.getPageSize();
        Integer visibleStatus = questionBankQueryRequest.getVisibleStatus();
        // 限制爬虫
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        QueryWrapper<QuestionBank> queryWrapper = questionBankService.getQueryWrapper(questionBankQueryRequest);
        if (visibleStatus == null || visibleStatus == 0) {
            queryWrapper.eq("visibleStatus", 0);
        } else if (visibleStatus == 1) {
            queryWrapper.eq("visibleStatus", 1)
                    .eq("userId", userService.getLoginUser(request).getId());
        }
        Page<QuestionBank> questionBankPage = questionBankService.page(new Page<>(current, size),
                queryWrapper);
        // 获取封装类
        return ResultUtils.success(questionBankService.getQuestionBankVOPage(questionBankPage, request));
    }

    /**
     * 分页获取当前登录用户创建的题库列表
     *
     * @param questionBankQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionBankVO>> listMyQuestionBankVOByPage(@RequestBody QuestionBankQueryRequest questionBankQueryRequest,
                                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        questionBankQueryRequest.setUserId(loginUser.getId());
        long current = questionBankQueryRequest.getCurrent();
        long size = questionBankQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBank> questionBankPage = questionBankService.page(new Page<>(current, size),
                questionBankService.getQueryWrapper(questionBankQueryRequest)
                        .eq(ObjectUtils.isNotEmpty(questionBankQueryRequest.getVisibleStatus()),
                                "visibleStatus", questionBankQueryRequest.getVisibleStatus()));
        // 获取封装类
        return ResultUtils.success(questionBankService.getQuestionBankVOPage(questionBankPage, request));
    }

    /**
     * 编辑题库（给用户使用）
     *
     * @param questionBankEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestionBank(@RequestBody QuestionBankEditRequest questionBankEditRequest, HttpServletRequest request) {
        if (questionBankEditRequest == null || questionBankEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        QuestionBank questionBank = new QuestionBank();
        BeanUtils.copyProperties(questionBankEditRequest, questionBank);
        // 数据校验
        questionBankService.validQuestionBank(questionBank, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = questionBankEditRequest.getId();
        QuestionBank oldQuestionBank = questionBankService.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestionBank.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 如果可见性由本人可见改为所有人可见，需要修改审核状态为待审核
        if (oldQuestionBank.getVisibleStatus() == 1 && questionBankEditRequest.getVisibleStatus() == 0) {
            questionBank.setReviewStatus(ReviewStatusEnum.REVIEWING.getValue());
        } else if (oldQuestionBank.getVisibleStatus() == 0 && questionBankEditRequest.getVisibleStatus() == 1) {
            questionBank.setReviewStatus(ReviewStatusEnum.WITHOUT.getValue());
        }
        // 操作数据库
        boolean result = questionBankService.updateById(questionBank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}

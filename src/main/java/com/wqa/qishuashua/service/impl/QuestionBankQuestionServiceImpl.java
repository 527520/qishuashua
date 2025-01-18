package com.wqa.qishuashua.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wqa.qishuashua.common.ErrorCode;
import com.wqa.qishuashua.constant.CommonConstant;
import com.wqa.qishuashua.constant.UserConstant;
import com.wqa.qishuashua.exception.BusinessException;
import com.wqa.qishuashua.exception.ThrowUtils;
import com.wqa.qishuashua.mapper.QuestionBankQuestionMapper;
import com.wqa.qishuashua.model.dto.questionbankquestion.QuestionBankQuestionQueryRequest;
import com.wqa.qishuashua.model.entity.Question;
import com.wqa.qishuashua.model.entity.QuestionBank;
import com.wqa.qishuashua.model.entity.QuestionBankQuestion;
import com.wqa.qishuashua.model.entity.User;
import com.wqa.qishuashua.model.vo.QuestionBankQuestionVO;
import com.wqa.qishuashua.model.vo.UserVO;
import com.wqa.qishuashua.service.QuestionBankQuestionService;
import com.wqa.qishuashua.service.QuestionBankService;
import com.wqa.qishuashua.service.QuestionService;
import com.wqa.qishuashua.service.UserService;
import com.wqa.qishuashua.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 题库题目关联服务实现
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private QuestionService questionService;

    @Resource
    private QuestionBankService questionBankService;

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add                  对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long questionId = questionBankQuestion.getQuestionId();
        Long questionBankId = questionBankQuestion.getQuestionBankId();

        // 创建数据时，参数不能为空
//        if (add) {
//            // 校验规则
//            ThrowUtils.throwIf(questionId == null, ErrorCode.PARAMS_ERROR);
//            ThrowUtils.throwIf(questionBankId == null, ErrorCode.PARAMS_ERROR);
//        }
        // 题目和题库必须存在
        if (questionId != null) {
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        if (questionBankId != null) {
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionBankQuestionQueryRequest.getId();
        Long notId = questionBankQuestionQueryRequest.getNotId();
        String sortField = questionBankQuestionQueryRequest.getSortField();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();
        Long userId = questionBankQuestionQueryRequest.getUserId();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();

        // 需要的查询条件
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题库题目关联封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);

        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankQuestionVO.setUser(userVO);

        // endregion

        return questionBankQuestionVO;
    }

    /**
     * 分页获取题库题目关联封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList
                .stream()
                .map(QuestionBankQuestionVO::objToVo)
                .collect(Collectors.toList());
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        questionBankQuestionVOList.forEach(questionBankQuestionVO -> {
            Long userId = questionBankQuestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionBankQuestionVO.setUser(userService.getUserVO(user));
        });
        // endregion

        questionBankQuestionVOPage.setRecords(questionBankQuestionVOList);
        return questionBankQuestionVOPage;
    }

    @Override
    public void batchAddQuestionToBank(List<Long> questionIdList, Long questionBankId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.PARAMS_ERROR, "题目ID列表不能为空");
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0L,
                ErrorCode.PARAMS_ERROR, "题库ID非法");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户信息非法");
        // 检查题目ID是否存在
        LambdaQueryWrapper<Question> questionLambdaQueryWrapper = Wrappers.lambdaQuery(Question.class)
                .select(Question::getId, Question::getUserId)
                .in(Question::getId, questionIdList);
        List<Question> questionList = questionService.list(questionLambdaQueryWrapper);
        ThrowUtils.throwIf(CollUtil.isEmpty(questionList), ErrorCode.PARAMS_ERROR, "合法的题目ID列表为空");
        // 检查题库是否存在
        QuestionBank questionBank = questionBankService.getById(questionBankId);
        // 判断题目、题库是否为本人或管理员，是否有权限
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            questionList.forEach(question -> {
                Long userId = question.getUserId();
                ThrowUtils.throwIf(!userId.equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
            });
            ThrowUtils.throwIf(!questionBank.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        }
        ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        List<Long> validQuestionIdList = questionList.stream().map(Question::getId).collect(Collectors.toList());
        // 检查哪些题目还不存在于题库中，避免重复插入
        LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                    .in(QuestionBankQuestion::getQuestionId, validQuestionIdList);
        List<QuestionBankQuestion> existQuestionList = this.list(lambdaQueryWrapper);
        Set<Long> existQuestionIdSet = existQuestionList.stream()
                .map(QuestionBankQuestion::getQuestionId)
                .collect(Collectors.toSet());
        validQuestionIdList = validQuestionIdList.stream()
                .filter(questionId -> !existQuestionIdSet.contains(questionId))
                .collect(Collectors.toList());
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIdList), ErrorCode.PARAMS_ERROR, "所有题目均已存在于题库中");
        // 执行插入

        // 自定义线程池（IO密集型线程池）
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                20,           // 核心线程数
                50,                      // 最大线程数
                60L,                     // 空闲线程存活时间
                TimeUnit.SECONDS,        // 存活时间单位
                new LinkedBlockingQueue<>(10000), // 阻塞队列容量
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略，由调用线程处理任务
        );
        // 保存所有批次任务
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 分批处理，避免长事务
        int batchSize = 1000;
        int totalQuestionListSize = validQuestionIdList.size();
        for (int i = 0; i < totalQuestionListSize; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalQuestionListSize);
            List<Long> subQuestionIdList = validQuestionIdList.subList(i, endIndex);
            List<QuestionBankQuestion> subQuestionBankQuestionList = subQuestionIdList.stream().map(questionId -> {
                QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
                questionBankQuestion.setQuestionBankId(questionBankId);
                questionBankQuestion.setQuestionId(questionId);
                questionBankQuestion.setUserId(loginUser.getId());
                return questionBankQuestion;
            }).collect(Collectors.toList());
            // 使用事务处理每批数据
            // 获取代理，使用代理调用 @Transactional 修饰的方法，事物才会生效
            QuestionBankQuestionService questionBankQuestionService = (QuestionBankQuestionServiceImpl) AopContext.currentProxy();
            // 异步处理没批数据，将任务添加到异步任务列表
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                    questionBankQuestionService.batchAddQuestionToBankInner(subQuestionBankQuestionList), threadPoolExecutor);
            futures.add(future);
        }
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // 关闭线程池
        threadPoolExecutor.shutdown();
    }

    /**
     * 批量添加题目到题库（事务）
     * @param questionBankQuestionList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionToBankInner(List<QuestionBankQuestion> questionBankQuestionList) {
        try {
            boolean result = this.saveBatch(questionBankQuestionList);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        } catch (DataIntegrityViolationException e) {
            log.error("数据库唯一键冲突或违反其他完整性约束, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已存在于该题库，无法重复添加");
        } catch (DataAccessException e) {
            log.error("数据库连接问题、事务问题等导致操作失败, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        } catch (Exception e) {
            // 捕获其他异常，做通用处理
            log.error("添加题目到题库时发生未知错误，错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveQuestionFromBank(List<Long> questionIdList, Long questionBankId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.PARAMS_ERROR, "题目ID列表不能为空");
        ThrowUtils.throwIf(questionBankId == null || questionBankId <= 0L,
                ErrorCode.PARAMS_ERROR, "题库ID非法");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户信息非法");
        // 判断题目、题库是否为本人或管理员，是否有权限
        LambdaQueryWrapper<Question> questionLambdaQueryWrapper = Wrappers.lambdaQuery(Question.class)
                .select(Question::getId, Question::getUserId)
                .in(Question::getId, questionIdList);
        if (!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            List<Question> questionList = questionService.list(questionLambdaQueryWrapper);
            // 检查题目ID是否存在
            ThrowUtils.throwIf(CollUtil.isEmpty(questionList), ErrorCode.PARAMS_ERROR, "合法的题目ID列表为空");
            // 检查题库是否存在
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
            questionList.forEach(question -> {
                Long userId = question.getUserId();
                ThrowUtils.throwIf(!userId.equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
            });
            ThrowUtils.throwIf(!questionBank.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        }
        // 执行移除
        // 排除不存在于题库中的题目
        LambdaQueryWrapper<QuestionBankQuestion> existQuestionLambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .in(QuestionBankQuestion::getQuestionId, questionIdList);
        List<QuestionBankQuestion> existQuestionList = this.list(existQuestionLambdaQueryWrapper);
        if (CollUtil.isNotEmpty(existQuestionList)) {
            List<Long> existQuestionBankQuestionIdList = existQuestionList.stream()
                    .map(QuestionBankQuestion::getId)
                    .collect(Collectors.toList());
            boolean result = this.removeByIds(existQuestionBankQuestionIdList);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "移除题目题库关联失败");
        }
    }

    @Override
    public void removeBatchByQuestionIdList(List<Long> questionIdList) {
        // 根据题目id移除题目题库关联
        LambdaQueryWrapper<QuestionBankQuestion> queryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .in(QuestionBankQuestion::getQuestionId, questionIdList);
        boolean result = this.remove(queryWrapper);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除题目题库关联失败");
    }
}

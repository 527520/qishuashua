-- 创建库
create database if not exists qishuashua;

USE qishuashua;

-- 用户表
CREATE TABLE
    IF NOT EXISTS USER
(
    id            BIGINT auto_increment COMMENT 'id' PRIMARY KEY,
    userAccount   VARCHAR(256)                           NOT NULL COMMENT '账号',
    userPassword  VARCHAR(512)                           NOT NULL COMMENT '密码',
    phoneNumber   char(11)                               NOT NULL COMMENT '手机号',
    unionId       VARCHAR(256)                           NULL COMMENT '微信开放平台id',
    mpOpenId      VARCHAR(256)                           NULL COMMENT '公众号openId',
    userName      VARCHAR(256)                           NULL COMMENT '用户昵称',
    userAvatar    VARCHAR(1024)                          NULL COMMENT '用户头像',
    userProfile   VARCHAR(512)                           NULL COMMENT '用户简介',
    userRole      VARCHAR(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/admin/ban/vip',
    editTime      datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    createTime    datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime    datetime     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete      TINYINT      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    vipExpireTime datetime                               NULL COMMENT '会员过期时间',
    vipCode       VARCHAR(128)                           NULL COMMENT '会员兑换码',
    vipNumber     BIGINT                                 NULL COMMENT '会员编号',
    INDEX idx_unionId (unionId)
) COMMENT '用户' COLLATE = utf8mb4_unicode_ci;

-- 题库表
CREATE TABLE
    IF NOT EXISTS question_bank
(
    id            BIGINT auto_increment COMMENT 'id' PRIMARY KEY,
    title         VARCHAR(256)                       NULL COMMENT '标题',
    description   text                               NULL COMMENT '描述',
    picture       VARCHAR(2048)                      NULL COMMENT '图片',
    userId        BIGINT                             NOT NULL COMMENT '创建用户 id',
    editTime      datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    createTime    datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime    datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    reviewStatus  INT      DEFAULT 0                 NOT NULL COMMENT '状态：0-待审核, 1-通过, 2-拒绝',
    visibleStatus TINYINT  DEFAULT 0                 NOT NULL COMMENT '可见状态：0-所有人可见, 1-仅本人可见',
    reviewMessage VARCHAR(512)                       NULL COMMENT '审核信息',
    reviewerId    BIGINT                             NULL COMMENT '审核人 id',
    reviewTime    datetime                           NULL COMMENT '审核时间',
    priority      INT      DEFAULT 0                 NOT NULL COMMENT '优先级',
    viewNum       INT      DEFAULT 0                 NOT NULL COMMENT '浏览量',
    isDelete      TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_title (title)
) COMMENT '题库' COLLATE = utf8mb4_unicode_ci;

-- 题目表
CREATE TABLE
    IF NOT EXISTS question
(
    id            BIGINT auto_increment COMMENT 'id' PRIMARY KEY,
    title         VARCHAR(256)                       NULL COMMENT '标题',
    content       text                               NULL COMMENT '内容',
    tags          VARCHAR(1024)                      NULL COMMENT '标签列表（json 数组）',
    answer        text                               NULL COMMENT '推荐答案',
    userId        BIGINT                             NOT NULL COMMENT '创建用户 id',
    editTime      datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间',
    createTime    datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime    datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    reviewStatus  INT      DEFAULT 0                 NOT NULL COMMENT '状态：0-待审核, 1-通过, 2-拒绝',
    visibleStatus TINYINT  DEFAULT 0                 NOT NULL COMMENT '可见状态：0-所有人可见, 1-仅本人可见',
    reviewMessage VARCHAR(512)                       NULL COMMENT '审核信息',
    reviewerId    BIGINT                             NULL COMMENT '审核人 id',
    reviewTime    datetime                           NULL COMMENT '审核时间',
    priority      INT      DEFAULT 0                 NOT NULL COMMENT '优先级',
    viewNum       INT      DEFAULT 0                 NOT NULL COMMENT '浏览量',
    thumbNum      INT      DEFAULT 0                 NOT NULL COMMENT '点赞数',
    favourNum     INT      DEFAULT 0                 NOT NULL COMMENT '收藏数',
    source        VARCHAR(512)                       NULL COMMENT '题目来源',
    needVip       TINYINT  DEFAULT 0                 NOT NULL COMMENT '仅会员可见（1 表示仅会员可见）',
    isDelete      TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_title (title),
    INDEX idx_userId (userId)
) COMMENT '题目' COLLATE = utf8mb4_unicode_ci;

-- 题库题目表（硬删除）
CREATE TABLE
    IF NOT EXISTS question_bank_question
(
    id             BIGINT auto_increment COMMENT 'id' PRIMARY KEY,
    questionBankId BIGINT                             NOT NULL COMMENT '题库 id',
    questionId     BIGINT                             NOT NULL COMMENT '题目 id',
    userId         BIGINT                             NOT NULL COMMENT '创建用户 id',
    createTime     datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime     datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE (questionBankId, questionId)
) COMMENT '题库题目' COLLATE = utf8mb4_unicode_ci;

-- 题目点赞表（硬删除）
create table if not exists question_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '题目点赞';

-- 题目收藏表（硬删除）
create table if not exists question_favour
(
    id         bigint auto_increment comment 'id' primary key,
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '题目收藏';

-- 题目评论表
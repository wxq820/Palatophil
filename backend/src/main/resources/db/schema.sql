-- ============================================================
-- 无定珍 全栈项目 数据库 Schema (MySQL 8.0)
-- 版本: 1.0
-- 字符集: utf8mb4
-- ============================================================

DROP DATABASE IF EXISTS `palatophil`;
CREATE DATABASE `palatophil` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `palatophil`;

-- ============================================================
-- 1. sys_user 用户主表
-- 支持两种登录方式：微信小程序(openid/unionid) + PC 后台(username/password)
-- ============================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `openid`         VARCHAR(64)  DEFAULT NULL COMMENT '微信 OpenID',
  `unionid`        VARCHAR(64)  DEFAULT NULL COMMENT '微信 UnionID',
  `username`       VARCHAR(64)  DEFAULT NULL COMMENT 'PC 后台账号',
  `password`       VARCHAR(128) DEFAULT NULL COMMENT 'BCrypt 密码',
  `nickname`       VARCHAR(64)  NOT NULL DEFAULT '未命名用户' COMMENT '昵称',
  `avatar`         VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  `role`           VARCHAR(32)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用 1=正常',
  `last_login_at`  DATETIME     DEFAULT NULL COMMENT '最近登录时间',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删 1=已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_unionid` (`unionid`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户主表';

-- ============================================================
-- 2. standard_ingredient 标准食材库
-- 存储系统预置/用户自定义食材，固定按克(g)存储
-- ============================================================
DROP TABLE IF EXISTS `standard_ingredient`;
CREATE TABLE `standard_ingredient` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`          VARCHAR(64)  NOT NULL COMMENT '标准名称',
  `aliases`       JSON         DEFAULT NULL COMMENT '别名数组 JSON',
  `category`      VARCHAR(32)  NOT NULL COMMENT '分类: VEGETABLE/MEAT/AQUATIC/GRAIN/SEASONING/DAIRY/FRUIT/OTHER',
  `unit_density`  DECIMAL(8,3) DEFAULT 1.000 COMMENT '密度(kg/L)，液态食材使用',
  `is_system`     TINYINT      NOT NULL DEFAULT 0 COMMENT '系统食材: 0=用户 1=系统',
  `audit_status`  TINYINT      NOT NULL DEFAULT 1 COMMENT '审核: 0=待审核 1=已通过 2=已拒绝',
  `owner_id`      BIGINT       DEFAULT NULL COMMENT '创建者ID',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_owner` (`name`, `owner_id`),
  KEY `idx_category` (`category`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标准食材库';

-- ============================================================
-- 3. recipe 标准食谱（母版/派生）
-- 派生关系通过 source_recipe_id 记录，软删除不级联
-- ============================================================
DROP TABLE IF EXISTS `recipe`;
CREATE TABLE `recipe` (
  `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
  `name`               VARCHAR(128) NOT NULL COMMENT '食谱名称',
  `description`        TEXT         DEFAULT NULL COMMENT '描述',
  `cover_image`        VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `source_recipe_id`   BIGINT       DEFAULT NULL COMMENT '源食谱ID（派生）',
  `owner_id`           BIGINT       DEFAULT NULL COMMENT '创建者',
  `visibility`         VARCHAR(16)  NOT NULL DEFAULT 'PRIVATE' COMMENT '可见性: PRIVATE/PUBLIC',
  `audit_status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '审核: 0=待审 1=通过 2=拒绝',
  `version`            INT          NOT NULL DEFAULT 1 COMMENT '版本号',
  `servings`           INT          NOT NULL DEFAULT 1 COMMENT '基准份数',
  `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`            TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_owner` (`owner_id`),
  KEY `idx_source` (`source_recipe_id`),
  KEY `idx_audit` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标准食谱';

-- ============================================================
-- 4. recipe_block 食材块
-- 用于"多食材 / 单食材"两种模式
-- ============================================================
DROP TABLE IF EXISTS `recipe_block`;
CREATE TABLE `recipe_block` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `recipe_id`   BIGINT       NOT NULL COMMENT '所属食谱',
  `name`        VARCHAR(64)  NOT NULL COMMENT '块名称（如：汤底、酱料）',
  `block_type`  VARCHAR(16)  NOT NULL DEFAULT 'SINGLE' COMMENT '类型: SINGLE/MULTI',
  `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_recipe` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食谱食材块';

-- ============================================================
-- 5. recipe_ingredient 食谱食材明细
-- amount_g 统一以克存储，1 位小数
-- ============================================================
DROP TABLE IF EXISTS `recipe_ingredient`;
CREATE TABLE `recipe_ingredient` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `recipe_id`      BIGINT       NOT NULL COMMENT '食谱ID',
  `block_id`       BIGINT       DEFAULT NULL COMMENT '食材块ID',
  `ingredient_id`  BIGINT       NOT NULL COMMENT '食材ID',
  `amount_g`       DECIMAL(10,2) NOT NULL COMMENT '克数，1位小数',
  `is_anchor`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否锚点食材',
  `note`           VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_recipe` (`recipe_id`),
  KEY `idx_block` (`block_id`),
  KEY `idx_ingredient` (`ingredient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食谱食材明细';

-- ============================================================
-- 6. coop_session 协同会话
-- 单会话上限 20 人（业务层校验）
-- ============================================================
DROP TABLE IF EXISTS `coop_session`;
CREATE TABLE `coop_session` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `owner_id`       BIGINT       NOT NULL COMMENT '发起人',
  `recipe_id`      BIGINT       DEFAULT NULL COMMENT '关联食谱',
  `title`          VARCHAR(128) DEFAULT NULL COMMENT '会话标题',
  `share_token`    VARCHAR(64)  NOT NULL COMMENT '分享 Token',
  `status`         VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/CLOSED/EXPIRED',
  `max_people`     INT          NOT NULL DEFAULT 20 COMMENT '上限人数',
  `expires_at`     DATETIME     NOT NULL COMMENT '过期时间',
  `closed_at`      DATETIME     DEFAULT NULL COMMENT '关闭时间',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_share_token` (`share_token`),
  KEY `idx_owner` (`owner_id`),
  KEY `idx_recipe` (`recipe_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协同会话';

-- ============================================================
-- 7. coop_session_member 会话成员
-- 协作者可无需账号，仅凭昵称入会
-- ============================================================
DROP TABLE IF EXISTS `coop_session_member`;
CREATE TABLE `coop_session_member` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `session_id`  BIGINT       NOT NULL COMMENT '会话ID',
  `user_id`     BIGINT       DEFAULT NULL COMMENT '注册用户（可为空）',
  `nickname`    VARCHAR(64)  NOT NULL COMMENT '昵称',
  `avatar`      VARCHAR(255) DEFAULT NULL,
  `role`        VARCHAR(16)  NOT NULL DEFAULT 'MEMBER' COMMENT 'OWNER/MEMBER',
  `joined_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `left_at`     DATETIME     DEFAULT NULL,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话成员';

-- ============================================================
-- 8. prep_order 采购清单
-- 状态：OPEN -> CONFIRMED -> ARCHIVED
-- ============================================================
DROP TABLE IF EXISTS `prep_order`;
CREATE TABLE `prep_order` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `session_id`     BIGINT       DEFAULT NULL COMMENT '来源会话',
  `owner_id`       BIGINT       NOT NULL COMMENT '创建者',
  `title`          VARCHAR(128) DEFAULT NULL COMMENT '清单标题',
  `status`         VARCHAR(16)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/CONFIRMED/ARCHIVED',
  `total_cost`     DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '预算总额',
  `actual_cost`    DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际总额',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`),
  KEY `idx_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购清单';

-- ============================================================
-- 9. prep_order_item 采购清单明细
-- ============================================================
DROP TABLE IF EXISTS `prep_order_item`;
CREATE TABLE `prep_order_item` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `order_id`       BIGINT       NOT NULL COMMENT '清单ID',
  `ingredient_id`  BIGINT       NOT NULL COMMENT '食材ID',
  `amount_g`       DECIMAL(10,2) NOT NULL COMMENT '克数',
  `unit_price`     DECIMAL(12,2) DEFAULT 0.00 COMMENT '单价(元/500g)',
  `subtotal`       DECIMAL(12,2) DEFAULT 0.00 COMMENT '小计',
  `purchased`      TINYINT      NOT NULL DEFAULT 0 COMMENT '已购: 0=否 1=是',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_ingredient` (`ingredient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购清单明细';

-- ============================================================
-- 10. recipe_tag 食谱标签
-- 预置 + 用户自定义
-- ============================================================
DROP TABLE IF EXISTS `recipe_tag`;
CREATE TABLE `recipe_tag` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(32)  NOT NULL COMMENT '标签名',
  `is_system`   TINYINT      NOT NULL DEFAULT 0 COMMENT '系统标签',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食谱标签';

-- ============================================================
-- 11. recipe_tag_rel 食谱-标签关联
-- ============================================================
DROP TABLE IF EXISTS `recipe_tag_rel`;
CREATE TABLE `recipe_tag_rel` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT,
  `recipe_id`  BIGINT NOT NULL,
  `tag_id`     BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recipe_tag` (`recipe_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食谱标签关联';

-- ============================================================
-- 12. follow_relation 关注关系
-- ============================================================
DROP TABLE IF EXISTS `follow_relation`;
CREATE TABLE `follow_relation` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT,
  `follower_id`  BIGINT NOT NULL COMMENT '关注者',
  `followee_id`  BIGINT NOT NULL COMMENT '被关注者',
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`      TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pair` (`follower_id`, `followee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注关系';

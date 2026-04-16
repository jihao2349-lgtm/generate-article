-- =============================================
-- 热点分析模块数据库表结构
-- 模块：generate-article
-- 功能：用于存储热点分析记录和历史关键词数据
-- 创建时间：2026-03-12
-- 作者：jihao
-- =============================================

-- 热点分析记录表：存储用户热点分析请求的完整流程和结果
CREATE TABLE IF NOT EXISTS `hotspot_analysis_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `request_no` VARCHAR(64) NOT NULL COMMENT '请求流水号',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户 ID',
    `request_type` VARCHAR(32) NOT NULL COMMENT '请求类型：KEYWORD、RAW_LIST',
    `keyword` VARCHAR(1000) DEFAULT NULL COMMENT '热点关键词，多个关键词使用英文逗号分隔',
    `request_hash` CHAR(64) NOT NULL COMMENT '请求摘要哈希，用于缓存命中和幂等判断',
    `source_platforms` JSON DEFAULT NULL COMMENT '抓取平台列表 JSON',
    `input_payload` JSON NOT NULL COMMENT '原始请求报文 JSON',
    `status` VARCHAR(32) NOT NULL DEFAULT 'INIT' COMMENT '状态：INIT、SUCCESS、FAILED',
    `result_json` JSON DEFAULT NULL COMMENT '热点分析结果 JSON',
    `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '失败原因',
    `started_time` DATETIME DEFAULT NULL COMMENT '开始处理时间',
    `finished_time` DATETIME DEFAULT NULL COMMENT '处理完成时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_request_no` (`request_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_request_hash` (`request_hash`),
    KEY `idx_request_type_status` (`request_type`, `status`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='热点分析记录表';

-- 热点关键词历史表：记录用户使用过的热点关键词及使用频次
CREATE TABLE IF NOT EXISTS `hotspot_keyword_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `keyword` VARCHAR(255) NOT NULL COMMENT '关键词',
    `used_count` INT NOT NULL DEFAULT 1 COMMENT '使用次数',
    `last_used_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近使用时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id_keyword` (`user_id`, `keyword`),
    KEY `idx_user_id_last_used_time` (`user_id`, `last_used_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='热点关键词历史表';

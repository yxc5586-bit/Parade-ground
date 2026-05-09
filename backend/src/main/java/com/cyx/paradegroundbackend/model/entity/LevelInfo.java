package com.cyx.paradegroundbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 关卡信息实体 — 存储AI生成或管理员创建的关卡题目数据，核心是题面(requirement)与选项(options)。
 * priority 控制精选/置顶：0=普通, 99=提升, 999=精选, 9999=置顶
 */
@Data
@TableName("level_info")
public class LevelInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String levelId;           // 业务编号，格式 level_yyyyMMddHHmmssSSS_xxxx

    private String levelName;         // 关卡名称

    private String difficulty;        // 难度：basic / intermediate / advanced / expert / platform

    private String salaryRange;       // 对应薪资档位：10000-15000 / ... / 50000+

    private String tags;              // 标签JSON数组，如 ["系统设计","高并发"]

    private String requirement;       // 产品需求文档JSON（背景、目标、角色、流程等）

    private String options;           // 候选方案选项JSON数组，含正确项+干扰项

    private String correctOptionIds;  // 正确答案ID列表JSON，如 ["A","C","F"]

    private String analysisDirection; // AI生成的解题分析方向文本

    private Integer priority;         // 精选优先级：0=普通, 99=提升, 999=精选, 9999=置顶

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDelete;
}

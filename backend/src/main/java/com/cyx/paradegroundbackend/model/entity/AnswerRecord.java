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
 * 作答记录实体 — 存储用户每次提交答案后的判题结果、得分与薪资变化，是战绩系统的基础数据。
 */
@Data
@TableName("answer_record")
public class AnswerRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;              // 作答用户ID

    private String levelId;           // 对应关卡业务编号

    private String selectedOptionIds; // 用户选择的方案ID列表JSON

    private String correctOptionIds;  // 正确答案ID列表JSON（冗余存储，用于回溯）

    private Integer clientSpendSeconds; // 用户答题耗时（秒）

    private Integer score;            // AI评审判分 0-100

    private Integer salaryChange;     // 薪资变化值（可为负数）

    private Integer updatedSalary;    // 结算后月薪

    private String resultReport;      // AI评审报告JSON（评价标题/正文/职位建议等）

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDelete;
}

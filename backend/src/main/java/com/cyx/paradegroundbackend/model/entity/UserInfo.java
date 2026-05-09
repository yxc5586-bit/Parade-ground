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
 * 用户信息实体 — 存储用户账号、薪资与角色，是整个练兵场系统的玩家主体。
 */
@Data
@TableName("user_info")
public class UserInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userAccount;       // 登录账号

    private String userPassword;      // 加密后的密码（MD5+盐）

    private String userName;          // 展示昵称

    private Integer currentSalary;    // 当前月薪（战力的货币化体现）

    private String userAvatar;        // 头像：0-3 为默认色值，否则为自定义URL

    private String userRole;          // 角色：user / admin

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDelete;
}

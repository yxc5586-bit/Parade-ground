package com.cyx.paradegroundbackend.model.vo;

import com.cyx.paradegroundbackend.model.entity.UserInfo;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/** 登录用户VO — 返回给前端的安全用户信息，不含密码，头像默认按userId%4分配四种默认色 */
@Data
public class LoginUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String userAccount;

    private String userName;

    private Integer currentSalary;

    private String userAvatar;

    private String userRole;

    private LocalDateTime createTime;

    public static LoginUserVO fromEntity(UserInfo userInfo) {
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(userInfo, loginUserVO);
        if (loginUserVO.getUserAvatar() == null || loginUserVO.getUserAvatar().isEmpty()) {
            loginUserVO.setUserAvatar(String.valueOf(userInfo.getId() % 4));
        }
        return loginUserVO;
    }
}

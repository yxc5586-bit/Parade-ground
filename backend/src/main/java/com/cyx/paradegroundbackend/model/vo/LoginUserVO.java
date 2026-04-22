package com.cyx.paradegroundbackend.model.vo;

import com.cyx.paradegroundbackend.model.entity.UserInfo;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class LoginUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String userAccount;

    private String userName;

    private Integer currentSalary;

    private LocalDateTime createTime;

    public static LoginUserVO fromEntity(UserInfo userInfo) {
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(userInfo, loginUserVO);
        return loginUserVO;
    }
}

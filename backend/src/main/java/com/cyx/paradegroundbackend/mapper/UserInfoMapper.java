package com.cyx.paradegroundbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyx.paradegroundbackend.model.entity.UserInfo;
import org.apache.ibatis.annotations.Param;

public interface UserInfoMapper extends BaseMapper<UserInfo> {

    UserInfo selectByUserAccount(@Param("userAccount") String userAccount);

    int updateSalaryIfCurrent(@Param("userId") Long userId,
                              @Param("expectedSalary") Integer expectedSalary,
                              @Param("updatedSalary") Integer updatedSalary);
}

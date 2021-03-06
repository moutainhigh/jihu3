package com.yqbing.servicebing.repository.database.dao;

import com.yqbing.servicebing.repository.database.abstracts.UserTokenExample;
import com.yqbing.servicebing.repository.database.bean.UserToken;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface UserTokenMapper {
    int countByExample(UserTokenExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UserToken record);

    int insertSelective(UserToken record);

    List<UserToken> selectByExample(UserTokenExample example);

    UserToken selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserToken record);

    int updateByPrimaryKey(UserToken record);

	UserToken queryUserByToken(@Param("appId")Integer appId,@Param("token")String token);

	UserToken queryPlatformUserByToken(@Param("platformId")Integer platformId, @Param("token")String token);

	UserToken queryUserIdByAppId(@Param("userId")Long userId, @Param("appId")Integer appId);

	UserToken queryByToken(@Param("token")String token);
}
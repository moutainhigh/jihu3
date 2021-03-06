package com.yqbing.servicebing.repository.database.dao;

import com.yqbing.servicebing.repository.database.abstracts.StoreHouseAbs;
import com.yqbing.servicebing.repository.database.bean.StoreHouse;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
@Repository
public interface StoreHouseMapper {
    int countByExample(StoreHouseExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(StoreHouse record);

    int insertSelective(StoreHouse record);

    List<StoreHouse> selectByExample(StoreHouseExample example);

    StoreHouseAbs selectByPrimaryKey(Integer id);
    
    StoreHouseAbs selectByPack(String appPack);

    int updateByPrimaryKeySelective(StoreHouse record);

    int updateByPrimaryKey(StoreHouse record);

	StoreHouse getByAppName(String appName);
	
	StoreHouse getByAppPack(String appPack);

	List<StoreHouse> queryByType(@Param("startIndex")Integer startIndex, @Param("pageSize")Integer pageSize,
			@Param("type1")Integer type1);

	List<StoreHouse> selectByLikeAppName(@Param("appName")String appName);
    
	/**
	 * 
	
	 * @Title: getByAppNameList
	
	 * @Description: TODO
	
	 * @param appName
	 * @return
	
	 * @return: StoreHouse
	 */
	List<StoreHouse> getByAppNameList(String appName);

//	List<StoreHouse> searchAppName(String appName);
}
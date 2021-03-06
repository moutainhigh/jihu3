/*package com.yqbing.servicebing.webapp.task;

import javax.annotation.Resource;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import com.yqbing.servicebing.repository.database.dao.StoreHouseMapper;
import com.yqbing.servicebing.repository.redis.RStoreHouseData;
import com.yqbing.servicebing.service.WebmagicgameService;


@Controller
@Lazy(false)
public class WebmagicrGame {

	@Resource(name = "webmagicgameService")
	private WebmagicgameService webmagicgameService = null;
	@Resource(name = "storeHouseMapper")
	private StoreHouseMapper storeHouseMapper= null;
	
	@Resource(name = "rStoreHouseData")
	private RStoreHouseData rStoreHouseData = null;
	
	
	@Scheduled(cron = "0 0 3 * * ?")
	public void downgomeUrl()throws Exception{
		webmagicgameService.downgomeUrl(storeHouseMapper,rStoreHouseData);
	} 
	
}
*/
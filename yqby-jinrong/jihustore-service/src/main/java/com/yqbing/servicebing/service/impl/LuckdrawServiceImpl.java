package com.yqbing.servicebing.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yqbing.servicebing.common.ErrorCodeEnum;
import com.yqbing.servicebing.repository.database.bean.GoodsOrder;
import com.yqbing.servicebing.repository.database.bean.TicketGoodsLuckdrawExt;
import com.yqbing.servicebing.repository.database.bean.TicketGoodsWithBLOBs;
import com.yqbing.servicebing.repository.database.dao.GoodsOrderMapper;
import com.yqbing.servicebing.repository.database.dao.TicketGoodsLuckdrawExtMapper;
import com.yqbing.servicebing.repository.database.dao.TicketGoodsMapper;
import com.yqbing.servicebing.repository.jihu.bean.LuckdrawRecord;
import com.yqbing.servicebing.repository.jihu.bean.TUserInfoBean;
import com.yqbing.servicebing.repository.jihu.bean.UserLuckdrawExt;
import com.yqbing.servicebing.repository.jihu.bean.UserLuckdrawSign;
import com.yqbing.servicebing.repository.jihu.dao.LuckdrawRecordExample;
import com.yqbing.servicebing.repository.jihu.dao.LuckdrawRecordMapper;
import com.yqbing.servicebing.repository.jihu.dao.TUserInfoBeanMapper;
import com.yqbing.servicebing.repository.jihu.dao.UserLuckdrawExtExample;
import com.yqbing.servicebing.repository.jihu.dao.UserLuckdrawExtMapper;
import com.yqbing.servicebing.repository.jihu.dao.UserLuckdrawSignMapper;
import com.yqbing.servicebing.repository.jihuc.bean.SysDict;
import com.yqbing.servicebing.repository.redis.GoodsOrderLog;
import com.yqbing.servicebing.service.LuckdrawService;
import com.yqbing.servicebing.utils.DateUtil;
import com.yqbing.servicebing.utils.HttpUtil;
import com.yqbing.servicebing.utils.NotifyUtil;
import com.yqbing.servicebing.webapp.request.AddressReq;
import com.yqbing.servicebing.webapp.request.Days;
import com.yqbing.servicebing.webapp.request.Discount;
import com.yqbing.servicebing.webapp.response.AwardRes;
import com.yqbing.servicebing.webapp.response.Goods;
import com.yqbing.servicebing.webapp.response.HomePageRes;
@Service("luckdrawService")
public class LuckdrawServiceImpl<V> implements LuckdrawService{

	private static final Logger LOGGER = LoggerFactory.getLogger(LuckdrawServiceImpl.class);
	@Resource
	private UserLuckdrawExtMapper userLuckdrawExtMapper = null;
	@Resource
	private TicketGoodsMapper ticketGoodsMapper = null;
	@Resource
	private LuckdrawRecordMapper luckdrawRecordMapper = null;
	@Resource
	private TUserInfoBeanMapper tUserInfoBeanMapper = null;
	@Resource
	private GoodsOrderMapper goodsOrderMapper = null;
	@Resource
	private TicketGoodsLuckdrawExtMapper ticketGoodsLuckdrawExtMapper = null;
	@Resource
	private GoodsOrderLog rgoodsOrderLog = null;
	@Resource
	private UserLuckdrawSignMapper userLuckdrawSignMapper = null;
	@Value("${ali_https}")
	private String ali_https = null;
	@Resource
	private TUserInfoBeanMapper userInfoBeanMapper = null;
	
	
	@SuppressWarnings("all")
	@Override
	public String signed(TUserInfoBean user) {
		
		//这个用户今天有没有签到.没有可以签到,
		
		UserLuckdrawExt userext = null;
		Map<String,Object> map = null;
		try {
			UserLuckdrawExtExample example = new UserLuckdrawExtExample();
			example.createCriteria().andUserIdEqualTo(user.getUserId()).andSignInTimeBetween(DateUtil.gettwelve(),DateUtil.getzero());
			List<UserLuckdrawExt> list = userLuckdrawExtMapper.selectByExample(example);//今天是否签到
			if(list != null && list.size() > 0){
				
				return  NotifyUtil.error(ErrorCodeEnum.SIGNED);//今日已经签到
			}
			userext = userLuckdrawExtMapper.selectByUserId(user.getUserId());
			Boolean s = false;
			if(userext == null){
				s = true;
				userext = new UserLuckdrawExt();
				userext.setSignInFrequency(+1);
				userext.setUserId(user.getUserId());
				userext.setCreateTime(new Date());
			}else{
				
				userext.setSignInFrequency(userext.getSignInFrequency()+1);
			}
			userext.setSignInTime(new Date());
			userext.setUpdateTime(new Date());
	        if(s){
				
	        	userLuckdrawExtMapper.insertSelective(userext);
			}else{
				
				userLuckdrawExtMapper.updateByPrimaryKeySelective(userext);
			}
	        
	        
	        
	        UserLuckdrawSign luckdrawSign = new UserLuckdrawSign();
	        luckdrawSign.setUserId(user.getUserId());
	        luckdrawSign.setSignInTime(new Date());
	        luckdrawSign.setSignDay(DateUtil.format(new Date()));
	        userLuckdrawSignMapper.insert(luckdrawSign);
	        map = new HashMap<String, Object>();
	        map.put("number", userLuckdrawSignMapper.queryBykeepDay(user.getUserId()));//连续签到天数
	        map.put("ral", userext.getSignInFrequency()*10);//当前积分
	        
		} catch (Exception e) {
			
			
			e.printStackTrace();
			
			return  NotifyUtil.error(ErrorCodeEnum.OPERATERROE);
		}
		
		
		return NotifyUtil.success(map);
	}

	
	//首页 (全部商品  剩余次数 是否签到  抽奖记录 我的中奖)
	@Override
	public String homePage(TUserInfoBean validToken) {
		
		
	   HomePageRes pageRes = new HomePageRes();
	   
	   List<Goods> goods = new ArrayList<Goods>();
	   List<TicketGoodsLuckdrawExt> traw = ticketGoodsLuckdrawExtMapper.selectandluckdraw("luckdraw");
	   traw.forEach(tg -> {
		   
	   Goods good = new Goods();
	   good.setId(tg.getGoodsId());
	   good.setName(tg.getGoodsName());
	   good.setIcon(tg.getGoodsicon());
	   good.setNumber(tg.getNumber());
	
	   goods.add(good);
	   } );
	   pageRes.setGoods(goods);
	 
	 //pageRes.setAward(award(validToken));
	   pageRes.setDays(get7days());
	   UserLuckdrawExt selectByUserId = userLuckdrawExtMapper.selectByUserId(validToken.getUserId());
	   if(selectByUserId == null){
		   pageRes.setRal(0);//当前积分
	   }else{
		   pageRes.setRal(selectByUserId.getSignInFrequency()*10);//当前积分
	   }
		UserLuckdrawExtExample example = new UserLuckdrawExtExample();
		example.createCriteria().andUserIdEqualTo(validToken.getUserId()).andSignInTimeBetween(DateUtil.gettwelve(),DateUtil.getzero());
		List<UserLuckdrawExt> list1 = userLuckdrawExtMapper.selectByExample(example);//今天是否签到
		if(list1 != null && list1.size() > 0){
			
			pageRes.setSign(1);//已经签到
			
		}else{
			pageRes.setSign(0);//未签到
		}
		
		 pageRes.setSignDay(userLuckdrawSignMapper.queryBykeepDay(validToken.getUserId()));//连续签到天数
		 pageRes.setDiscount(getDis());
		
	   return NotifyUtil.success(pageRes);
	}


	
	public List<AwardRes> award(TUserInfoBean validToken,Integer page,Integer size) {
		
		//查看30天抽奖记录
		   try {
			 //  List<AwardRes> goods = new ArrayList<AwardRes>();
			   LuckdrawRecordExample recordExample = new LuckdrawRecordExample();
			   recordExample.createCriteria().andStatusEqualTo("0");
			   if(page != null){
				   
				   recordExample.setOrderByClause(" create_time desc limit "+page*size+","+size);
			   }else{
				   
				   recordExample.setOrderByClause(" create_time desc ");
			   }
			  // List<LuckdrawRecord> list2 = luckdrawRecordMapper.selectByExample(recordExample);
			   if(page == null){
				   page = 0;
			   }
			   if(size == null){
				   size = 1000;
			   }
			   List<AwardRes> goods = luckdrawRecordMapper.queryAllData(page*size,size);
			   
		/*	   list2.forEach(tg -> {
			    AwardRes award = new AwardRes();
			    award.setId(tg.getId());
			    TUserInfoBean infoBean = userInfoBeanMapper.selectByPrimaryKey(tg.getUserId());
			    if(infoBean != null){
			    	award.setUserName(infoBean.getUserName());//用户名称
			    	award.setUserIcon(infoBean.getUserPic());//用户头像
			    }
			    award.setUserPhone(tg.getPhone());//用户手机号
			    TicketGoodsWithBLOBs goodsWithBLOBs = ticketGoodsMapper.selectByPrimaryKey(tg.getGoodsId());
			    if(goodsWithBLOBs != null){
			    	award.setGoodsIcon(goodsWithBLOBs.getIcon());//商品头像
			    }
			    award.setGoodsName(tg.getGoodsName());
			    award.setTime(tg.getCreateTime());
			    goods.add(award);
			   });*/
			   return goods;
			//success = NotifyUtil.success(goods);
		} catch (Exception e) {
			e.printStackTrace();
			NotifyUtil.error(ErrorCodeEnum.OPERATERROE);
		}
		  
		
		return null;
	}


	
	@SuppressWarnings("unused")
	@Override
	public String action(TUserInfoBean validToken,Long id) {//id == 次数
     
		//是否有抽奖次数//后期再改
		Integer num = 0;
		Integer ral = 0;
		List<Discount> dis = getDis();
		List<Discount> dict = dis.stream().filter(d -> d.id.equals(id)).collect(Collectors.toList());
	
			if(dict != null && dict.size() > 0){
				ral = dict.get(0).getRal();
				num = dict.get(0).getNumber();
			}else{
				return  NotifyUtil.error(ErrorCodeEnum.OPERATERROE);
			}
	
		   UserLuckdrawExt userext = userLuckdrawExtMapper.selectByUserId(validToken.getUserId());
		   if(userext != null && userext.getSignInFrequency()*10 < ral ){
			   
			return  NotifyUtil.error(ErrorCodeEnum.NUMBERSIGN);
		   }
		   
		   //所有商品
 		   List<Goods> goods = new ArrayList<Goods>();
		   List<TicketGoodsLuckdrawExt> traw = ticketGoodsLuckdrawExtMapper.selectandluckdraw("luckdraw");
		   traw.forEach(tg -> {
			   
		   Goods good = new Goods();
		   good.setId(tg.getGoodsId());
		   good.setName(tg.getGoodsName());
		   good.setIcon(tg.getGoodsicon());
		   LuckdrawRecordExample recordExample = new LuckdrawRecordExample();
		   recordExample.createCriteria().andStatusEqualTo("0").andGoodsIdEqualTo(tg.getGoodsId());
		   List<LuckdrawRecord> list = luckdrawRecordMapper.selectByExample(recordExample);
		   if(list.size() >= tg.getNumber()){//商品如果已经用完了,就把中奖率设置为0
			  
			   good.setRatio(0.00);
		   }else{
			   good.setRatio(tg.getRatio());
			   
		   }
		   good.setNumber(tg.getNumber());
		
		   goods.add(good);
		   } );
		   List<Goods> drawgoods =  null;
		   try {
		   //抽奖逻辑
		   drawgoods =    new ArrayList<Goods>();
		   for (int i = 0; i < num; i++) {
			   
			   int index = getPrizeIndex(goods);
			   Goods goods2 = goods.get(index);
			   LOGGER.info("抽中奖品：" +goods2.getName());
			   //成功之后 减少一次抽奖次数
				   if(goods2 == null){
					   return NotifyUtil.error(ErrorCodeEnum.AWARDNOTGOODS);
				   }
				   //加入中奖记录表
				   if(!goods2.getNumber().equals(100000)){//谢谢惠顾,不存在中奖记录里
					   LuckdrawRecord record = new LuckdrawRecord();
					   record.setCreateTime(new Date());
					   record.setGoodsId(goods2.getId());
					   record.setGoodsName(goods2.getName());
					   record.setPhone(validToken.getUserPhone());
					   record.setStatus("0");
					   record.setType("0");
					   record.setUserId(validToken.getUserId());
					   luckdrawRecordMapper.insertSelective(record);
				   }
				   drawgoods.add(goods2);
		   }
		   userext.setSignInFrequency(userext.getSignInFrequency()-ral/10);
		   userext.setUpdateTime(new Date());
		   userLuckdrawExtMapper.updateByPrimaryKeySelective(userext);
		   } catch (Exception e) {
			e.printStackTrace();
			return NotifyUtil.error(ErrorCodeEnum.OPERATERROE);
		   }
		   Map<String,Object> map = new HashMap<String, Object>();
		   map.put("number", userLuckdrawSignMapper.queryBykeepDay(validToken.getUserId()));//连续签到天数
	       map.put("ral", userext.getSignInFrequency()*10);//当前积分
		   map.put("list", drawgoods);
		   return NotifyUtil.success(map);
	}

   
	
	//抽奖机呼
	@Override
	public String awardrec(TUserInfoBean validToken,Integer page,Integer size) {
		String success = null;
		List<AwardRes> award = award(validToken,page,size);
		
		success = NotifyUtil.success(award);
		
		return success;
	}

	
	
	
	
	//中奖概率
	public  void getAward(){
		
		
		
		
	}
	/**
     * 根据Math.random()产生一个double型的随机数，判断每个奖品出现的概率
     * @param prizes
     * @return random：奖品列表prizes中的序列（prizes中的第random个就是抽中的奖品）
     *    0<randomNumber<=0.1   表示抽中一等奖
     *   0.1<randomNumber<=0.3 表示抽中二等奖
     *   0.3<randomNumber<=0.6 表示抽中三等奖
     *   0.6<randomNumber<=1.0 表示抽中四等奖
     *  如果计划中奖率是100%的话，那么10个奖品只能抽奖10次，所以还要根据实际情况设置每种奖品数量和权重。
     *
　       *　如果需要设置中奖率不为100%，可以添加一个“伪奖品”，并为其设置权重，那么抽到这个“伪奖品”的概率就是不中奖的概率。
     *
　　   *  如果在抽奖过程中某类奖品抽完了，可以做个判断，如果此奖品的剩余数量为0，则重新抽取奖品，直到抽到其他奖品位置
     */
    public  int getPrizeIndex(List<Goods> prizes) {
      //  DecimalFormat df = new DecimalFormat("######0.00");  
        int random = -1;
        try{
            //计算总权重
            double sumWeight = 0;
            for(Goods p: prizes){
                sumWeight += p.getRatio();
            }

            //产生随机数
            double randomNumber;
            randomNumber = Math.random();//大于0 ,小于一的数字
            //根据随机数在所有奖品分布的区域并确定所抽奖品
            double d1 = 0;
            double d2 = 0;          
            for(int i=0;i<prizes.size();i++){
                d2 += prizes.get(i).getRatio()/sumWeight;
                if(i==0){
                    d1 = 0;
                }else{
                    d1 += prizes.get(i-1).getRatio()/sumWeight;
                }
                if(randomNumber >= d1 && randomNumber <= d2){
                    random = i;
                    break;
                }
            }
        }catch(Exception e){
        	e.printStackTrace();
        	LOGGER.error("生成抽奖随机数出错，出错原因：" +e.getLocalizedMessage());
        }
        return random;
    }


	@Override
	public String myaward(TUserInfoBean validToken,String type, String page, String size) {
		
		
		   List<AwardRes> mygoods = new ArrayList<AwardRes>();
		  /* LuckdrawRecordExample recordExample = new LuckdrawRecordExample();
		   recordExample.createCriteria().andUserIdEqualTo(validToken.getUserId()).andStatusEqualTo("0").andTypeEqualTo(type);
		   
		   List<LuckdrawRecord> list2 = luckdrawRecordMapper.selectByExample(recordExample);*/
		   Integer pa = 0;
		   Integer si = 100;
		   if(page != null && size != null){
			   pa = Integer.valueOf(page);
			   si = Integer.valueOf(size);
		   }
		   mygoods = luckdrawRecordMapper.queryUserIdAndType(validToken.getUserId(),type,pa*si,si);
		   /*list2.forEach(tg -> {
			
			AwardRes award = new AwardRes();
		    award.setId(tg.getId());
		    TUserInfoBean userInfoBean = tUserInfoBeanMapper.selectByPrimaryKey(tg.getUserId());
		   
		    award.setUserName(userInfoBean.getUserName());//用户名称
		    award.setUserIcon(userInfoBean.getUserPic());//用户头像
		    award.setUserPhone(tg.getPhone());//用户手机号
		    TicketGoodsWithBLOBs goodsWithBLOBs = ticketGoodsMapper.selectByPrimaryKey(tg.getGoodsId());
		    if(goodsWithBLOBs != null){
		    	award.setGoodsIcon(goodsWithBLOBs.getIcon());//商品头像
		    }
		    award.setGoodsName(tg.getGoodsName());
		    award.setTime(tg.getCreateTime());
		    mygoods.add(award);
				   
		   } );*/
		  Map<String, Object> map = new HashMap<String, Object>();
		  map.put("data", mygoods);
		  return NotifyUtil.success(map);
	}


	
	@Override
	public String exchange(AddressReq req,TUserInfoBean user) {
		
		LuckdrawRecord luck = luckdrawRecordMapper.queryUserAndId(req.getId(),user.getUserId());
		if(luck == null){
			return NotifyUtil.error(ErrorCodeEnum.AWARDNOTGOODS);
		}
		if(luck.getType().equals("1")){//已经兑换过
			return NotifyUtil.error(ErrorCodeEnum.AWARDEXCHANGE);
		}
		
		GoodsOrder goodsOrder = new GoodsOrder();
		goodsOrder.setAddress(req.getAddress());
		goodsOrder.setAddressName(req.getName());
		goodsOrder.setAddressPhone(req.getPhone());
		if(StringUtils.isNotBlank(req.getProvinceCode())){
			
			goodsOrder.setProvinceId(Integer.valueOf(req.getProvinceCode()));
		}
		goodsOrder.setProvinceName(req.getProvince());
		if(StringUtils.isNotBlank(req.getCityCode())){
			goodsOrder.setCityId(Integer.valueOf(req.getCityCode()));
			
		}
		goodsOrder.setCityName(req.getCity());
		if(StringUtils.isNotBlank(req.getAreaCode())){
			
			goodsOrder.setAreaId(Integer.valueOf(req.getAreaCode()));
		
		}
		goodsOrder.setAreaName(req.getArea());
		goodsOrder.setGoodsId(luck.getGoodsId());
		//生成订单号
		String increment = rgoodsOrderLog.increment(new Date().getTime());
		
		goodsOrder.setOrderId(increment);
		goodsOrder.setUserId(Long.valueOf(user.getUserId()));
		goodsOrder.setUserName(user.getUserName());
		goodsOrder.setCreateTime(new Date());
		goodsOrder.setOrderTime(new Date());
		goodsOrder.setPayTime(new Date());
		goodsOrder.setChannel("union");
		TicketGoodsWithBLOBs goodsWithBLOBs = ticketGoodsMapper.selectByPrimaryKey(luck.getGoodsId());
		if(goodsWithBLOBs != null){
			
			goodsOrder.setGoodsImg(goodsWithBLOBs.getIcon());
		}
		goodsOrder.setGoodsName(luck.getGoodsName());
		try {
			short s = 1;
			goodsOrder.setStatus(s);
//			map = new HashMap<String, Object>();
//			map.put("orderId", increment);
			goodsOrderMapper.insertSelective(goodsOrder);
			luck.setType("1");
			luck.setUpdateTime(new Date());
			luckdrawRecordMapper.updateByPrimaryKeySelective(luck);
		} catch (Exception e) {
			e.printStackTrace();
			return NotifyUtil.error(ErrorCodeEnum.OPERATERROE);
		}
		return NotifyUtil.success();
	}
	
	//返回七天日期
	public  List<Days> get7days(){
		//获取当前天
		//根据天获取周
		List<Days> days = new ArrayList<Days>();
		for (int i = 0; i < 7; i++) {
			
			Days day = new Days();
			day.setDay(DateUtil.getFetureDateHH(i));
			day.setWeek(DateUtil.getWeekOfDate(DateUtil.getFetureDate(i)));
			days.add(day);
		
		}
		
		return days;
	}
	public List<Discount> getDis(){
		List<Discount> list = new ArrayList<Discount>();
		try {
			String sendPost = HttpUtil.sendPost(ali_https+"/lachine/ral", "");
			
			 JSONObject JSONObject = new JSONObject(sendPost);
			 int code = JSONObject.getInt("code");
			 if(code != 0){
				 
				 return null;
			 }
			Object object = JSONObject.get("body");
			
			List<SysDict> list2= new Gson().fromJson(object.toString(), new TypeToken<List<SysDict>>() {}.getType());
			
			for (SysDict sysDict : list2) {
				Discount discount = new Discount();
				
				discount.setId(sysDict.getId());
				discount.setNumber(Integer.valueOf(sysDict.getBaseValue()));
				discount.setRal(Integer.valueOf(sysDict.getExtraValue()));
				list.add(discount);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return list;
	}
	 /*  
	  * 我的中奖名单
	  * 
	  * ArrayList<AwardRes> mygoods = new ArrayList<AwardRes>();
	   LuckdrawRecordExample recordExample = new LuckdrawRecordExample();
	   recordExample.createCriteria().andUserIdEqualTo(validToken.getUserId()).andStatusEqualTo("0");
	   
	   List<LuckdrawRecord> list2 = luckdrawRecordMapper.selectByExample(recordExample);
	   list2.forEach(tg -> {
		AwardRes award = new AwardRes();
	    award.setId(tg.getId());
	    TUserInfoBean userInfoBean = tUserInfoBeanMapper.selectByPrimaryKey(tg.getUserId());
	   
	    award.setUserName(userInfoBean.getUserName());//用户名称
	    award.setUserIcon(userInfoBean.getUserPic());//用户头像
	    award.setUserPhone(tg.getPhone());//用户手机号
	    TicketGoodsWithBLOBs goodsWithBLOBs = ticketGoodsMapper.selectByPrimaryKey(tg.getGoodsId());
	    if(goodsWithBLOBs != null){
	    	award.setGoodsIcon(goodsWithBLOBs.getIcon());//商品头像
	    }
	    award.setGoodsName(tg.getGoodsName());
	    award.setTime(tg.getCreateTime());
	    mygoods.add(award);
			   
	   } );
	   pageRes.setMyAward(mygoods);*/


	@Override
	public String getdis() {
		// TODO Auto-generated method stub
		return NotifyUtil.success(getDis());
	}
   /*public static void main(String[] args) {
	  Long id = 166L;
	   List<Discount> dis  = new ArrayList<Discount>();
	   
	   Discount discount = new Discount();
	   discount.setId(166L);
	   discount.setNumber(10);
	   dis.add(discount);
	   Discount discount1 = new Discount();
	   discount1.setId(12L);
	   discount1.setNumber(10);
	   dis.add(discount1);
	   List<Discount> dict = dis.stream().filter(d -> d.id == id).collect(Collectors.toList());
		if(dict != null && dict.size() > 0){
			System.out.println(dict.get(0).getNumber());
		}
}	*/
}

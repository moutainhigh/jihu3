package com.yqbing.servicebing.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.yqbing.servicebing.common.ErrorCodeEnum;
import com.yqbing.servicebing.repository.database.abstracts.BanK360InfoBeanExample;
import com.yqbing.servicebing.repository.database.abstracts.BanK360InfoBeanExample.Criteria;
import com.yqbing.servicebing.repository.database.abstracts.Bank360OffBeanExample;
import com.yqbing.servicebing.repository.database.abstracts.StoreUserBeanExample;
import com.yqbing.servicebing.repository.database.abstracts.ZanclickOffBeanExample;
import com.yqbing.servicebing.repository.database.bean.BanK360InfoBean;
import com.yqbing.servicebing.repository.database.bean.Bank360ActivateBean;
import com.yqbing.servicebing.repository.database.bean.Bank360OffBean;
import com.yqbing.servicebing.repository.database.bean.StoreInfoBean;
import com.yqbing.servicebing.repository.database.bean.StoreUserBean;
import com.yqbing.servicebing.repository.database.bean.TUserInfoBean;
import com.yqbing.servicebing.repository.database.bean.ZanclickOffBean;
import com.yqbing.servicebing.repository.database.dao.BanK360InfoBeanMapper;
import com.yqbing.servicebing.repository.database.dao.Bank360ActivateBeanMapper;
import com.yqbing.servicebing.repository.database.dao.Bank360OffBeanMapper;
import com.yqbing.servicebing.repository.database.dao.StoreInfoBeanMapper;
import com.yqbing.servicebing.repository.database.dao.StoreUserBeanMapper;
import com.yqbing.servicebing.repository.database.dao.TUserInfoBeanMapper;
import com.yqbing.servicebing.service.Bank360Service;
import com.yqbing.servicebing.utils.DateUtil;
import com.yqbing.servicebing.utils.NotifyUtil;
import com.yqbing.servicebing.utils.QRCodeUtil;
import com.yqbing.servicebing.utils.QrcodeImageUtils;
import com.yqbing.servicebing.utils.StrUtils;



@Service("bank360Service")
@SuppressWarnings("all")
public class Bank360ServiceImpl implements Bank360Service{
	private static final Logger LOGGER = LoggerFactory.getLogger(Bank360ServiceImpl.class);
	@Autowired
	private StoreInfoBeanMapper storeInfoBeanMapper = null;
	@Autowired
	private StoreUserBeanMapper storeUserBeanMapper = null;
	
	@Autowired
	private BanK360InfoBeanMapper banK360InfoBeanMapper = null;
	@Autowired
	private Bank360OffBeanMapper bank360OffBeanMapper = null;
	
	@Autowired
	private Bank360ActivateBeanMapper bank360ActivateBeanMapper = null;
	@Autowired
	private TUserInfoBeanMapper userInfoBeanMapper = null;
	
	@Override
	public String put360Bank(TUserInfoBean validToken) {
		
		
		
		String success = StringUtils.EMPTY;
		
		long userId = validToken.getUserId();
		
		short p = 1;
		StoreUserBeanExample example2 = new StoreUserBeanExample();
		com.yqbing.servicebing.repository.database.abstracts.StoreUserBeanExample.Criteria criteria2 = example2.createCriteria();
		
		criteria2.andUserIdEqualTo(Integer.valueOf(userId+"")).andUserStateEqualTo(p);
		List<StoreUserBean> list = storeUserBeanMapper.selectByExample(example2);
		
		//ResultModel success = ResultModel.success();
	//	List<StoreInfoBean> list = storeInfoBeanMapper.selectByExample(example);
		if(null == list || list.size()<= 0){
			success = NotifyUtil.error(ErrorCodeEnum.DISACCORD,"店铺不存在");
			return success;
		}
		StoreUserBean suBean = list.get(0);
		
		StoreInfoBean infoBean = storeInfoBeanMapper.selectByPrimaryKey(suBean.getStoreId());
		/*if(!infoBean.getStoreName().equals(storeName)){
			success = NotifyUtil.error(ErrorCodeEnum.NOTSRORENAME,"请输入自己的门店名称");
			return success;
		}*/
		//是否已经申请过
		BanK360InfoBeanExample banK360InfoBeanExample = new BanK360InfoBeanExample();
		Criteria criteria = banK360InfoBeanExample.createCriteria();
		criteria.andUserIdEqualTo(userId);
		List<BanK360InfoBean> bankinfos = banK360InfoBeanMapper.selectByExample(banK360InfoBeanExample);
		if(bankinfos != null &&  bankinfos.size() >  0){
			LOGGER.info("----------------------------已经申请过了,用户信息userinfo:"+new Gson().toJson(validToken));
			success = NotifyUtil.error(ErrorCodeEnum.OPERATERROE,"禁止重复申请");
			return success;
		}
		
		if(infoBean == null){
			success = NotifyUtil.error(ErrorCodeEnum.NOTSRORE,"店铺不存在");
			return success;
		}
		// TODO Auto-generated method stub
		
		BanK360InfoBean banK360 = new BanK360InfoBean();
		banK360.setUserId(userId);
		banK360.setUserName(validToken.getUserName());
		banK360.setUserPhone(validToken.getUserPhone());
		banK360.setStoreId(infoBean.getStoreId());
		banK360.setStoreName(infoBean.getStoreName());
		banK360.setCityId(infoBean.getCityId());
		banK360.setCityName(infoBean.getCityName());
		banK360.setCreateTime(new Date());
		try {
			
			banK360InfoBeanMapper.insertSelective(banK360);
			LOGGER.info("----------------------------申请成功,banK360:"+new Gson().toJson(banK360));
			success =  NotifyUtil.success();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			success = NotifyUtil.error(ErrorCodeEnum.OPERATERROE);
		
		}
		return success;
	}

	@Override
	public String queryBank360Off(long userId) {
		
		// TODO Auto-generated method stub
		//	ResultModel success = ResultModel.success();
			String success = StringUtils.EMPTY;
			Bank360OffBeanExample banK360OffBean = new Bank360OffBeanExample();
		      banK360OffBean.createCriteria();
		     Bank360OffBean bank360OffBean =null;
			List<Bank360OffBean> list = bank360OffBeanMapper.selectByExample(banK360OffBean);
			if(list != null){
				bank360OffBean = list.get(0);
				
			}
			success = NotifyUtil.success(bank360OffBean);
			return success;
	}

	@Override
	public String bank360List(TUserInfoBean validToken, String page,
			String size, String startTime, String endTime) {
		String success = StringUtils.EMPTY;
		String qrCodeUrl =  StringUtils.EMPTY;
		
		/**
		 * .判断是否存在推广链接
		   .如果存在链接 生成二维码链接,并存到七牛上
		   .通过推广链接拿出来这个用户所有的激活量
		 */
	    long userId = validToken.getUserId();
	    Map<String,Object> map = null;
		short p = 1;
		StoreUserBeanExample example2 = new StoreUserBeanExample();
		com.yqbing.servicebing.repository.database.abstracts.StoreUserBeanExample.Criteria criteria2 = example2.createCriteria();
		
		criteria2.andUserIdEqualTo(Integer.valueOf(userId+"")).andUserStateEqualTo(p);
		List<StoreUserBean> list = storeUserBeanMapper.selectByExample(example2);
		if(null == list || list.size()<= 0){
			success = NotifyUtil.error(ErrorCodeEnum.DISACCORD,"店铺不存在");
			return success;
		}
		StoreUserBean suBean = list.get(0);
		BanK360InfoBeanExample banK360InfoBeanExample = new BanK360InfoBeanExample();
		Criteria criteria = banK360InfoBeanExample.createCriteria();
		criteria.andUserIdEqualTo(userId);
		List<BanK360InfoBean> bankinfos = banK360InfoBeanMapper.selectByExample(banK360InfoBeanExample);
		if(null == bankinfos ||  bankinfos.size() <=  0){
			LOGGER.info("----------------------------申请拉新资格,userinfo:"+new Gson().toJson(validToken));
			success = NotifyUtil.error(ErrorCodeEnum.BANK360LAXIN,"申请拉新资格");
			return success;
		}
		if(bankinfos != null){
			BanK360InfoBean banKinfo = bankinfos.get(0);
			String bankUrl = banKinfo.getBankUrl();
			if(StringUtils.isBlank(bankUrl)){//为空:在申请当中
				LOGGER.info("----------------------------正在审核当,banK360:"+new Gson().toJson(banKinfo));
				success = NotifyUtil.error(ErrorCodeEnum.BANK360ING,"正在审核当中请稍后");
				return success;
			}
			
			String qrCode = banKinfo.getQrCode();
			if(StringUtils.isBlank(qrCode)){//为空 生成二维码 存到七牛哪里.七牛返回链接存到数据库里
				try {
					/* qrCodeUrl = QRCodeUtil.geneQRCodeToSting(bankUrl, "qrCode-"
					        + suBean.getStoreId() + "-" + System.currentTimeMillis(),
					103, 103);*/
                    String logoFile = "http://p2a60yqmm.bkt.clouddn.com/icon360bank.png";
                    qrCodeUrl = QrcodeImageUtils.drawLogoQRCode(logoFile, "360bank_qrCode-" + "-" + System.currentTimeMillis(), bankUrl, null);
              
              
				//	 qrCodeUrl = "www.baidu.com";
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				map = new HashMap<String,Object>();
				map.put("qrcode", qrCodeUrl);
				banKinfo.setQrCode(qrCodeUrl);
				banK360InfoBeanMapper.updateByPrimaryKeySelective(banKinfo);
				success = NotifyUtil.success(map);
				
				return success;
			}
			//-----------不是空,显示二维码链接,同时激活数量展示出来
			Date parsestartTime = null;
			Date parseendTime = null;
			int page1 = 0;
			int size1 = 1000;
			HashMap<String, Object> map1 = new HashMap<String, Object>();
			if (!StrUtils.isNullOrBlank(startTime)&& !StrUtils.isNullOrBlank(endTime)) {
				try {
					parsestartTime = DateUtil.parse(startTime);
					parseendTime = DateUtil.parse(endTime);
					if(!StrUtils.isNullOrBlank(page) && !StrUtils.isNullOrBlank(size)){
						page1 = Integer.valueOf(page);
						size1 = Integer.valueOf(size);
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			PageHelper.startPage(page1, size1);
			map1.put("startTime", parsestartTime);
			map1.put("endTime", parseendTime);
			map1.put("bankUrl", bankUrl);
			Short s = suBean.getBack1();
			map = new HashMap<String,Object>();
			int total = 0;
			List<Bank360ActivateBean> bas = bank360ActivateBeanMapper.queryList(map1);
			for (Bank360ActivateBean b3 : bas) {
				total += b3.getActivate()> 3 ? 3 : b3.getActivate();
				b3.setActivate(b3.getActivate()> 3 ? 3 : b3.getActivate());
			}
		//	int total = 0;
			
			
			
			
		
			List<Bank360ActivateBean> newbas = new ArrayList<Bank360ActivateBean>();
			
			List<Bank360ActivateBean> newbas360 = new ArrayList<Bank360ActivateBean>();
			
		//	int total = bank360ActivateBeanMapper.queryToal(map1);
			
	        if(s == 3){//店长 
	        	short d = 2;
	        	List<Bank360ActivateBean> bas1 = null;
				//获取所有店员的Id
				//返回所有的店员
				StoreUserBeanExample example3 = new StoreUserBeanExample();
				com.yqbing.servicebing.repository.database.abstracts.StoreUserBeanExample.Criteria criteria3 = example3.createCriteria();
				
				criteria3.andStoreIdEqualTo(suBean.getStoreId()).andUserStateEqualTo(p).andBack1EqualTo(d);
				List<StoreUserBean> list3 = storeUserBeanMapper.selectByExample(example3);
				if(null != list3 &&  list3.size() >  0){//有店员
					LOGGER.info("----------------------------申请拉新资格,userinfo:"+new Gson().toJson(validToken));
				    for (StoreUserBean s1 : list3) {
						Integer uid = s1.getUserId();
						String bankUrl2 = null;
						BanK360InfoBeanExample banK360InfoBeanExample1 = new BanK360InfoBeanExample();
						Criteria criteria1 = banK360InfoBeanExample1.createCriteria();
						criteria1.andUserIdEqualTo(s1.getUserId().longValue());
						List<BanK360InfoBean> banks = banK360InfoBeanMapper.selectByExample(banK360InfoBeanExample1);
						if(null == banks ||  banks.size()<=   0){
							continue;
						}
						
						map1.put("bankUrl", banks.get(0).getBankUrl());
						map = new HashMap<String,Object>();
					//	int total1 = bank360ActivateBeanMapper.queryToal(map1);
					//	total += total1;
						bas1 = bank360ActivateBeanMapper.queryList(map1);
						if(null != banks &&  banks.size()>  0){
							
							newbas.addAll(bas1);
						}
					  
					}
				
				   
				    
				    
				    
			/*	for (Bank360ActivateBean ba : bas) {
					
					for (Bank360ActivateBean ba1 : newbas) {
						if(DateUtil.format(ba.getRegisTime()).equals(DateUtil.format(ba1.getRegisTime()))){//相等代表同一天
							Bank360ActivateBean activateBean = new Bank360ActivateBean();
							activateBean.setActivate(ba.getActivate()+ba1.getActivate());
							activateBean.setId(ba.getId());
							activateBean.setBankUrl(bankUrl);
							activateBean.setCreateTime(ba.getCreateTime());
							activateBean.setRegisTime(ba.getRegisTime());
						
							newbas360.add(activateBean);
						}
					}
				}*/
				 newbas.addAll(bas);
				 int tot = 0;
				 //int tot1 = 0;
				 Map<String, Bank360ActivateBean> map2 = new HashMap<>();
				 for (Bank360ActivateBean as : newbas) {
					 if (!map2.containsKey(DateUtil.format(as.getRegisTime()))) {
						 Bank360ActivateBean activateBean = new Bank360ActivateBean();
							activateBean.setActivate(as.getActivate() > 3?3:as.getActivate());
							activateBean.setId(as.getId());
							activateBean.setBankUrl(bankUrl);
							activateBean.setCreateTime(as.getCreateTime());
							activateBean.setRegisTime(as.getRegisTime());
							map2.put(DateUtil.format(as.getRegisTime()), activateBean);
							newbas360.add(activateBean);
							//tot1 +=as.getActivate();
					}else{
						Bank360ActivateBean ba = map2.get(DateUtil.format(as.getRegisTime()));
						int tots = as.getActivate()> 3?3:as.getActivate();
						ba.setActivate(tots+ba.getActivate());
						//tot +=ba.getActivate();
					}
				 }
				    
				
				if(newbas360 == null || newbas360.size() <= 0){
					map.put("list", bas);
					map.put("total", total);
				}else{
					for (Bank360ActivateBean b3 : newbas360) {
						tot += b3.getActivate();
					}
					
					map.put("list", newbas360);
					map.put("total", tot);
				}
				map.put("qrcode", qrCode);
				//	map.put("total", total);
				success = NotifyUtil.success(map);
				return success;
				}
			}
	        map.put("list", bas);
			map.put("total", total);
			map.put("qrcode", qrCode);
		//	map.put("total", total);
			success = NotifyUtil.success(map);
			return success;
		}
		
		return success;
	}

	@Override
	public String getUser(Integer userId) {
		TUserInfoBean bean = userInfoBeanMapper.selectByPrimaryKey(userId);
		return NotifyUtil.success(bean);
	}
	
}

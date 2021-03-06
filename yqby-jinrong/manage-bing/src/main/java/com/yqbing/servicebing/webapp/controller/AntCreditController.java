package com.yqbing.servicebing.webapp.controller;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.docx4j.model.datastorage.XPathEnhancerParser.main_return;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.yqbing.servicebing.common.ErrorCode;
import com.yqbing.servicebing.common.Layui;
import com.yqbing.servicebing.common.ResultModel;
import com.yqbing.servicebing.repository.database.bean.OfferOpenBean;
import com.yqbing.servicebing.repository.database.bean.StoreOrderBean;
import com.yqbing.servicebing.repository.database.bean.WeBankBean;
import com.yqbing.servicebing.repository.database.bean.ZanclickFqBean;
import com.yqbing.servicebing.repository.database.bean.ZanclickLogBean;
import com.yqbing.servicebing.repository.database.bean.ZanclickOffBean;
import com.yqbing.servicebing.repository.database.bean.ZanclickRefundBean;
import com.yqbing.servicebing.service.AntCreditService;
import com.yqbing.servicebing.service.WeBankService;
import com.yqbing.servicebing.webapp.response.Antstatistics;
import com.yqbing.servicebing.webapp.response.ZanclickLogRes;

/**
 * 

 * @ClassName: AntCreditController

 * @Description: 花呗分期付款

 * @author: mpb

 * @date: 2019年2月11日 上午11:47:11
 */
@SuppressWarnings("all")
@RestController
public class AntCreditController extends BaseController{

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AntCreditController.class);
	
	@Resource(name="antCreditService")
	private AntCreditService antCreditService = null;
	
	
	@RequestMapping(value ={"/manageWB/stroreOpenOrderPage"},method=RequestMethod.GET)
	public  ModelAndView  stroreOpenOrderPage(){
		
		return new ModelAndView("wzBac/html/zanclick/zanClickStore");
	}
	//商铺开户信息列表
	@RequestMapping(value ={"/manageWB/stroreOpenOrder"},method=RequestMethod.GET)
	public  Layui  stroreOpenOrder(String statustype,String storeName,String stime,String etime,int page,int limit){
		LOGGER.info("-----------------------------------------交易记录上传参数--name:"+storeName+",page:"+page+",limit:"+limit);
		Layui data = null;
		try {
			
			PageInfo<StoreOrderBean>  zls = antCreditService.stroreOpenOrder(statustype,storeName,stime, etime, page,limit);
			if(null != zls){
				
				data = Layui.data(zls.getTotal(),zls.getList());
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return data;
	}
	
	@RequestMapping(value ={"/manageWB/zanClickLogPage"},method=RequestMethod.GET)
	public  ModelAndView  zanClickLogPage(){
		
		return new ModelAndView("wzBac/html/zanclick/zanClickLog");
	}
	
	
	//交易记录订单列表
	@RequestMapping(value ={"/manageWB/zanClickLog"},method=RequestMethod.GET)
	public  Layui  zanClickLog(String storeName,String stime,String etime,int page,int limit){
		LOGGER.info("-----------------------------------------交易记录上传参数--name:"+storeName+",page:"+page+",limit:"+limit);
		Layui data = null;
		try {
			
			PageInfo<ZanclickLogRes> zls= antCreditService.queryZanClickLog(storeName,stime,etime ,page, limit);
			
          if(null != zls){
				
				data = Layui.data(zls.getTotal(),zls.getList());
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return data;
	}
	
	//订单详情列表
	@RequestMapping(value ={"/manageWB/zanClickLogDatail"},method=RequestMethod.POST)
	public  ResultModel  zanClickLogDatail(Integer StoreId,String out_trade_no,String trade_no){
		
		ResultModel resultModel = ResultModel.success();
		try {
			
			resultModel = antCreditService.queryZanClickLogDatail(StoreId,out_trade_no, trade_no);
		} catch (Exception e) {
			// TODO: handle exception
			resultModel.error(ErrorCode.OTHERS, e.getMessage());
		}
		
		return resultModel;
	}
	//发送财富功能
		@RequestMapping(value ={"/manageWB/sendWealth"},method=RequestMethod.POST)
		public  ResultModel  sendWealth(Integer StoreId,String trade_no){
			
			ResultModel resultModel = ResultModel.success();
			try {
				if(StringUtils.isBlank(trade_no)){
					resultModel = ResultModel.error(ErrorCode.PARAM_NULL,ErrorCode.getErrInfo(ErrorCode.PARAM_NULL));
					return resultModel;
				}
				
				if(StoreId ==0 ){
					resultModel = ResultModel.error(ErrorCode.PARAM_NULL,ErrorCode.getErrInfo(ErrorCode.PARAM_NULL));
					return resultModel;
				}
				resultModel = antCreditService.sendWealth(StoreId,trade_no);
			} catch (Exception e) {
				// TODO: handle exception
				resultModel.error(ErrorCode.OTHERS, ErrorCode.getErrInfo(ErrorCode.OTHERS));
			}
			
			return resultModel;
		}
	//
		
	//跳转到可以退款列表	
	@RequestMapping(value ={"/manageWB/refundPage"},method=RequestMethod.GET)
	public  ModelAndView  refundPage(){
			
			return new ModelAndView("wzBac/html/zanclick/refundPage");
	}
	//可以退款列表
	@RequestMapping(value ={"/manageWB/refundList"},method=RequestMethod.GET)
	public Layui    refundList(String name,String stime,String etime,int page,int limit){
			
		LOGGER.info("-----------------------------------------交易记录上传参数--name:"+name+",page:"+page+",limit:"+limit);
		Layui data = null;
		try {
			
			PageInfo<ZanclickLogRes> zls= antCreditService.queryRefundPageList(name,stime,etime ,page, limit);
			
          if(null != zls){
				
				data = Layui.data(zls.getTotal(),zls.getList());
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return data;
		}
	
	//退款功能
	@RequestMapping(value ={"/manageWB/refund"},method=RequestMethod.POST)
	public  ResultModel  refund(Integer StoreId,String out_trade_no,String trade_no,String ali_trade_no){
		
		ResultModel resultModel = ResultModel.success();
		try {
			
			resultModel = antCreditService.refund(StoreId,out_trade_no, trade_no, ali_trade_no);
		} catch (Exception e) {
			// TODO: handle exception
			resultModel.error(ErrorCode.OTHERS, e.getMessage());
		}
		
		return resultModel;
	}
	//跳转到已经退款列表	
	@RequestMapping(value ={"/manageWB/alreadyRefundPage"},method=RequestMethod.GET)
	public  ModelAndView  alreadyRefundPage(){
			
			return new ModelAndView("wzBac/html/zanclick/alreadyRefundPage");
	}
	//已经退款
	@RequestMapping(value ={"/manageWB/alreadyRefundList"},method=RequestMethod.GET)
	public Layui    alreadyRefundList(String name,String stime,String etime,int page,int limit){
			
		LOGGER.info("-----------------------------------------交易记录上传参数--name:"+name+",page:"+page+",limit:"+limit);
		Layui data = null;
		try {
			
			PageInfo<ZanclickRefundBean> zls= antCreditService.queryAlreadyRefundList(name,page, limit);
			
          if(null != zls){
				
				data = Layui.data(zls.getTotal(),zls.getList());
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return data;
		}
	
	
	
	//分期费率
		@RequestMapping(value ={"/manageWB/zanClickfqPage"},method=RequestMethod.GET)
		public  ModelAndView  zanClickfqPage(){
			
			return new ModelAndView("wzBac/html/zanclick/zanClickfq");
		}
		//费率表
		@RequestMapping(value ={"/manageWB/zanClickfq"},method=RequestMethod.GET)
		public  Layui  zanClickfq(){
			
			Layui data = null;
			try {
				
				List<ZanclickFqBean> zls= antCreditService.queryZanClickfq();
				
					
				data = Layui.data(1,zls);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			return data;
		}
		
		
	//添加
		@RequestMapping(value ={"/manageWB/apendzanClickfq"},method=RequestMethod.POST)
		public  ResultModel  apendzanClickfq(String fq0,String fq6,String fq12,String fq24){
			
			ResultModel resultModel = ResultModel.success();
			try {
				if(StringUtils.isBlank( fq0)){
					resultModel = ResultModel.error(ErrorCode.E1020701,ErrorCode.getErrInfo(ErrorCode.E1020701));
					
					return resultModel;
				}
				if(StringUtils.isBlank( fq6)){
					resultModel = ResultModel.error(ErrorCode.E1020701,ErrorCode.getErrInfo(ErrorCode.E1020701));
					return resultModel;
				}
				if(StringUtils.isBlank( fq12)){
					resultModel = ResultModel.error(ErrorCode.E1020701,ErrorCode.getErrInfo(ErrorCode.E1020701));
					return resultModel;
				}
				if(StringUtils.isBlank( fq24)){
					resultModel = ResultModel.error(ErrorCode.E1020701,ErrorCode.getErrInfo(ErrorCode.E1020701));
					return resultModel;
				}
				resultModel = antCreditService.apendzanClickfq(fq0, fq6, fq12, fq24);
				
			} catch (Exception e) {
				// TODO: handle exception
				resultModel = ResultModel.error(ErrorCode.OTHERS,
						ErrorCode.getErrInfo(ErrorCode.OTHERS));
				
			}
			
			return resultModel;
		}
		//---------------------------开关页面
		
		@RequestMapping(value ={"/manageWB/zanopenPage"},method=RequestMethod.GET)
		  public  ModelAndView  offerisopenPage(){
			
			return new ModelAndView("wzBac/html/zanclick/zanoffer");
		   }
		 @RequestMapping(value ={"/manageWB/queryzanopen"},method=RequestMethod.GET)
		    public ResultModel queryzanopen(){
				ResultModel resultModel = ResultModel.success();
				try {
					ZanclickOffBean ob = antCreditService.queryZanopen();
					
					resultModel.setResult(ob);
				} catch (Exception e) {
					e.getStackTrace();
				}
				
				return resultModel;
			}
		
		 @RequestMapping(value ={"/manageWB/zanopen"},method=RequestMethod.GET)
		 public ResultModel zanopen(String off){
				
			
			ResultModel resultModel = ResultModel.success();
			try {
				int s = antCreditService.zanopen(off);
				if(s < 0){
					resultModel = ResultModel.error(ErrorCode.OTHERS,
							ErrorCode.getErrInfo(ErrorCode.OTHERS));
					return resultModel;
				}
				resultModel.setResult(s);
			} catch (Exception e) {
				
			}
			
			return resultModel;
				
			//	return data;
			}
		 
		    @RequestMapping(value ={"/manageWB/setStoreName"},method=RequestMethod.GET)
			public void setStoreName(){
					antCreditService.setStoreName();
			}
		   //统计交易金额 
		    @RequestMapping(value ={"/manageWB/statisticsPage"},method=RequestMethod.GET)
			public  ModelAndView  statisticsPage(){
				
				return new ModelAndView("wzBac/html/zanclick/statistics");
		}
		    
		  //
			@RequestMapping(value ={"/manageWB/statistics"},method=RequestMethod.GET)
			public  List<Antstatistics>  statistics(String stime,String etime){
				List<Antstatistics>	ants =  null;
				LOGGER.info("-----------------------------------------交易记录上传参数--,etime:"+etime);
				try {
					
					ants =  antCreditService.statistics(stime,etime);
					
		         
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				

				return ants;
			}
			
			
			public static String[] chars36 = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
			public static String[] chars62 = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		 
			public static String generateInviteCode(){
				StringBuffer shortBuffer = new StringBuffer();
				String uuid = UUID.randomUUID().toString().replace("-", "");
				for (int i = 0; i < 8; i++)
				{
					String str = uuid.substring(i * 4, i * 4 + 4);
					int x = Integer.parseInt(str, 16);
					// 如果是 chars62,则是x%62
					shortBuffer.append(chars62[x % 36]);
				}
				return shortBuffer.toString();
			}

			
			public static void main(String[] args) {
				System.out.println(generateInviteCode());
			}
	
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
}

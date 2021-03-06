package com.yqbing.servicebing.webapp.controller;

import io.swagger.annotations.Api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.github.wxpay.sdk.WXPayConstants;
import com.google.common.collect.Maps;
import com.yqbing.servicebing.exception.ObjectNotExistsException;
import com.yqbing.servicebing.exception.ParameterInvalidException;
import com.yqbing.servicebing.repository.database.bean.AlipayAppConfig;
import com.yqbing.servicebing.repository.database.bean.BalanceAppConfig;
import com.yqbing.servicebing.repository.database.bean.Customer;
import com.yqbing.servicebing.repository.database.bean.PayAppInfo;
import com.yqbing.servicebing.repository.database.bean.PayChannelInfo;
import com.yqbing.servicebing.repository.database.bean.Trade;
import com.yqbing.servicebing.service.ICustomerService;
import com.yqbing.servicebing.service.IPayChannelService;
import com.yqbing.servicebing.service.ITradeService;
import com.yqbing.servicebing.utils.BigDecimalUtil;
import com.yqbing.servicebing.utils.HttpUtil;
import com.yqbing.servicebing.utils.enums.ErrorCode;
import com.yqbing.servicebing.utils.json.JSONObject;
import com.yqbing.servicebing.utils.json.JSONUtils;
import com.yqbing.servicebing.utils.pay.PaymentErrorCodeEnum;
import com.yqbing.servicebing.utils.pay.SignUtil;
import com.yqbing.servicebing.utils.pay.enums.CustomerStatusEnum;
import com.yqbing.servicebing.utils.web.CommonResult;
import com.yqbing.servicebing.webapp.request.dto.WxpayAppConfig;
import com.yqbing.servicebing.webapp.request.pay.enums.PayChannelEnum;
import com.yqbing.servicebing.webapp.request.trade.dto.Goods;
import com.yqbing.servicebing.webapp.request.trade.dto.TradeInfo;
import com.yqbing.servicebing.webapp.request.trade.dto.TradeInit;
import com.yqbing.servicebing.webapp.request.trade.enums.TradeStatusEnum;

@RestController
@RequestMapping(value="/app/pay/api")
@Api(tags = "下订单")
public class PayController {

	
	private static final Logger logger = LoggerFactory.getLogger(PayController.class);
	
	@Value("${pay.callback.doamin}")
	private String payCallbackDomain;
	
	@Resource
	private ICustomerService customerService;
	
	@Resource
	private ITradeService tradeService;
	
	@Resource
	private IPayChannelService payChannelService;
	
	
	
	
	private volatile Map<String, AlipayClient> alipayClients = Maps.newHashMap();
	/**
	  *   生成订单位
	 * @param request
	 * @param customerId
	 * @param orderId
	 * @param payAmount
	 * @param totalAmount
	 * @param goods
	 * @param returnUrl
	 * @param notifyUrl
	 * @param userId
	 * @param callbackParams
	 * @param charset
	 * @param signType
	 * @param sign
	 * @param payChannel
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(value="/pay")
	public CommonResult pay(HttpServletRequest request,
			@RequestParam(value="appChannel") Integer appChannel,
			@RequestParam(value="customerId") Integer customerId,
			@RequestParam(value="orderId") String orderId,
			@RequestParam(value="payAmount") Long payAmount,
			@RequestParam(value="totalAmount") Long totalAmount,
			@RequestParam(value="goods") String goods,
			@RequestParam(value="returnUrl") String returnUrl,
			@RequestParam(value="notifyUrl") String notifyUrl,			
			@RequestParam(value="userId", defaultValue="0") Long userId,
			@RequestParam(value="callbackParams", defaultValue="") String callbackParams,
			@RequestParam(value="version", defaultValue="1.0") String version,
			@RequestParam(value="charset", defaultValue="utf-8") String charset,
			@RequestParam(value="signType", defaultValue="MD5") String signType,
			@RequestParam(value="sign") String sign,
			@RequestParam(value="payChannel", required=false) String payChannel) throws UnsupportedEncodingException{

		if(appChannel == null || appChannel.intValue() <= 0) {
			throw new ParameterInvalidException(PaymentErrorCodeEnum.APP_CHANNEL_NOT_EXIST, "appChannel " + appChannel + " is null!");
		}
		
		//获取账户信息
		Customer customer = customerService.getCustomer(customerId);
		if(customer == null){
			throw new ParameterInvalidException(PaymentErrorCodeEnum.CUSTOMER_NOT_EXIST, "customer " + customerId + " not exist!");
		}
		if(customer.getCustomerStatus() != CustomerStatusEnum.ENABLED.getId()){
			throw new ParameterInvalidException(PaymentErrorCodeEnum.CUSTOMER_DISABLED, "customer " + customerId + " is not enabled!");
		}
		
		//检查所选支付渠道是否可用
		PayChannelInfo payChannelInfo = payChannelService.getPayChannelInfo(payChannel);
		if(payChannelInfo == null){
			throw new ParameterInvalidException(PaymentErrorCodeEnum.PAY_CHANNEL_INVALID, "PayChannel " + payChannel + " invalid!");
		}

		//获取签名原始值
		Map<String, String> paramsMap = Maps.newHashMap();
		Enumeration paramNames = request.getParameterNames();
		while(paramNames.hasMoreElements()){
			String paramName = paramNames.nextElement().toString();
			paramsMap.put(paramName, request.getParameter(paramName));
		}
		
		//检查签名
		boolean checkSignSuccess = SignUtil.checkSign(paramsMap, customer.getSecurityKey(), signType, sign);
		if(!checkSignSuccess){
			throw new ParameterInvalidException(PaymentErrorCodeEnum.CUSTOMER_SIGN_INVALID, "customer " + customerId + " sign check failed!");
		}
		
		//检查是否已经支付完成,避免重复支付
		List<Trade> successTrades = tradeService.getPaySuccessTrades(customerId, orderId);
		if(successTrades != null && successTrades.size() > 0){
			throw new ParameterInvalidException(PaymentErrorCodeEnum.TRADE_IS_PAYED, "order " + orderId + " is payed!");
		}
		
		goods = URLDecoder.decode(goods, charset);
		returnUrl = URLDecoder.decode(returnUrl, charset);
		notifyUrl = URLDecoder.decode(notifyUrl, charset);
		
		//暂存订单信息
		TradeInit tradeInit = new TradeInit();
		tradeInit.setCustomerId(customerId);
		tradeInit.setOrderId(orderId);
		tradeInit.setPayAmount(payAmount);
		tradeInit.setTotalAmount(totalAmount);
		tradeInit.setGoodsItem(JSONUtils.toObject(goods, Goods.class));
		tradeInit.setReturnUrl(returnUrl);
		tradeInit.setNotifyUrl(notifyUrl);
		tradeInit.setCallbackParams(callbackParams);
		tradeInit.setPayChannel(payChannel);
		tradeInit.setUserId(userId);
		tradeInit.setAppChannel(appChannel);
		
		TradeInfo trade = tradeService.getTradeInfo(tradeInit.getCustomerId(), tradeInit.getOrderId());
		if(trade != null) {
			if(trade.getPayAmount().longValue() != tradeInit.getPayAmount()){
				throw new ParameterInvalidException(PaymentErrorCodeEnum.TRADE_ORDER_PAY_AMOUNT_IS_MODIFY, "order payAmount " + tradeInit + " is modify!");
			}		
			if(trade.getTotalAmount().longValue() != tradeInit.getTotalAmount()){
				throw new ParameterInvalidException(PaymentErrorCodeEnum.TRADE_ORDER_TOTAL_AMOUNT_IS_MODIFY, "order totalAmount " + tradeInit + " is modify!");
			}
			if(!trade.getPayChannel().equals(payChannel) || trade.getAppChannel() == null || trade.getAppChannel().intValue() != appChannel || !trade.getPayProvider().equals(payChannelInfo.getPayProvider())) {
				tradeService.updateTradePayChannel(trade.getId(), appChannel, payChannel, payChannelInfo.getPayProvider());
				trade.setAppChannel(appChannel);
				trade.setPayGroupId(customer.getPayGroupId());
				trade.setPayProvider(payChannelInfo.getPayProvider());
				trade.setPayChannel(payChannel);
			}
		}else {
			trade = tradeService.createTrade(tradeInit);
		}
		if(trade == null || trade.getId() == null){
			throw new ObjectNotExistsException(PaymentErrorCodeEnum.TRADE_CREATE_FAILED, "Trade create failed! " + tradeInit);
		}
		if(trade.getTradeStatus() != TradeStatusEnum.WAIT_PAY.getId()){
			throw new ParameterInvalidException(PaymentErrorCodeEnum.TRADE_IS_PAYED, "trade " + tradeInit.getOrderId() + " is payed!");
		}
		
		JSONObject retJo = new JSONObject();
		try {
			if(payChannel.equals(PayChannelEnum.BALANCE_APP.getChannel())) {
				retJo = balancepay(appChannel, customerId, trade, tradeInit);
			}else if(payChannel.equals(PayChannelEnum.ALIPAY_APP.getChannel())) {
				retJo = alipay(appChannel, customerId, trade, tradeInit);
			}else if(payChannel.equals(PayChannelEnum.WXPAY_APP.getChannel())) {
				retJo = wxpay(appChannel, customer, trade, tradeInit);
			}else {
				throw new ParameterInvalidException(PaymentErrorCodeEnum.PAY_CHANNEL_INVALID, "PayChannel " + payChannel + " invalid!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new CommonResult(ErrorCode.CODE_SUCCESS, "OK", retJo);
	}
	private JSONObject wxpay(Integer appChannel, Customer customer, Trade trade, TradeInit tradeInit) throws Exception {
		PayAppInfo payApp = payChannelService.getPayApp(appChannel, customer.getCustomerId(), trade.getPayGroupId(), trade.getPayProvider());
		WxpayAppConfig payChannelConfig = (WxpayAppConfig)payApp.getAppConfig();
		
		logger.info("WxpayAppConfig : " + payChannelConfig.getAppID() + "," + payChannelConfig.getMchID() + "," + payChannelConfig.getKey() + "," + payChannelConfig.isUseSandbox());
		
		WXPay wxpay = new WXPay(payChannelConfig, WXPayConstants.SignType.MD5, payChannelConfig.isUseSandbox());
		
        Map<String, String> data = new HashMap<String, String>();
        data.put("body", customer.getCustomer() + "-" + tradeInit.getGoodsItem().getSubject());
        data.put("out_trade_no", trade.getId());
        data.put("fee_type", "CNY");
        data.put("total_fee", trade.getPayAmount() + "");
        data.put("spbill_create_ip", "39.105.149.169");
        data.put("notify_url", payCallbackDomain + "/pay/callback/wxpay/notify");
        data.put("trade_type", "APP");  // 此处指定为扫码支付
        
        logger.info("data param info = {}", JSONObject.fromObject(data).toString());
        
        Map<String, String> respMap = null;
        try {
        	respMap = wxpay.unifiedOrder(data);
        } catch (Exception e) {
        	logger.error("", e);
        }
        logger.info("data param info fill data = {}", JSONObject.fromObject(data).toString());
        logger.info("WX RET = {}", JSONObject.fromObject(respMap).toString());
        
        String returnCode = respMap.get("return_code");
		if(!"SUCCESS".equals(returnCode)){
			logger.warn("WxPay order create failed! return_code=" + returnCode + " return_msg=" + respMap.get("return_msg"));
		}
		
		String resultCode = respMap.get("result_code");
		if(!"SUCCESS".equals(resultCode)){
			logger.warn("WxPay order create failed! errCode=" + respMap.get("err_code") + ", errCodeDesc (" + respMap.get("err_code_des") + ")");
		}
		
		String prepay_id = respMap.get("prepay_id");
		
		String nonceStr = WXPayUtil.generateNonceStr();
		SortedMap<String, String> getParams = new TreeMap<String, String>();
		String timeStamp = String.valueOf(getCurrentTimestamp());
		String packages = "Sign=WXPay";
		getParams.put("appid", payChannelConfig.getAppID());
		getParams.put("partnerid", payChannelConfig.getMchID());
		getParams.put("prepayid", prepay_id);
		getParams.put("package", packages);
		getParams.put("noncestr", nonceStr);
		getParams.put("timestamp", timeStamp);
//		getParams.put("signType", "MD5");
		logger.info("WXPayUtil.generateSignature param = " + JSONObject.fromObject(getParams).toString());
		String sign = WXPayUtil.generateSignature(getParams, payChannelConfig.getKey());
		logger.info("WXPayUtil.generateSignature sign = " + sign);
		JSONObject retJo = new JSONObject();
		retJo.put("appId", payChannelConfig.getAppID());
		retJo.put("partnerid", payChannelConfig.getMchID());
		retJo.put("prepay_id", prepay_id);
		retJo.put("nonceStr", nonceStr);
		retJo.put("packages", packages);
		retJo.put("orderNo", trade.getOrderId());
		retJo.put("timeStamp", timeStamp);
		retJo.put("total_fee", trade.getPayAmount());
		retJo.put("sign", sign);
		logger.info("WXPayUtil.generateSignature ret = " + retJo.toString());
		return retJo;
	}
	
	private JSONObject balancepay(int appChannel, int customerId, Trade trade, TradeInit tradeInit) {
//		String totalAmount = BigDecimalUtil.roundDown(trade.getTotalAmount(), 100, 2).toString();
		logger.info("alipay : appChannel = {}, customerId = {}, payGroupId = {}, payProvider = {}", appChannel, customerId, trade.getPayGroupId(), trade.getPayProvider());
		PayAppInfo payApp = payChannelService.getPayApp(appChannel, customerId, trade.getPayGroupId(), trade.getPayProvider());
		BalanceAppConfig payChannelConfig = (BalanceAppConfig)payApp.getAppConfig();
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("appId", payChannelConfig.getAppId());
		paramsMap.put("body", tradeInit.getGoodsItem().getSubject());
		paramsMap.put("subject", tradeInit.getGoodsItem().getSubject());
		paramsMap.put("outTradeNo", trade.getId().toString());
		paramsMap.put("totalAmount", String.valueOf(trade.getTotalAmount()));
		paramsMap.put("userId", String.valueOf(tradeInit.getUserId()));
		paramsMap.put("notifyUrl", payCallbackDomain + "/pay/callback/balance/notify");
//		String json = HttpUtil.sendPost(payCallbackDomain + "/app/goods/orders/api/balanceTrade", paramsMap);
		String json = HttpUtil.sendPost(payChannelConfig.getUrl(), paramsMap);
		JSONObject jo = JSONObject.fromObject(json);
		JSONObject retJo = new JSONObject();
		retJo.put("data", jo.getJSONObject("body"));
		return retJo;
	}
	private JSONObject alipay(int appChannel, int customerId, Trade trade, TradeInit tradeInit) {
		String totalAmount = BigDecimalUtil.roundDown(trade.getTotalAmount(), 100, 2).toString();
//		Map<String, String> bizContent = Maps.newHashMap();
//		bizContent.put("out_trade_no", trade.getId().toString());
//		bizContent.put("total_amount", totalAmount);
//		bizContent.put("subject", tradeInit.getGoodsItem().getSubject());
//		bizContent.put("body", tradeInit.getGoodsItem().getSubject());
//		bizContent.put("product_code", "QUICK_MSECURITY_PAY");
		
		logger.info("alipay : appChannel = {}, customerId = {}, payGroupId = {}, payProvider = {}", appChannel, customerId, trade.getPayGroupId(), trade.getPayProvider());
		PayAppInfo payApp = payChannelService.getPayApp(appChannel, customerId, trade.getPayGroupId(), trade.getPayProvider());
		AlipayAppConfig payChannelConfig = (AlipayAppConfig)payApp.getAppConfig();
		
		AlipayClient alipayClient = getAlipayClient(payChannelConfig);		
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
		model.setBody(tradeInit.getGoodsItem().getSubject());
		model.setSubject(tradeInit.getGoodsItem().getSubject());
		model.setOutTradeNo(trade.getId().toString());
		model.setTimeoutExpress("30m");
		model.setTotalAmount(totalAmount);
		model.setProductCode("QUICK_MSECURITY_PAY");
		request.setBizModel(model);
		request.setNotifyUrl(payCallbackDomain + "/pay/callback/alipay/notify");
	    String body = "";
	    try {
	    	AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
	    	body = response.getBody();
	    } catch (AlipayApiException e) {
	    	logger.error("", e);
	    }
	    logger.info("body = {}", body);
	    JSONObject retJo = new JSONObject();
	    retJo.put("data", body);
	    return retJo;
	}
	
	private AlipayClient getAlipayClient(AlipayAppConfig payChannelConfig){
		
		String key = payChannelConfig.getAppId();
		AlipayClient alipayClient = alipayClients.get(key);
		if(alipayClient == null){
			synchronized (this) {
				if(alipayClients.get(key) == null){
					alipayClient = new DefaultAlipayClient(payChannelConfig.getUrl(), payChannelConfig.getAppId(), 
							payChannelConfig.getAppPrivateKey(), "json", "UTF-8", payChannelConfig.getAlipayPublicKey(), "RSA2");
					alipayClients.put(key, alipayClient);
				}
			}
		}
		return alipayClient;
	}
	
	

	private long getCurrentTimestamp() {
        return System.currentTimeMillis()/1000;
    }
	
}

package com.yqbing.servicebing.utils;

import java.io.IOException;

import com.yqbing.servicebing.constants.Constant;


public class SendSms {

	public static int sendSms(String phone) {
		int code = -1;
		//随机生成验证码位数不确定
//		code = (int) (Math.random() * 900000);
		//随机生成验证码位数只有6位
	    code=(int)((Math.random()*9+1)*100000);
		try {
//			JavaSmsApi.sendSms(JavaSmsApi.APIKEY, text, phone);
			String text = Constant.SMS_DESC + code;
			
			// upd gavin 2018/03/15 Start
//			JavaSmsApi.tplSendSms(JavaSmsApi.APIKEY, 1783962, text, phone);
			JavaSmsApi.tplSendSms(JavaSmsApi.APIKEY, 3181448, text, phone);
			// upd gavin 2018/03/15 End
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return code;
	}
	
	public static int sendMessage(String phone,String text){
		int code = -1;
		code = (int) (Math.random() * 900000);
		try {
			//String text = Constant.SMS_DESC + code;
//			JavaSmsApi.sendSms(JavaSmsApi.APIKEY, text, phone);
			
			JavaSmsApi.tplSendSms(JavaSmsApi.APIKEY, 1783962, text, phone);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return code;
	}
	public static void main(String[] args) {
	//	int sendSms = sendSms("18035823101");
		 System.out.println(sendSms("18035823101"));
		//System.out.println("J17000020".contains("J17"));
	}

}

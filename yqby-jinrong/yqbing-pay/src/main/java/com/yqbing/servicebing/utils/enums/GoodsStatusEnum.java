package com.yqbing.servicebing.utils.enums;

public enum GoodsStatusEnum {
	CODE_DELETEED(-2, "已删除"),
	CODE_CANCEL(-1, "已下架"),
	CODE_WAIT_AUDIT(0, "待审核"),
	CODE_AUDITED(1, "已审核");
	
	private int code;
	
	private String message;

	private GoodsStatusEnum(int code, String message) {
		this.code = code;
		this.message = message;
	}


	public String getMessage() {
		return message;
	}


	public int getCode() {
		return code;
	}

}
